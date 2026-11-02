/*
 * @test
 * @summary javax.management.MBeanTrustPermission conformance: JMX API type verification.
 * @run main MBeanTrustPermissionTest
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

public class MBeanTrustPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanTrustPermission p = new MBeanTrustPermission("register");
        check(p.getName().equals("register"), "name");
        check(p.implies(new MBeanTrustPermission("register")), "implies");
        check(p.equals(new MBeanTrustPermission("register")), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
