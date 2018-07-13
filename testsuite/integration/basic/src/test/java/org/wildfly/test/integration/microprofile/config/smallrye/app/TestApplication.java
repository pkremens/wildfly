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

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.wildfly.test.integration.microprofile.config.smallrye.SubsystemConfigSourceTask;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2017 Red Hat inc.
 */
@ApplicationPath("/microprofile")
public class TestApplication extends Application {

    @Path("/test")
    public static class Resource {

        @Inject
        Config config;

        @Inject
        @ConfigProperty(name = "my.prop", defaultValue = "BAR")
        String prop1;

        @Inject
        @ConfigProperty(name = "my.other.prop", defaultValue = "no")
        boolean prop2;

        @Inject
        @ConfigProperty(name = SubsystemConfigSourceTask.MY_PROP_FROM_SUBSYSTEM_PROP_NAME)
        String prop3;

        @Inject
        @ConfigProperty(name = "myPets")
        String myConfigProperty;

        @Inject
        @ConfigProperty(name = "myPets", defaultValue = "dog,cat,dog\\,cat")
//        Set<String> myPets;
                String[] myPets;
//        List<String> myPets;

//        //where its value is a comma separated value ( myPets=dog,cat,dog\\,cat)
        @Inject
        @ConfigProperty(name = "myPets", defaultValue = "frog,cat,dog\\,cat")
        private String[] myArrayPets;
        @Inject
        @ConfigProperty(name = "myPets", defaultValue = "frog,cat,dog\\,cat")
        private List<String> myListPets;
        @Inject
        @ConfigProperty(name = "myPets", defaultValue = "frog,cat,dog\\,cat")
        private Set<String> mySetPets;

//        ./standalone.sh -DmyPets=dog,cat,mouse\\,mice

        //where its value is a comma separated value ( myPets=dog,cat,dog\\,cat)
//        @Inject
//        @ConfigProperty(name = "myPets")
//        private String[] myArrayPets;
//        @Inject
//        @ConfigProperty(name = "myPets")
//        private List<String> myListPets;
//        @Inject
//        @ConfigProperty(name = "myPets")
//        private Set<String> mySetPets;


        @Inject
        @ConfigProperty(name = "optional.injected.prop.that.is.not.configured")
        Optional<String> optionalProp;

        @GET
        @Produces("text/plain")
        public Response doGet() {
            Optional<String> foo = config.getOptionalValue("my.prop.never.defined", String.class);
            StringBuilder text = new StringBuilder();
            text.append("my.prop.never.defined = " + foo + "\n");
            text.append("my.prop = " + prop1 + "\n");
            text.append("my.other.prop = " + prop2 + "\n");
            text.append("optional.injected.prop.that.is.not.configured = " + optionalProp + "\n");
            text.append(SubsystemConfigSourceTask.MY_PROP_FROM_SUBSYSTEM_PROP_NAME + " = " + prop3 + "\n");
            text.append(System.lineSeparator());
            text.append("myPets test").append(System.lineSeparator());
            text.append("myArrayPets.length: " + myArrayPets.length).append(System.lineSeparator());
            text.append("myListPets.size(): " + myListPets.size()).append(System.lineSeparator());
            text.append("mySetPets.size(): " + mySetPets.size()).append(System.lineSeparator());
            text.append(System.lineSeparator());
            text.append("MY PETS " + myConfigProperty).append(System.lineSeparator());
            return Response.ok(text).build();
        }
    }
}
