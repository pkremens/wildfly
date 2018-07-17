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

package org.wildfly.test.integration.microprofile.config.smallrye.app;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
@ApplicationPath("/microprofile")
public class TestApplication extends Application {

    @Path("/test")
    public static class Resource {
        @Inject
        @ConfigProperty(name = "myPets", defaultValue = "default")
        String myPetsProperty;

        // ARQUILLIAN: server.jvm.args in testsuite/integration/pom.xml
        // <server.jvm.args>-DmyPets2=test ${surefire.system.args} ???
        // eapCleanup ; git wipeout ; ./bin/standalone.sh -DmyPets=snake,ox
        // RUNNING SERVER: ./standalone.sh -DmyPets=dog, cat,mouse
        @Inject
        @ConfigProperty(name = "myPets", defaultValue = "horse,monkey")
//        @ConfigProperty(name = "myPets")
        private String[] myArrayPets;


        @Inject
//        @ConfigProperty(name = "myPets", defaultValue = "cat,lama")
        @ConfigProperty(name = "myPets")
        private List<String> myListPets;

        @Inject
//        @ConfigProperty(name = "myPets", defaultValue = "dog,mouse")
        @ConfigProperty(name = "myPets")
        private Set<String> mySetPets;

        @GET
        @Produces("text/plain")
        public Response doGet() {
            StringBuilder text = new StringBuilder();
            text.append("myPets: ").append(System.lineSeparator());
            text.append("myPets: " + myPetsProperty).append(System.lineSeparator());
            text.append("myArrayPets:").append(System.lineSeparator());
            Arrays.stream(myArrayPets).forEach(animal -> text.append(" - ").append(animal).append(System.lineSeparator()));
            text.append(System.lineSeparator());
            text.append("myListPets:").append(System.lineSeparator());
            myListPets.stream().forEach(animal -> text.append(" - ").append(animal).append(System.lineSeparator()));
            text.append(System.lineSeparator());
//            text.append("myNonDefaultListPets:").append(System.lineSeparator());
//            myNonDefaultListPets.stream().forEach(animal -> text.append(" - ").append(animal).append(System.lineSeparator()));
//            text.append(System.lineSeparator());
            text.append("mySetPets:").append(System.lineSeparator());
            mySetPets.stream().forEach(animal -> text.append(" - ").append(animal).append(System.lineSeparator()));
            text.append(System.lineSeparator());
            return Response.ok(text).build();
        }
    }
}
