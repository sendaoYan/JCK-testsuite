/*
 * @test
 * @summary javax.management.Descriptor conformance: JMX API type verification.
 * @run main DescriptorTest
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

public class DescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.Descriptor");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends java.io.Serializable");
        check(java.lang.Cloneable.class.isAssignableFrom(c), "extends java.lang.Cloneable");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "getFieldValue"), "has getFieldValue");
        check(has(mm, "setField"), "has setField");
        check(has(mm, "getFields"), "has getFields");
        check(has(mm, "getFieldNames"), "has getFieldNames");
        check(has(mm, "getFieldValues"), "has getFieldValues");
        check(has(mm, "removeField"), "has removeField");
        check(has(mm, "setFields"), "has setFields");
        check(has(mm, "clone"), "has clone");
        check(has(mm, "isValid"), "has isValid");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
