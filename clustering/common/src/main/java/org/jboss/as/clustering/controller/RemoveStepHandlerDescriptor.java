/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.as.clustering.controller;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.jboss.as.controller.CapabilityReferenceRecorder;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;

/**
 * Describes the common properties of a remove operation handler.
 * @author Paul Ferraro
 */
@FunctionalInterface
public interface RemoveStepHandlerDescriptor extends OperationStepHandlerDescriptor {
    /**
     * The description resolver for the operation.
     * @return a description resolver
     */
    ResourceDescriptionResolver getDescriptionResolver();

    /**
     * Returns a mapping of capability references to an ancestor resource.
     * @return a tuple of capability references and requirement resolvers.
     */
    default Map<CapabilityReferenceRecorder, Function<PathAddress, String>> getResourceCapabilityReferences() {
        return Collections.emptyMap();
    }
}
