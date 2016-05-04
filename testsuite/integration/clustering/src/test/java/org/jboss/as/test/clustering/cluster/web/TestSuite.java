package org.jboss.as.test.clustering.cluster.web;

import org.jboss.as.test.clustering.cluster.web.authentication.BasicAuthenticationWebFailoverTestCase;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * https://issues.jboss.org/browse/JBEAP-4494
 *
 * @author Petr Kremensky pkremens@redhat.com on 04/05/2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        NonHaWebSessionPersistenceTestCase.class,
        BasicAuthenticationWebFailoverTestCase.class,
        DistributableTestCase.class,
})
public class TestSuite {
}
