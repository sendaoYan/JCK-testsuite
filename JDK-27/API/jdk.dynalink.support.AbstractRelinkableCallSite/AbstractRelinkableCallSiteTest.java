/*
 * @test
 * @summary jdk.dynalink.support.AbstractRelinkableCallSite conformance: white-box structure + black-box behavior.
 * @run main AbstractRelinkableCallSiteTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class AbstractRelinkableCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.support.AbstractRelinkableCallSite");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(RelinkableCallSite.class.isAssignableFrom(c), "implements RelinkableCallSite");
        check(MutableCallSite.class.isAssignableFrom(c), "extends MutableCallSite");
        check(hasMethod(c, "getDescriptor"), "getDescriptor");
        check(hasMethod(c, "initialize"), "initialize");

    }

    static void testBlackBox() throws Exception {
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(csd);
        AbstractRelinkableCallSite abs = cs;
        check(abs.getDescriptor().getOperation() == StandardOperation.GET, "getDescriptor");
        abs.initialize(MethodHandles.identity(Object.class));
        check(true, "initialize callable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
