/*
 * @test
 * @summary javax.management.MBeanPermission conformance: JMX API type verification.
 * @run main MBeanPermissionTest
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

public class MBeanPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        MBeanPermission p = new MBeanPermission("*", "getAttribute,setAttribute");
        check(p.getActions().contains("getAttribute"), "actions contains getAttribute");
        check(p.getActions().contains("setAttribute"), "actions contains setAttribute");
        check(p.implies(new MBeanPermission("*", "getAttribute")), "implies subset");
        MBeanPermission p2 = new MBeanPermission("a.b.C", "invoke", new ObjectName("d:type=x"), "invoke");
        check(p2.implies(new MBeanPermission("a.b.C", "invoke", new ObjectName("d:type=x"), "invoke")), "implies equal");
        check(p.equals(new MBeanPermission("*", "getAttribute,setAttribute")), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
