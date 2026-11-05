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


    static void testTypeFilteredDelegation() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<String> asked = new ArrayList<>();
        TypeBasedGuardingDynamicLinker strings = new TypeBasedGuardingDynamicLinker() {
            public boolean canLinkType(Class<?> t) { asked.add(t.getSimpleName()); return t == String.class; }
            public GuardedInvocation getGuardedInvocation(LinkRequest r, LinkerServices s) { return null; }
        };
        TypeBasedGuardingDynamicLinker everything = new TypeBasedGuardingDynamicLinker() {
            public boolean canLinkType(Class<?> t) { return true; }
            public GuardedInvocation getGuardedInvocation(LinkRequest r, LinkerServices s) { return null; }
        };
        CompositeTypeBasedGuardingDynamicLinker c = new CompositeTypeBasedGuardingDynamicLinker(List.of(strings, everything));
        check(c.canLinkType(String.class) && c.canLinkType(Integer.class), "canLinkType is the OR of delegates");
        check(!new CompositeTypeBasedGuardingDynamicLinker(List.of(strings)).canLinkType(Integer.class),
              "single-delegate OR");
        // canLinkType-false delegates are skipped entirely for the receiver type
        asked.clear();
        LinkRequest req = new SimpleLinkRequest(new CallSiteDescriptor(lk, StandardOperation.GET,
                MethodType.methodType(Object.class, Object.class)), false, Integer.valueOf(1));
        c.getGuardedInvocation(req, null);
        check(!asked.contains("Integer"), "string-only linker never consulted for Integer: " + asked);
        // optimize() folds a mixed list into a single composite-friendly list
        List<GuardingDynamicLinker> opt = CompositeTypeBasedGuardingDynamicLinker.optimize(List.of(strings, everything));
        check(opt.size() == 1, "optimize folds into one composite: " + opt.size());
        // optimize of a lone type-based linker keeps it direct
        List<GuardingDynamicLinker> opt2 = CompositeTypeBasedGuardingDynamicLinker.optimize(List.of(strings));
        check(opt2.size() == 1 && opt2.get(0) == strings, "lone linker stays unwrapped");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testTypeFilteredDelegation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
