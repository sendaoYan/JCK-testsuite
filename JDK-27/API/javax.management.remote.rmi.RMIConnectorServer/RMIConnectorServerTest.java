/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectorServer conformance: JMX API type verification.
 * @run main RMIConnectorServerTest
 */

import java.lang.reflect.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.Hashtable;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.security.*;
import java.rmi.*;
import java.rmi.server.*;
import javax.security.auth.Subject;
import javax.management.*;
import javax.management.loading.*;
import javax.management.modelmbean.*;
import javax.management.monitor.*;
import javax.management.openmbean.*;
import javax.management.relation.*;
import javax.management.remote.*;
import javax.management.remote.rmi.*;
import javax.management.timer.*;

public class RMIConnectorServerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        RMIConnectorServer cs = new RMIConnectorServer(url, null, ManagementFactory.getPlatformMBeanServer());
        check(cs.getMBeanServer() != null, "mbean server");
        check(!cs.isActive(), "not active");
        check(cs.getAttributes() != null, "attributes");
        check(cs.getConnectionIds().length == 0, "no connections");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
