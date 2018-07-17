/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

package org.wildfly.test.integration.microprofile.config.smallrye;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.test.integration.microprofile.config.smallrye.app.MicroProfileConfigTestCase;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

/**
 * Add a config-source with a property class in the microprofile-config-smallrye subsystem.
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
public class SubsystemConfigSourceTask implements ServerSetupTask {
    public static final String MY_PROP_FROM_SUBSYSTEM_PROP_NAME = "my.prop.from.subsystem";
    public static final String MY_PROP_FROM_SUBSYSTEM_PROP_VALUE = "I'm configured in the subsystem";


    @Override
    public void setup(ManagementClient managementClient, String containerId) throws Exception {
        addConfigSource(managementClient.getControllerClient(), MY_PROP_FROM_SUBSYSTEM_PROP_NAME, MY_PROP_FROM_SUBSYSTEM_PROP_VALUE);
    }

    @Override
    public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
        removeConfigSource(managementClient.getControllerClient());
    }


    private void addConfigSource(ModelControllerClient client, String propName, String propValue) throws IOException, URISyntaxException {
        ModelNode op;
        op = new ModelNode();
        op.get(OP_ADDR).add(SUBSYSTEM, "microprofile-config-smallrye");
        op.get(OP_ADDR).add("config-source", "test");
        op.get(OP).set(ADD);
        op.get(PROPERTIES).add(propName, propValue);
        client.execute(op);

        // tohle bude chtit nejak zkonstantit, ale skrz WFWIP-57 a malo casu si s tim ted nemuzu vic hrat :/
        // https://issues.jboss.org/browse/WFWIP-57 MalformedInputException is thrown in case config-source dir contains binaries
        File propertiesDir = new File(MicroProfileConfigTestCase.class.getResource("who.is.the.best").toURI()).getParentFile();
        op = new ModelNode();
        op.get(OP_ADDR).add(SUBSYSTEM, "microprofile-config-smallrye");
        op.get(OP_ADDR).add("config-source", "testDir");
        op.get(OP).set(ADD);
        op.get("dir").set(propertiesDir.getAbsolutePath());

        // Execute
        System.out.println("https://issues.jboss.org/browse/WFWIP-57 MalformedInputException is thrown in case " +
                "config-source dir contains binaries");
        System.out.println(op.toJSONString(false));
        System.out.println(client.execute(op).toJSONString(false));

    }

    private void removeConfigSource(ModelControllerClient client) throws IOException {
        ModelNode op;
        op = new ModelNode();
        op.get(OP_ADDR).add(SUBSYSTEM, "microprofile-config-smallrye");
        op.get(OP_ADDR).add("config-source", "test");
        op.get(OP).set(REMOVE);
        client.execute(op);

        op = new ModelNode();
        op.get(OP_ADDR).add(SUBSYSTEM, "microprofile-config-smallrye");
        op.get(OP_ADDR).add("config-source", "testDir");
        op.get(OP).set(REMOVE);
        client.execute(op);
    }
}
