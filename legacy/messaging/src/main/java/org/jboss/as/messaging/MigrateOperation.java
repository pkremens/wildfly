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

package org.jboss.as.messaging;

import static org.jboss.as.controller.OperationContext.Stage.MODEL;
import static org.jboss.as.controller.PathAddress.EMPTY_ADDRESS;
import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODULE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.STEPS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.operations.common.Util.createAddOperation;
import static org.jboss.as.controller.operations.common.Util.createOperation;
import static org.jboss.as.controller.operations.common.Util.createRemoveOperation;
import static org.jboss.as.messaging.CommonAttributes.ACCEPTOR;
import static org.jboss.as.messaging.CommonAttributes.BRIDGE;
import static org.jboss.as.messaging.CommonAttributes.CLUSTER_CONNECTION;
import static org.jboss.as.messaging.CommonAttributes.CONNECTION_FACTORY;
import static org.jboss.as.messaging.CommonAttributes.CONNECTOR;
import static org.jboss.as.messaging.CommonAttributes.CONNECTOR_REF_STRING;
import static org.jboss.as.messaging.CommonAttributes.DISCOVERY_GROUP_NAME;
import static org.jboss.as.messaging.CommonAttributes.HORNETQ_SERVER;
import static org.jboss.as.messaging.CommonAttributes.HTTP_ACCEPTOR;
import static org.jboss.as.messaging.CommonAttributes.HTTP_CONNECTOR;
import static org.jboss.as.messaging.CommonAttributes.POOLED_CONNECTION_FACTORY;
import static org.jboss.as.messaging.CommonAttributes.REMOTE_ACCEPTOR;
import static org.jboss.as.messaging.CommonAttributes.REMOTE_CONNECTOR;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.CompositeOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 * Operation to migrate from the legacy messaging subsystem to the new messaging-activemq subsystem.
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2015 Red Hat inc.
 */

public class MigrateOperation implements OperationStepHandler {

    private static final OperationStepHandler DESCRIBE_MIGRATION_INSTANCE = new MigrateOperation(true);
    private static final OperationStepHandler MIGRATE_INSTANCE = new MigrateOperation(false);

    private final boolean describe;

    private MigrateOperation(boolean describe) {

        this.describe = describe;
    }

    static void registerOperations(ManagementResourceRegistration registry, ResourceDescriptionResolver resourceDescriptionResolver) {
        registry.registerOperationHandler(new SimpleOperationDefinitionBuilder("migrate", resourceDescriptionResolver)
                        .setRuntimeOnly()
                        .build(),
                MigrateOperation.MIGRATE_INSTANCE);
        registry.registerOperationHandler(new SimpleOperationDefinitionBuilder("describe-migration", resourceDescriptionResolver)
                        .setRuntimeOnly()
                        .build(),
                MigrateOperation.DESCRIBE_MIGRATION_INSTANCE);
    }

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        if (!describe && context.getRunningMode() != RunningMode.ADMIN_ONLY) {
            throw new OperationFailedException("the messaging migration can be performed when the server is in admin-only mode");
        }

        // node containing the description (list of add operations) of the legacy subsystem
        final ModelNode legacyModelAddOps = new ModelNode();
        // preserve the order of insertion of the add operations for the new subsystem.
        final Map<PathAddress, ModelNode> migrationOperations = new LinkedHashMap<PathAddress, ModelNode>();

        // invoke an OSH to describe the legacy messaging subsystem
        describeLegacyMessagingResources(context, legacyModelAddOps);
        // invoke an OSH to add the messaging-activemq extension
        // FIXME: this does not work it the extension :add is added to the migrationOperation directly
        addMessagingActiveMQExtension(context, migrationOperations, describe);
        context.addStep(new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
                // transform the legacy add operations and put them in migrationOperations
                transformResources(legacyModelAddOps, migrationOperations);
                // put the /subsystem=messaging:remove operation
                removeMessagingSubsystem(migrationOperations);
                // put the /extension=org.jboss.asmessaging:remove operation
                removeMessagingExtension(migrationOperations);

                if (describe) {
                    // for describe-migration operation, do nothing and return the list of operations that would
                    // be executed in the composite operation
                    context.getResult().set(migrationOperations.values());
                } else {
                    // invoke an OSH on a composite operation with all the migration operations
                    migrateSubsystems(context, migrationOperations);
                }
            }
        }, MODEL);
    }


    private void addMessagingActiveMQExtension(OperationContext context, Map<PathAddress, ModelNode> migrationOperations, boolean dryRun) {
        PathAddress extensionAddress = pathAddress(EXTENSION, "org.wildfly.extension.messaging-activemq");
        OperationEntry addEntry = context.getRootResourceRegistration().getOperationEntry(extensionAddress, ADD);
        ModelNode addOperation = createAddOperation(extensionAddress);
        addOperation.get(MODULE).set("org.widlfy.extension.messaging-activemq");
        if (dryRun) {
            migrationOperations.put(extensionAddress, addOperation);
        } else {
            context.addStep(context.getResult().get(extensionAddress.toString()), addOperation, addEntry.getOperationHandler(), MODEL);
        }
    }

    private void removeMessagingSubsystem(Map<PathAddress, ModelNode> migrationOperations) {
        PathAddress subsystemAddress =  pathAddress(MessagingExtension.SUBSYSTEM_PATH);
        ModelNode removeOperation = createRemoveOperation(subsystemAddress);
        migrationOperations.put(subsystemAddress, removeOperation);
    }

    private void removeMessagingExtension(Map<PathAddress, ModelNode> migrationOperations) {
        PathAddress extensionAddress = pathAddress(EXTENSION, "org.jboss.as.messaging");
        ModelNode removeOperation = createRemoveOperation(extensionAddress);
        migrationOperations.put(extensionAddress, removeOperation);
    }

    private void migrateSubsystems(OperationContext context, final Map<PathAddress, ModelNode> migrationOperations) {
        ModelNode compositeOp = createOperation(COMPOSITE, EMPTY_ADDRESS);
        compositeOp.get(STEPS).set(migrationOperations.values());
        context.addStep(compositeOp, CompositeOperationHandler.INSTANCE, MODEL);
    }

    private ModelNode transformAddress(ModelNode legacyAddress) {
        ModelNode newAddress = new ModelNode();
        for (Property segment : legacyAddress.asPropertyList()) {
            final Property newSegment;
            switch (segment.getName()) {
                case CommonAttributes.SUBSYSTEM:
                    newSegment = new Property(SUBSYSTEM, new ModelNode("messaging-activemq"));
                    break;
                case HORNETQ_SERVER:
                    newSegment = new Property("server", segment.getValue());
                    break;
                default:
                    newSegment = segment;
            }
            newAddress.add(newSegment);
        }
        return newAddress;
    }

    private void transformResources(final ModelNode legacyModelDescription, final Map<PathAddress, ModelNode> newAddOperations) {
        for (ModelNode legacyAddOp : legacyModelDescription.get(RESULT).asList()) {
            final ModelNode newAddOp = legacyAddOp.clone();

            ModelNode newAddress = transformAddress(legacyAddOp.get(OP_ADDR).clone());
            newAddOp.get(OP_ADDR).set(newAddress);

            if (newAddress.asList().size() > 2) {
                Property subsystemSubresource = newAddress.asPropertyList().get(1);
                if (subsystemSubresource.getName().equals("server")) {
                    Property serverSubresource = newAddress.asPropertyList().get(2);
                    switch (serverSubresource.getName()) {
                        case CONNECTION_FACTORY:
                            migrateConnectionFactory(newAddOp);
                            break;
                        case POOLED_CONNECTION_FACTORY:
                            migratePooledConnectionFactory(newAddOp);
                            break;
                        case CLUSTER_CONNECTION:
                            migrateClusterConnection(newAddOp);
                            break;
                        case BRIDGE:
                            migrateBridge(newAddOp);
                            break;
                        case ACCEPTOR:
                        case HTTP_ACCEPTOR:
                        case REMOTE_ACCEPTOR:
                        case CONNECTOR:
                        case HTTP_CONNECTOR:
                        case REMOTE_CONNECTOR:
                            if (newAddress.asPropertyList().size() > 3) {
                                // if there are any param resource underneath connectors and acceptors,
                                // add them directly to their parent add operation in the param attributes
                                String name = newAddress.asPropertyList().get(3).getValue().asString();
                                ModelNode value = newAddOp.get(VALUE);
                                PathAddress currentAddress = pathAddress(newAddress);
                                ModelNode parentAddOp = newAddOperations.get(currentAddress.getParent());
                                parentAddOp.get("params").add(new Property(name, value));
                                continue;
                            }
                    }
                }
            }

            newAddOperations.put(pathAddress(newAddOp.get(OP_ADDR)), newAddOp);
        }
    }

    private void describeLegacyMessagingResources(OperationContext context, ModelNode legacyModelDescription) {
        ModelNode describeLegacySubsystem = createOperation(GenericSubsystemDescribeHandler.DEFINITION, context.getCurrentAddress());
        context.addStep(legacyModelDescription, describeLegacySubsystem, GenericSubsystemDescribeHandler.INSTANCE, MODEL, true);
    }

    private void migrateConnectionFactory(ModelNode addOperation) {
        migrateConnectorAttribute(addOperation);
        migrateDiscoveryGroupNameAttribute(addOperation);
    }

    private void migratePooledConnectionFactory(ModelNode addOperation) {
        migrateConnectorAttribute(addOperation);
        migrateDiscoveryGroupNameAttribute(addOperation);
    }

    private void migrateClusterConnection(ModelNode addOperation) {
        // connector-ref attribute has been renamed to connector-name
        addOperation.get("connector-name").set(addOperation.get(CONNECTOR_REF_STRING));
        addOperation.remove(CONNECTOR_REF_STRING);
    }

    private void migrateConnectorAttribute(ModelNode addOperation) {
        ModelNode connector = addOperation.get(CONNECTOR);
        if (connector.isDefined()) {
            // legacy connector is a property list where the name is the connector and the value is undefined
            List<Property> connectorProps = connector.asPropertyList();
            for (Property connectorProp : connectorProps) {
                addOperation.get("connectors").add(connectorProp.getName());
            }
            addOperation.remove(CONNECTOR);
        }
    }
    private void migrateDiscoveryGroupNameAttribute(ModelNode addOperation) {
        ModelNode discoveryGroup = addOperation.get(DISCOVERY_GROUP_NAME);
        if (discoveryGroup.isDefined()) {
            // discovery-group-name attribute has been renamed to discovery-group
            addOperation.get("discovery-group").set(discoveryGroup);
            addOperation.remove(DISCOVERY_GROUP_NAME);
        }
    }

    private void migrateBridge(ModelNode addOperation) {
        migrateDiscoveryGroupNameAttribute(addOperation);
    }
}
