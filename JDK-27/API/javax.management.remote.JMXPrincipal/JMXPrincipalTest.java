/*
 * @test
 * @summary javax.management.remote.JMXPrincipal conformance: JMX API type verification.
 * @run main JMXPrincipalTest
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

public class JMXPrincipalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        JMXPrincipal p = new JMXPrincipal("user");
        check(p.getName().equals("user"), "name");
        check(p.equals(new JMXPrincipal("user")), "equals");
        check(p.hashCode() == new JMXPrincipal("user").hashCode(), "hashCode");
        check(p instanceof Principal, "is Principal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
