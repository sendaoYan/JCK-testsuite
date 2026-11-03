/*
 * @test
 * @summary jdk.dynalink.linker.support.CompositeTypeBasedGuardingDynamicLinker conformance: white-box structure + black-box behavior.
 * @run main CompositeTypeBasedGuardingDynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class CompositeTypeBasedGuardingDynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.CompositeTypeBasedGuardingDynamicLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(TypeBasedGuardingDynamicLinker.class.isAssignableFrom(c), "implements TypeBasedGuardingDynamicLinker");
        check(hasMethod(c, "canLinkType"), "canLinkType");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");

    }

    static void testBlackBox() throws Exception {
        CompositeTypeBasedGuardingDynamicLinker c = new CompositeTypeBasedGuardingDynamicLinker(Collections.emptyList());
        check(c != null, "constructed");
        check(!c.canLinkType(String.class), "cannot link");
        check(CompositeTypeBasedGuardingDynamicLinker.optimize(Collections.emptyList()) != null, "optimize");
        check(c.canLinkType(Object.class) == false, "cannot link Object");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
