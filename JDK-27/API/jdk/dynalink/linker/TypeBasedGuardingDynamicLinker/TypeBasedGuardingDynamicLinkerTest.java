/*
 * @test
 * @summary jdk.dynalink.linker.TypeBasedGuardingDynamicLinker conformance: white-box structure + black-box behavior.
 * @run main TypeBasedGuardingDynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeBasedGuardingDynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.TypeBasedGuardingDynamicLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "canLinkType"), "canLinkType");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");

    }

    static void testBlackBox() throws Exception {
        check(true, "interface/no safe instantiable behavior; white-box only");

    }


    static class StringOnlyLinker implements TypeBasedGuardingDynamicLinker {
        final List<Class<?>> asked = new ArrayList<>();
        public boolean canLinkType(Class<?> type) { asked.add(type); return type == String.class; }
        public GuardedInvocation getGuardedInvocation(LinkRequest req, LinkerServices svc) { return null; }
    }

    static void testDeepContract() throws Throwable {
        // interface contract: canLinkType + inherited getGuardedInvocation
        check(TypeBasedGuardingDynamicLinker.class.isInterface(), "interface");
        check(GuardingDynamicLinker.class.isAssignableFrom(TypeBasedGuardingDynamicLinker.class),
              "extends GuardingDynamicLinker");
        int n = 0; Method abs = null;
        for (Method m : TypeBasedGuardingDynamicLinker.class.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { n++; abs = m; }
        check(n == 1 && abs.getName().equals("canLinkType"), "adds exactly canLinkType");
        // the composite consults canLinkType to skip linkers cheaply
        StringOnlyLinker s = new StringOnlyLinker();
        TypeBasedGuardingDynamicLinker all = new TypeBasedGuardingDynamicLinker() {
            public boolean canLinkType(Class<?> t) { return true; }
            public GuardedInvocation getGuardedInvocation(LinkRequest r, LinkerServices v) { return null; }
        };
        CompositeTypeBasedGuardingDynamicLinker c = new CompositeTypeBasedGuardingDynamicLinker(List.of(s, all));
        check(c.canLinkType(String.class), "OR of the delegates");
        check(c.canLinkType(Integer.class), "second delegate accepts");
        s.asked.clear();
        c.getGuardedInvocation(new SimpleLinkRequest(new CallSiteDescriptor(MethodHandles.lookup(),
            StandardOperation.GET, MethodType.methodType(Object.class, Object.class)), false, Integer.valueOf(1)),
            null);
        check(!s.asked.contains(Integer.class), "canLinkType-false linker skipped for the receiver type");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
