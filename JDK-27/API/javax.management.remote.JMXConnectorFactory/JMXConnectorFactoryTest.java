/*
 * @test
 * @summary javax.management.remote.JMXConnectorFactory conformance: JMX API type verification.
 * @run main JMXConnectorFactoryTest
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

public class JMXConnectorFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES.equals("jmx.remote.protocol.provider.pkgs"), "pkgs const");
        check(JMXConnectorFactory.DEFAULT_CLASS_LOADER.equals("jmx.remote.default.class.loader"), "cl const");
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        JMXConnector c = JMXConnectorFactory.newJMXConnector(url, null);
        check(c != null, "connector non-null");
        c.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
