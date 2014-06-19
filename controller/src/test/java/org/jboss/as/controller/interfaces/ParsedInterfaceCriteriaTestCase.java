/*
* JBoss, Home of Professional Open Source.
* Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.controller.interfaces;

import java.net.Inet4Address;
import java.util.regex.Pattern;

import org.jboss.as.controller.ExpressionResolver;
import org.jboss.as.controller.parsing.Element;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ParsedInterfaceCriteriaTestCase {

    //TODO
//    @Test
//    public void testNotLoopbackAndIntetaddress() {
//        ModelNode op = new ModelNode();
//        op.get(Element.LOOPBACK.getLocalName()).set(true);
//        op.get(Element.INET_ADDRESS.getLocalName()).set("127.0.0.1");
//        ParsedInterfaceCriteria criteria = ParsedInterfaceCriteria.parse(op);
//        Assert.assertNotNull(criteria.getFailureMessage());
//    }

    @Test
    public void testNotAndSameIncludedCriteria() {
        ModelNode op = new ModelNode();
        op.get(Element.LOOPBACK.getLocalName()).set(true);
        op.get(Element.NOT.getLocalName(), Element.LOOPBACK.getLocalName()).set(true);
        ParsedInterfaceCriteria criteria = ParsedInterfaceCriteria.parse(op, true, ExpressionResolver.TEST_RESOLVER);
        Assert.assertNotNull(criteria.getFailureMessage());
    }

    @Test
    public void testNotAndDifferentIncludedCriteria() {
        ModelNode op = new ModelNode();
        op.get(Element.LOOPBACK.getLocalName()).set(true);
        op.get(Element.NOT.getLocalName(), Element.INET_ADDRESS.getLocalName()).set("127.0.0.1");
        ParsedInterfaceCriteria criteria = ParsedInterfaceCriteria.parse(op, true, ExpressionResolver.TEST_RESOLVER);
        Assert.assertNull(criteria.getFailureMessage());
    }

    @Test
    public void testEqualsMethods() throws Exception {
        //The 'not' part of the validation uses equals so test it properly here
        //TODO Any
        //TODO Not
        Assert.assertTrue(new InetAddressMatchInterfaceCriteria("127.0.0.1").equals(new InetAddressMatchInterfaceCriteria("127.0.0.1")));
        Assert.assertFalse(new InetAddressMatchInterfaceCriteria("127.0.0.1").equals(new InetAddressMatchInterfaceCriteria("127.0.0.2")));
        Assert.assertTrue(LinkLocalInterfaceCriteria.INSTANCE.equals(LinkLocalInterfaceCriteria.INSTANCE));
        Assert.assertTrue(LoopbackInterfaceCriteria.INSTANCE.equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertTrue(new NicInterfaceCriteria("en1").equals(new NicInterfaceCriteria("en1")));
        Assert.assertFalse(new NicInterfaceCriteria("en1").equals(new NicInterfaceCriteria("en2")));
        Assert.assertFalse(new NicMatchInterfaceCriteria(Pattern.compile(".")).equals(new NicMatchInterfaceCriteria(Pattern.compile("a"))));
        Assert.assertTrue(new NicMatchInterfaceCriteria(Pattern.compile(".")).equals(new NicMatchInterfaceCriteria(Pattern.compile("."))));
        Assert.assertFalse(new NicMatchInterfaceCriteria(Pattern.compile(".")).equals(new NicMatchInterfaceCriteria(Pattern.compile("a"))));
        Assert.assertTrue(PointToPointInterfaceCriteria.INSTANCE.equals(PointToPointInterfaceCriteria.INSTANCE));
        Assert.assertTrue(SiteLocalInterfaceCriteria.INSTANCE.equals(SiteLocalInterfaceCriteria.INSTANCE));
        Assert.assertTrue(new SubnetMatchInterfaceCriteria(new byte[] {1, 2}, 3).equals(new SubnetMatchInterfaceCriteria(new byte[] {1, 2}, 3)));
        Assert.assertFalse(new SubnetMatchInterfaceCriteria(new byte[] {1, 2}, 3).equals(new SubnetMatchInterfaceCriteria(new byte[] {2}, 3)));
        Assert.assertFalse(new SubnetMatchInterfaceCriteria(new byte[] {1, 2}, 3).equals(new SubnetMatchInterfaceCriteria(new byte[] {1, 2}, 4)));
        Assert.assertTrue(SupportsMulticastInterfaceCriteria.INSTANCE.equals(SupportsMulticastInterfaceCriteria.INSTANCE));
        Assert.assertTrue(UpInterfaceCriteria.INSTANCE.equals(UpInterfaceCriteria.INSTANCE));
        Assert.assertTrue(VirtualInterfaceCriteria.INSTANCE.equals(VirtualInterfaceCriteria.INSTANCE));
        Assert.assertTrue(new WildcardInetAddressInterfaceCriteria(Inet4Address.getLocalHost()).equals(new WildcardInetAddressInterfaceCriteria(Inet4Address.getLocalHost())));


        Assert.assertFalse(new InetAddressMatchInterfaceCriteria("127.0.0.1").equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(LinkLocalInterfaceCriteria.INSTANCE.equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(new NicInterfaceCriteria("en1").equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(new NicMatchInterfaceCriteria(Pattern.compile(".")).equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(PointToPointInterfaceCriteria.INSTANCE.equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(SiteLocalInterfaceCriteria.INSTANCE.equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(new SubnetMatchInterfaceCriteria(new byte[] {1, 2}, 3).equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(SupportsMulticastInterfaceCriteria.INSTANCE.equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(UpInterfaceCriteria.INSTANCE.equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(VirtualInterfaceCriteria.INSTANCE.equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(new WildcardInetAddressInterfaceCriteria(Inet4Address.getLocalHost()).equals(LoopbackInterfaceCriteria.INSTANCE));
        Assert.assertFalse(LoopbackInterfaceCriteria.INSTANCE.equals(UpInterfaceCriteria.INSTANCE));
    }

    @Test
    public void testSubnetMatchInterfaceCriteria() throws Exception {
        SubnetMatchInterfaceCriteria criteria;

        // ipv4
        criteria = new SubnetMatchInterfaceCriteria(new byte[] {10, 0, 0, 0}, 24);
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {10, 0, 0, 1}));
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {10, 0, 1, 1}));
        criteria = new SubnetMatchInterfaceCriteria(new byte[] {10, 0, 2, 0}, 23);
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {10, 0, 2, 1}));
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {10, 0, 1, 1}));
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {10, 0, 0, 1}));
        criteria = new SubnetMatchInterfaceCriteria(new byte[] {(byte)192, (byte)168, 20, 32}, 31);
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {(byte)192, (byte)168, 20, 31}));
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {(byte)192, (byte)168, 20, 32}));
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {(byte)192, (byte)168, 20, 33}));
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {(byte)192, (byte)168, 20, 34}));
        criteria = new SubnetMatchInterfaceCriteria(new byte[] {0, 0, 0, 0}, 0);
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {10, 0, 0, 1}));
        criteria = new SubnetMatchInterfaceCriteria(new byte[] {(byte)192, (byte)168, 20, 32}, 32);
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {(byte)192, (byte)168, 20, 32}));
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {(byte)192, (byte)168, 20, 31}));

        // ipv6
        criteria = new SubnetMatchInterfaceCriteria(new byte[] {0x20, 0x01, (byte)0xdb, 0x08, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, 64);
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {0x20, 0x01, (byte)0xdb, 0x08, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x23}));
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {0x20, 0x01, (byte)0xdb, 0x08, 0x0b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x23}));
        criteria = new SubnetMatchInterfaceCriteria(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xff, (byte)0xff, 0x00, 0x00, 0x00, 0x00}, 96);
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xff, (byte)0xff, 0x0a, 0x00, 0x00, 0x01}));
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xff, (byte)0xfe, 0x0a, 0x00, 0x00, 0x01}));
        criteria = new SubnetMatchInterfaceCriteria(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xff, (byte)0xff, 0x0a, 0x00, 0x02, 0x00}, 119);
        Assert.assertTrue(criteria.verifyAddressByMask(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xff, (byte)0xff, 0x0a, 0x00, 0x02, 0x01}));
        Assert.assertFalse(criteria.verifyAddressByMask(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xff, (byte)0xff, 0x0a, 0x00, 0x01, 0x01}));
    }

}
