/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.as.cli.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLExtendedStreamReader;


/**
 * @author Alexey Loubyansky
 *
 */
class VaultConfig {

    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String VAULT = "vault";
    private static final String CODE = "code";
    private static final String MODULE = "module";
    private static final String VAULT_OPTION = "vault-option";


    private String code;
    private String module;
    private final Map<String, Object> options = new HashMap<String, Object>();

    /**
     * Parse the vault config
     *
     * @param reader the reader at the vault element
     * @param expectedNs the namespace
     * @return the vault configuration
     */
    static VaultConfig readVaultElement_1_3(XMLExtendedStreamReader reader, Namespace expectedNs) throws XMLStreamException {
        final VaultConfig config = new VaultConfig();

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String value = reader.getAttributeValue(i);
            String name = reader.getAttributeLocalName(i);
            if (name.equals(CODE)){
                config.code = value;
            } else if (name.equals(MODULE)){
                config.module = value;
            } else {
                unexpectedVaultAttribute(reader.getAttributeLocalName(i), reader);
            }
        }
        if (config.code == null && config.module != null){
            throw new XMLStreamException("Attribute 'module' was specified without an attribute"
                    + " 'code' for element '" + VAULT + "' at " + reader.getLocation());
        }
        readVaultOptions(reader, config);
        return config;
    }

    Map<String, Object> getOptions() {
        return options;
    }

    String getCode() {
        return code;
    }

    String getModule() {
        return module;
    }

    private void addOption(String name, String value) {
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is null or empty");
        }
        if(value == null || value.isEmpty()) {
            throw new IllegalArgumentException("value is null or empty");
        }
        options.put(name, value);
    }

    private static void readVaultOptions(XMLExtendedStreamReader reader, VaultConfig config) throws XMLStreamException {
        boolean done = false;
        while (reader.hasNext() && done == false) {
            int tag = reader.nextTag();
            if(tag == XMLStreamConstants.START_ELEMENT) {
                final String localName = reader.getLocalName();
                if (localName.equals(VAULT_OPTION)) {
                    final String name = reader.getAttributeValue(null, NAME);
                    if(name == null) {
                        throw new XMLStreamException("Attribute '" + NAME +
                                "' is not found for element '" +
                                VAULT_OPTION + "' at " + reader.getLocation());
                    }
                    final String value = reader.getAttributeValue(null, VALUE);
                    if(value == null) {
                        throw new XMLStreamException("Attribute '" + VALUE +
                                "' is not found for element " +
                                VAULT_OPTION + "' at " + reader.getLocation());
                    }
                    config.addOption(name.trim(), value.trim());
                    CliConfigImpl.CliConfigReader.requireNoContent(reader);
                } else {
                    throw new XMLStreamException("Unexpected element: " + localName);
                }
            } else if(tag == XMLStreamConstants.END_ELEMENT) {
                final String localName = reader.getLocalName();
                if (localName.equals(VAULT)) {
                    done = true;
                }
            }
        }
    }

    private static void unexpectedVaultAttribute(String attribute, XMLStreamReader reader) throws XMLStreamException {
        throw new XMLStreamException("Attribute '" + attribute +
                "' is unknown for element '" +
                VAULT_OPTION + "' at " + reader.getLocation());

    }

    private static class ExternalVaultConfigReader implements XMLElementReader<VaultConfig> {

        @Override
        public void readElement(XMLExtendedStreamReader reader, VaultConfig config) throws XMLStreamException {

            String rootName = reader.getLocalName();
            if (VAULT.equals(rootName) == false) {
                throw new XMLStreamException("Unexpected element: " + rootName);
            }

            final int count = reader.getAttributeCount();
            for (int i = 0; i < count; i++) {
                final String value = reader.getAttributeValue(i);
                unexpectedVaultAttribute(reader.getAttributeLocalName(i), reader);
            }
            if (config.code == null && config.module != null){
                throw new XMLStreamException("Attribute 'module' was specified without an attribute"
                        + " 'code' for element '" + VAULT + "' at " + reader.getLocation());
            }
            readVaultOptions(reader, config);
        }
    }
}