/*
 * @test
 * @summary javax.management.ObjectInstance conformance: JMX API type verification.
 * @run main ObjectInstanceTest
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

public class ObjectInstanceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        ObjectInstance oi = new ObjectInstance("domain:type=x", "java.lang.String");
        check(oi.getClassName().equals("java.lang.String"), "className");
        check(oi.getObjectName().equals(new ObjectName("domain:type=x")), "objectName");
        ObjectInstance oi2 = new ObjectInstance(new ObjectName("domain:type=x"), "java.lang.String");
        check(oi.equals(oi2), "equals");
        check(oi.hashCode() == oi2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
