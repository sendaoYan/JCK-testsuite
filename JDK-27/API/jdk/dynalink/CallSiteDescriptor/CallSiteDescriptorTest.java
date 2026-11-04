/*
 * @test
 * @summary jdk.dynalink.CallSiteDescriptor conformance: white-box structure + black-box behavior.
 * @run main CallSiteDescriptorTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class CallSiteDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.CallSiteDescriptor");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(SecureLookupSupplier.class.isAssignableFrom(c), "extends SecureLookupSupplier");
        check(hasMethod(c, "getOperation"), "getOperation");
        check(hasMethod(c, "getMethodType"), "getMethodType");
        check(hasMethod(c, "changeOperation"), "changeOperation");
        check(hasMethod(c, "changeMethodType"), "changeMethodType");

    }

    static void testBlackBox() throws Exception {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(Object.class, Object.class);
        CallSiteDescriptor csd = new CallSiteDescriptor(lk, StandardOperation.GET, mt);
        check(csd.getOperation() == StandardOperation.GET, "operation");
        check(csd.getMethodType().equals(mt), "method type");
        check(csd.changeOperation(StandardOperation.SET).getOperation() == StandardOperation.SET, "changeOperation");
        check(csd.changeMethodType(mt).getMethodType().equals(mt), "changeMethodType");
        check(csd.hashCode() == csd.hashCode(), "hashCode stable");
        check(csd.equals(csd), "equals self");
        check(csd.toString() != null, "toString");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
