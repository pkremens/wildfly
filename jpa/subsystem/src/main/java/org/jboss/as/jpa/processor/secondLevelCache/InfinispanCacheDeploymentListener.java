/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.jpa.processor.secondLevelCache;

import static org.jboss.as.jpa.messages.JpaLogger.ROOT_LOGGER;

import java.security.AccessController;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.as.clustering.msc.ServiceContainerHelper;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jipijapa.cache.spi.Classification;
import org.jipijapa.cache.spi.Wrapper;
import org.jipijapa.event.spi.EventListener;
import org.jipijapa.plugin.spi.PersistenceUnitMetadata;
import org.wildfly.clustering.infinispan.spi.service.CacheContainerServiceName;
import org.wildfly.clustering.infinispan.spi.service.CacheServiceName;
import org.wildfly.clustering.service.AliasServiceBuilder;

/**
 * InfinispanCacheDeploymentListener adds Infinispan second level cache dependencies during application deployment.
 *
 * @author Scott Marlow
 * @author Paul Ferraro
 */
public class InfinispanCacheDeploymentListener implements EventListener {

    public static final String CACHE_TYPE = "cachetype";    // shared (jpa) or private (for native applications)
    public static final String CACHE_PRIVATE = "private";
    public static final String CONTAINER = "container";
    public static final String COLLECTION = "collection";
    public static final String ENTITY = "entity";
    public static final String IMMUTABLE_ENTITY = "immutable-entity";
    public static final String NAME = "name";
    public static final String NATURAL_ID = "natural-id";
    public static final String QUERY = "query";
    public static final String TIMESTAMPS = "timestamps";
    public static final String PENDING_PUTS = "pending-puts";
    public static final String CUSTOM = "custom";

    public static final String DEFAULT_CACHE_CONTAINER = "hibernate";

    @Override
    public void beforeEntityManagerFactoryCreate(Classification classification, PersistenceUnitMetadata persistenceUnitMetadata) {

    }

    @Override
    public void afterEntityManagerFactoryCreate(Classification classification, PersistenceUnitMetadata persistenceUnitMetadata) {

    }

    @Override
    public Wrapper startCache(Classification classification, Properties properties) throws Exception {
        String cache_type = properties.getProperty(CACHE_TYPE);
        String container = properties.getProperty(CONTAINER);
        EmbeddedCacheManager embeddedCacheManager;
        ServiceName serviceName;
        if (CACHE_PRIVATE.equals(cache_type)) {
            // need a private cache for non-jpa application use
            String name = properties.getProperty(NAME);
            serviceName = ServiceName.JBOSS.append(DEFAULT_CACHE_CONTAINER, (name != null) ? name : UUID.randomUUID().toString());

            ServiceContainer target = currentServiceContainer();
            // Create a mock service that represents this session factory instance
            ServiceBuilder<EmbeddedCacheManager> builder = new AliasServiceBuilder<>(serviceName, CacheContainerServiceName.CACHE_CONTAINER.getServiceName(container), EmbeddedCacheManager.class).build(target)
                    .setInitialMode(ServiceController.Mode.ACTIVE)
            ;
            embeddedCacheManager = ServiceContainerHelper.getValue(builder.install());

        } else {
            // need a shared cache for jpa applications
            serviceName = CacheContainerServiceName.CACHE_CONTAINER.getServiceName(container);
            ServiceRegistry registry = currentServiceContainer();
            embeddedCacheManager = (EmbeddedCacheManager) registry.getRequiredService(serviceName).getValue();
        }
        return new CacheWrapper(embeddedCacheManager, serviceName);
    }

    @Override
    public void addCacheDependencies(Classification classification, Properties properties) {
        String container = properties.getProperty(CONTAINER);
        String entity = properties.getProperty(ENTITY);
        String immutableEntity = properties.getProperty(IMMUTABLE_ENTITY);
        String naturalId = properties.getProperty(NATURAL_ID);
        String collection = properties.getProperty(COLLECTION);
        String query = properties.getProperty(QUERY);
        String timestamps  = properties.getProperty(TIMESTAMPS);
        String pendingPuts = properties.getProperty(PENDING_PUTS);
        String custom = properties.getProperty(CUSTOM);
        addDependency(CacheServiceName.CONFIGURATION.getServiceName(container, entity));
        addDependency(CacheServiceName.CONFIGURATION.getServiceName(container, immutableEntity));
        addDependency(CacheServiceName.CONFIGURATION.getServiceName(container, collection));
        addDependency(CacheServiceName.CONFIGURATION.getServiceName(container, naturalId));
        if (pendingPuts != null) {
            addDependency(CacheServiceName.CONFIGURATION.getServiceName(container, pendingPuts));
        }
        if (query != null) {
            addDependency(CacheServiceName.CONFIGURATION.getServiceName(container, timestamps));
            addDependency(CacheServiceName.CONFIGURATION.getServiceName(container, query));
        }
        if (custom != null) {
            Stream.of(custom.split("\\s+")).forEach(config -> addDependency(CacheServiceName.CONFIGURATION.getServiceName(container, config)));
        }
    }

    private void addDependency(ServiceName dependency) {
        if(ROOT_LOGGER.isTraceEnabled()) {
            ROOT_LOGGER.tracef("add second level cache dependency on service '%s'", dependency.getCanonicalName());
        }
        CacheDeploymentListener.getInternalDeploymentServiceBuilder().addDependency(dependency);
    }

    @Override
    public void stopCache(Classification classification, Wrapper wrapper, boolean ignoreStop) {
        if (!ignoreStop) {
            // Remove the service created in createCacheManager(...)
            CacheWrapper cacheWrapper = (CacheWrapper) wrapper;
            if(ROOT_LOGGER.isTraceEnabled()) {
                ROOT_LOGGER.tracef("stop second level cache by removing dependency on service '%s'", cacheWrapper.serviceName.getCanonicalName());
            }
            ServiceContainerHelper.remove(currentServiceContainer().getRequiredService(cacheWrapper.serviceName));
        } else if(ROOT_LOGGER.isTraceEnabled()){
            CacheWrapper cacheWrapper = (CacheWrapper) wrapper;
            ROOT_LOGGER.tracef("skipping stop of second level cache, will keep dependency on service '%s'", cacheWrapper.serviceName.getCanonicalName());
        }
    }

    private static class CacheWrapper implements Wrapper {

        public CacheWrapper(EmbeddedCacheManager embeddedCacheManager, ServiceName serviceName) {
            this.embeddedCacheManager = embeddedCacheManager;
            this.serviceName = serviceName;
        }

        private final EmbeddedCacheManager embeddedCacheManager;
        private final ServiceName serviceName;

        @Override
        public Object getValue() {
            return embeddedCacheManager;
        }
    }

    private static ServiceContainer currentServiceContainer() {
        if(System.getSecurityManager() == null) {
            return CurrentServiceContainer.getServiceContainer();
        }
        return AccessController.doPrivileged(CurrentServiceContainer.GET_ACTION);
    }
}
