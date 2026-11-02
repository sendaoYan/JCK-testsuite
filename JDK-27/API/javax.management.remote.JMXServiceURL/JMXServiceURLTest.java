/*
 * @test
 * @summary javax.management.remote.JMXServiceURL conformance: JMX API type verification.
 * @run main JMXServiceURLTest
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

public class JMXServiceURLTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        JMXServiceURL u = new JMXServiceURL("service:jmx:rmi://localhost:9999/server");
        check(u.getProtocol().equals("rmi"), "protocol");
        check(u.getHost().equals("localhost"), "host");
        check(u.getPort() == 9999, "port");
        check(u.getURLPath().equals("/server"), "url path");
        check(u.toString().equals("service:jmx:rmi://localhost:9999/server"), "toString");
        check(u.equals(new JMXServiceURL("service:jmx:rmi://localhost:9999/server")), "equals");
        check(u.hashCode() == new JMXServiceURL("service:jmx:rmi://localhost:9999/server").hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
