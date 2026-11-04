/*
 * @test
 * @summary java.lang.invoke.VolatileCallSite conformance: white-box + black-box behavior.
 *          Deepened: JDK helpful NPE messages for null setTarget/constructor
 *          arguments, WMTE "should be of type" on type mismatch, placeholder
 *          invocation IllegalStateException ("uninitialized call site"),
 *          cached dynamicInvoker follows volatile target updates, getTarget
 *          identity round-trip, sibling relation to MutableCallSite,
 *          constructor parity between the two public constructors.
 * @run main VolatileCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VolatileCallSite;

public class VolatileCallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int seven() { return 7; }
    static int eight() { return 8; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.VolatileCallSite");
        check(c.getSuperclass() == CallSite.class, "extends CallSite");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 2, "two public constructors");
        c.getDeclaredConstructor(MethodType.class);
        c.getDeclaredConstructor(MethodHandle.class);
        java.lang.reflect.Method getT = c.getDeclaredMethod("getTarget");
        java.lang.reflect.Method dyn = c.getDeclaredMethod("dynamicInvoker");
        check(java.lang.reflect.Modifier.isFinal(getT.getModifiers()), "getTarget final");
        check(java.lang.reflect.Modifier.isFinal(dyn.getModifiers()), "dynamicInvoker final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // target-based constructor and mutable updates (volatile visibility)
        MethodHandle seven = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        VolatileCallSite site = new VolatileCallSite(seven);
        check(site.type() == MethodType.methodType(int.class), "type");
        check((int) site.getTarget().invokeExact() == 7, "initial target");
        check((int) site.dynamicInvoker().invokeExact() == 7, "dynamicInvoker initial");

        MethodHandle eight = lookup.findStatic(VolatileCallSiteTest.class, "eight",
                MethodType.methodType(int.class));
        site.setTarget(eight);
        check((int) site.getTarget().invokeExact() == 8, "volatile target replaced");
        check((int) site.dynamicInvoker().invokeExact() == 8, "dynamicInvoker after update");

        // type-based constructor
        VolatileCallSite typed = new VolatileCallSite(MethodType.methodType(long.class));
        check(typed.type() == MethodType.methodType(long.class), "type constructor");
        check(typed.getTarget().type() == MethodType.methodType(long.class),
              "placeholder has declared type");

        // wrong-type replacement rejected
        try {
            site.setTarget(lookup.findStatic(VolatileCallSiteTest.class, "str",
                    MethodType.methodType(String.class)));
            check(false, "wrong type rejected");
        } catch (java.lang.invoke.WrongMethodTypeException e) {
            check(true, "setTarget wrong type");
        }

        // null target constructor rejects
        try {
            new VolatileCallSite((MethodHandle) null);
            check(false, "null target rejected");
        } catch (NullPointerException expected) {
            check(true, "null target NPE");
        }

        // two sites with the same target stay independent
        VolatileCallSite a = new VolatileCallSite(seven);
        VolatileCallSite b = new VolatileCallSite(seven);
        b.setTarget(eight);
        check((int) a.getTarget().invokeExact() == 7 && (int) b.getTarget().invokeExact() == 8,
              "sites are independent");
    }

    static String str() { return "s"; }

    // ---- Part 3: deepened contract (2026-11-05) ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++; return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // JDK helpful NPE/WMTE messages.
    static void testNegativeMessages() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle seven = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        MethodHandle bad = lookup.findStatic(VolatileCallSiteTest.class, "str",
                MethodType.methodType(String.class));
        VolatileCallSite site = new VolatileCallSite(seven);
        checkThrowsMsg(NullPointerException.class, () -> site.setTarget(null),
                "\"newTarget\" is null", "setTarget(null) NPE message");
        checkThrowsMsg(java.lang.invoke.WrongMethodTypeException.class,
                () -> site.setTarget(bad), "should be of type",
                "setTarget wrong type WMTE message");
        check(site.getTarget() == seven, "site intact after failures");
        checkThrowsMsg(NullPointerException.class, () -> new VolatileCallSite((MethodHandle) null),
                "\"target\" is null", "(MethodHandle)null ctor NPE message");
        checkThrowsMsg(NullPointerException.class, () -> new VolatileCallSite((MethodType) null),
                "\"targetType\" is null", "(MethodType)null ctor NPE message");
    }

    // MethodType constructor placeholder: right type, refuses invocation with
    // IllegalStateException until a real target is installed.
    static void testPlaceholderSemantics() throws Throwable {
        VolatileCallSite site = new VolatileCallSite(MethodType.methodType(int.class));
        check(site.getTarget() == site.getTarget(), "placeholder stable until replaced");
        checkThrowsMsg(IllegalStateException.class,
                () -> { int v = (int) site.getTarget().invoke(); },
                "uninitialized call site", "placeholder invoke ISE");
        checkThrowsMsg(IllegalStateException.class,
                () -> { int v = (int) site.dynamicInvoker().invoke(); },
                "uninitialized call site", "placeholder dynamicInvoker ISE");
        site.setTarget(MethodHandles.constant(int.class, 42));
        check((int) site.dynamicInvoker().invokeExact() == 42, "usable after setTarget");
    }

    // Cached dynamicInvoker handles keep re-dispatching through the site
    // across volatile target updates.
    static void testCachedInvokerFollowsVolatileUpdates() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle seven = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        MethodHandle eight = lookup.findStatic(VolatileCallSiteTest.class, "eight",
                MethodType.methodType(int.class));
        VolatileCallSite site = new VolatileCallSite(seven);
        MethodHandle c1 = site.dynamicInvoker();
        MethodHandle c2 = site.dynamicInvoker();
        site.setTarget(eight);
        check((int) c1.invokeExact() == 8, "c1 follows update");
        check((int) c2.invokeExact() == 8, "c2 follows update");
        site.setTarget(seven);
        check((int) c1.invokeExact() == 7, "switch back visible");
        check((int) site.getTarget().invokeExact() == 7, "getTarget().invokeExact equal");
    }

    // getTarget identity semantics across installs.
    static void testGetTargetIdentityRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle seven = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        MethodHandle eight = lookup.findStatic(VolatileCallSiteTest.class, "eight",
                MethodType.methodType(int.class));
        VolatileCallSite site = new VolatileCallSite(seven);
        site.setTarget(eight);
        check(site.getTarget() == eight, "identity after install");
        site.setTarget(seven);
        check(site.getTarget() == seven, "identity restored");
        check(site.getTarget() == site.getTarget(), "repeatable read");
    }

    // VolatileCallSite and MutableCallSite are siblings under CallSite.
    static void testSiblingRelation() {
        check(!java.lang.invoke.MutableCallSite.class.isAssignableFrom(VolatileCallSite.class)
              && !VolatileCallSite.class.isAssignableFrom(
                      java.lang.invoke.MutableCallSite.class),
              "siblings, not parent/child");
        check(CallSite.class.isAssignableFrom(VolatileCallSite.class),
              "CallSite is the shared parent");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testNegativeMessages();
        testPlaceholderSemantics();
        testCachedInvokerFollowsVolatileUpdates();
        testGetTargetIdentityRoundTrip();
        testSiblingRelation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
