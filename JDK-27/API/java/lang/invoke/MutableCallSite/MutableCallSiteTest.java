/*
 * @test
 * @summary java.lang.invoke.MutableCallSite conformance: white-box + black-box behavior.
 *          Deepened: JDK helpful NPE messages for null target/ctor arguments,
 *          placeholder invocation IllegalStateException ("uninitialized call
 *          site"), interleaved setTarget/invokeExact alternation visible to
 *          cached invoker handles, getTarget identity round-trip, syncAll
 *          with null element NPE.
 * @run main MutableCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

public class MutableCallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int five() { return 5; }
    static int six() { return 6; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MutableCallSite");
        check(c.getSuperclass() == CallSite.class, "extends CallSite");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 2, "two public constructors");
        c.getDeclaredConstructor(MethodType.class);
        c.getDeclaredConstructor(MethodHandle.class);
        check(c.getDeclaredMethod("syncAll", MutableCallSite[].class) != null,
              "static syncAll");
        java.lang.reflect.Method getT = c.getDeclaredMethod("getTarget");
        java.lang.reflect.Method dyn = c.getDeclaredMethod("dynamicInvoker");
        check(java.lang.reflect.Modifier.isFinal(getT.getModifiers()), "getTarget final");
        check(java.lang.reflect.Modifier.isFinal(dyn.getModifiers()), "dynamicInvoker final");
        check(!java.lang.reflect.Modifier.isFinal(
                      c.getDeclaredMethod("setTarget", MethodHandle.class).getModifiers()),
              "setTarget overridable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // constructor from a target handle
        MethodHandle five = lookup.findStatic(MutableCallSiteTest.class, "five",
                MethodType.methodType(int.class));
        MutableCallSite site = new MutableCallSite(five);
        check(site.type() == MethodType.methodType(int.class), "type from target");
        check((int) site.getTarget().invokeExact() == 5, "initial target");

        // setTarget swaps the behavior
        MethodHandle six = lookup.findStatic(MutableCallSiteTest.class, "six",
                MethodType.methodType(int.class));
        site.setTarget(six);
        check((int) site.getTarget().invokeExact() == 6, "target replaced");
        check((int) site.dynamicInvoker().invokeExact() == 6, "dynamicInvoker sees update");

        // constructor from a type: target is a placeholder of that type
        MutableCallSite typed = new MutableCallSite(MethodType.methodType(String.class));
        check(typed.type() == MethodType.methodType(String.class), "type constructor");
        check(typed.getTarget().type() == MethodType.methodType(String.class),
              "placeholder target has the declared type");

        // type mismatch rejected with WrongMethodTypeException
        try {
            site.setTarget(lookup.findStatic(MutableCallSiteTest.class, "str",
                    MethodType.methodType(String.class)));
            check(false, "wrong type rejected");
        } catch (java.lang.invoke.WrongMethodTypeException e) {
            check(true, "setTarget wrong type");
        }

        // syncAll accepts arrays (including empty) without throwing
        MutableCallSite.syncAll(new MutableCallSite[0]);
        check(true, "syncAll(empty) ok");
        MutableCallSite.syncAll(new MutableCallSite[]{site, typed});
        check(true, "syncAll(sites) ok");
        try {
            MutableCallSite.syncAll(null);
            check(false, "null array rejected");
        } catch (NullPointerException expected) {
            check(true, "syncAll(null) NPE");
        }
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

    // JDK helpful messages name the exact null parameter.
    static void testNegativeMessages() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle one = lookup.findStatic(MutableCallSiteTest.class, "five",
                MethodType.methodType(int.class));
        MethodHandle bad = lookup.findStatic(MutableCallSiteTest.class, "str",
                MethodType.methodType(String.class));
        MutableCallSite site = new MutableCallSite(one);
        checkThrowsMsg(NullPointerException.class, () -> site.setTarget(null),
                "\"newTarget\" is null", "setTarget(null) NPE message");
        checkThrowsMsg(java.lang.invoke.WrongMethodTypeException.class,
                () -> site.setTarget(bad), "should be of type",
                "setTarget wrong type WMTE message");
        check(site.getTarget() == one, "site intact after failures");
        checkThrowsMsg(NullPointerException.class, () -> new MutableCallSite((MethodHandle) null),
                "\"target\" is null", "(MethodHandle)null ctor NPE message");
        checkThrowsMsg(NullPointerException.class, () -> new MutableCallSite((MethodType) null),
                "\"targetType\" is null", "(MethodType)null ctor NPE message");
    }

    // MethodType constructor placeholder: right type, refuses invocation with
    // IllegalStateException until a real target is installed.
    static void testPlaceholderSemantics() throws Throwable {
        MutableCallSite site = new MutableCallSite(MethodType.methodType(int.class));
        check(site.getTarget() == site.getTarget(), "placeholder stable until replaced");
        checkThrowsMsg(IllegalStateException.class,
                () -> { int v = (int) site.getTarget().invoke(); },
                "uninitialized call site", "placeholder invoke ISE");
        checkThrowsMsg(IllegalStateException.class,
                () -> { int v = (int) site.dynamicInvoker().invoke(); },
                "uninitialized call site", "placeholder dynamicInvoker ISE");
        site.setTarget(MethodHandles.constant(int.class, 11));
        check((int) site.dynamicInvoker().invokeExact() == 11, "usable after setTarget");
    }

    // Alternating setTarget/invokeExact with two cached invoker handles.
    static void testAlternatingBranchSwitch() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle five = lookup.findStatic(MutableCallSiteTest.class, "five",
                MethodType.methodType(int.class));
        MethodHandle six = lookup.findStatic(MutableCallSiteTest.class, "six",
                MethodType.methodType(int.class));
        MutableCallSite site = new MutableCallSite(five);
        MethodHandle c1 = site.dynamicInvoker();
        MethodHandle c2 = site.dynamicInvoker();
        check((int) c1.invokeExact() == 5, "start branch five");
        site.setTarget(six);
        check((int) c1.invokeExact() == 6, "c1 sees six");
        check((int) c2.invokeExact() == 6, "c2 sees six");
        site.setTarget(five);
        check((int) c1.invokeExact() == 5, "switch back visible");
        site.setTarget(six);
        check((int) site.getTarget().invokeExact() == 6, "getTarget().invokeExact equal");
        check((int) c2.invokeExact() == 6, "c2 still tracking");
    }

    // getTarget identity semantics across installs.
    static void testGetTargetIdentityRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle five = lookup.findStatic(MutableCallSiteTest.class, "five",
                MethodType.methodType(int.class));
        MethodHandle six = lookup.findStatic(MutableCallSiteTest.class, "six",
                MethodType.methodType(int.class));
        MutableCallSite site = new MutableCallSite(five);
        site.setTarget(six);
        check(site.getTarget() == six, "identity after install");
        site.setTarget(five);
        check(site.getTarget() == five, "identity restored");
        check(site.getTarget() == site.getTarget(), "repeatable read");
    }

    // syncAll with a null element is rejected even though the array is fine.
    static void testSyncAllNullElement() {
        try {
            MutableCallSite.syncAll(new MutableCallSite[]{null});
            check(false, "syncAll({null}) must throw");
        } catch (NullPointerException expected) {
            check(true, "syncAll({null}) NPE");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testNegativeMessages();
        testPlaceholderSemantics();
        testAlternatingBranchSwitch();
        testGetTargetIdentityRoundTrip();
        testSyncAllNullElement();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
