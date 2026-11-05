/*
 * @test
 * @summary jdk.dynalink.linker.support.CompositeGuardingDynamicLinker conformance: white-box structure + black-box behavior.
 * @run main CompositeGuardingDynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class CompositeGuardingDynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.support.CompositeGuardingDynamicLinker");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(GuardingDynamicLinker.class.isAssignableFrom(c), "implements GuardingDynamicLinker");
        check(hasMethod(c, "getGuardedInvocation"), "getGuardedInvocation");

    }

    static void testBlackBox() throws Exception {
        CompositeGuardingDynamicLinker c = new CompositeGuardingDynamicLinker(Collections.emptyList());
        check(c != null, "constructed");
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleLinkRequest r = new SimpleLinkRequest(csd, false, "obj");
        LinkerServices svc = new DynamicLinkerFactory().createLinker().getLinkerServices();
        check(c.getGuardedInvocation(r, svc) == null, "empty composite returns null");

    }


    static void testDelegationOrder() throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        final List<String> calls = new ArrayList<>();
        // first linker returning a value short-circuits the chain
        GuardingDynamicLinker a = (req, svc) -> { calls.add("a"); return null; };
        GuardingDynamicLinker b = (req, svc) -> { calls.add("b"); return null; };
        CompositeGuardingDynamicLinker c = new CompositeGuardingDynamicLinker(List.of(a, b));
        LinkRequest req = new SimpleLinkRequest(new CallSiteDescriptor(lk, StandardOperation.GET,
                MethodType.methodType(Object.class, Object.class)), false, new Object());
        check(c.getGuardedInvocation(req, null) == null, "all-declining composite returns null");
        check(calls.equals(List.of("a", "b")), "delegates consulted in order: " + calls);
        // a winner stops the walk
        calls.clear();
        final MethodHandle mh = MethodHandles.dropArguments(
                MethodHandles.constant(Object.class, "WIN"), 0, Object.class);
        GuardingDynamicLinker winner = (rq, s) -> { calls.add("w"); return new GuardedInvocation(mh, (MethodHandle) null); };
        CompositeGuardingDynamicLinker c2 = new CompositeGuardingDynamicLinker(List.of(a, winner, b));
        GuardedInvocation gi = c2.getGuardedInvocation(req, null);
        check(gi != null && gi.getInvocation() == mh, "winning invocation returned");
        check(calls.equals(List.of("a", "w")), "walk stops at the winner: " + calls);
        // an empty composite declines everything
        CompositeGuardingDynamicLinker empty = new CompositeGuardingDynamicLinker(List.of());
        check(empty.getGuardedInvocation(req, null) == null, "empty composite declines");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDelegationOrder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
