/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.connector.security;

import javax.security.auth.callback.CallbackHandler;

import org.jboss.jca.core.spi.security.Callback;
import org.jboss.jca.core.spi.security.SecurityContext;
import org.jboss.jca.core.spi.security.SecurityIntegration;

/**
 * SecurityIntegration implementation for Elytron.
 *
 * @author Flavia Rainone
 */
public class ElytronSecurityIntegration implements SecurityIntegration {
    /**
     * Constructor
     */
    public ElytronSecurityIntegration() {
    }

    /**
     * {@inheritDoc}
     */
    public SecurityContext createSecurityContext(String sd) throws Exception {
        // TODO
        return new ElytronSecurityContext();
    }

    /**
     * {@inheritDoc}
     */
    public SecurityContext getSecurityContext() {
        // TODO
        return new ElytronSecurityContext();
    }

    /**
     * {@inheritDoc}
     */
    public void setSecurityContext(SecurityContext context) {
        if (context == null) {
            // TODO
        }
        assert context instanceof ElytronSecurityContext;
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    public CallbackHandler createCallbackHandler() {
        return new ElytronCallbackHandler();
    }

    /**
     * {@inheritDoc}
     */
    public CallbackHandler createCallbackHandler(Callback callback) {
        return new ElytronCallbackHandler(callback);
    }

}
