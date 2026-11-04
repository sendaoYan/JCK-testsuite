/*
 * @test
 * @summary jdk.dynalink.support.SimpleRelinkableCallSite conformance: white-box structure + black-box behavior.
 * @run main SimpleRelinkableCallSiteTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class SimpleRelinkableCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.support.SimpleRelinkableCallSite");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(AbstractRelinkableCallSite.class.isAssignableFrom(c), "extends AbstractRelinkableCallSite");
        check(hasMethod(c, "relink"), "relink");
        check(hasMethod(c, "resetAndRelink"), "resetAndRelink");

    }

    static void testBlackBox() throws Exception {
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(csd);
        check(cs instanceof RelinkableCallSite, "relinkable");
        check(cs.type().equals(MethodType.methodType(Object.class, Object.class)), "type");
        check(cs.getDescriptor() == csd, "descriptor");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
