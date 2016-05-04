package org.jboss.as.test.clustering.cluster.web;

import org.jboss.as.test.clustering.cluster.web.authentication.BasicAuthenticationWebFailoverTestCase;
import org.jboss.as.test.clustering.cluster.singleton.SingletonDeploymentJBossAllTestCase;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * https://issues.jboss.org/browse/JBEAP-4282
 *
 * @author Petr Kremensky pkremens@redhat.com on 04/05/2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DistributableTestCase.class,
        BasicAuthenticationWebFailoverTestCase.class,
        SingletonDeploymentJBossAllTestCase.class,
})
public class TestSuite {
}
