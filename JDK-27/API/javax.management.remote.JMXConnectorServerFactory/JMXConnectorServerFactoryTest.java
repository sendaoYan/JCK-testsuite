/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerFactory conformance: JMX API type verification.
 * @run main JMXConnectorServerFactoryTest
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

public class JMXConnectorServerFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES.equals("jmx.remote.protocol.provider.pkgs"), "pkgs const");
        check(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER.equals("jmx.remote.default.class.loader"), "cl const");
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, ManagementFactory.getPlatformMBeanServer());
        check(cs != null, "server non-null");
        check(!cs.isActive(), "not active");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
