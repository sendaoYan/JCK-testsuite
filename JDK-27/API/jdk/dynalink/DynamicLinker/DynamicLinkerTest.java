/*
 * @test
 * @summary jdk.dynalink.DynamicLinker conformance: white-box structure + black-box behavior.
 * @run main DynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class DynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.DynamicLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "link"), "link");
        check(hasMethod(c, "getLinkerServices"), "getLinkerServices");

    }

    static void testBlackBox() throws Exception {
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        DynamicLinker dl = f.createLinker();
        check(dl != null, "linker");
        check(dl.getLinkerServices() != null, "linker services");
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(csd);
        check(dl.link(cs) == cs, "link returns site");
        check(dl.getLinkerServices().canConvert(String.class, Object.class), "canConvert");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
