/*
 * @test
 * @summary java.lang.invoke.ConstantCallSite conformance: white-box + black-box behavior.
 *          Deepened: permanent-target identity (getTarget/dynamicInvoker the same
 *          object), setTarget rejects every argument with message-less UOE,
 *          null-target NPE with JDK helpful message, and the protected
 *          (MethodType, MethodHandle) hook constructor incl. hook-once/receiver
 *          identity and negative hook results (WMTE/NPE/CCE messages).
 * @run main ConstantCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

public class ConstantCallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int three() { return 3; }
    static int four() { return 4; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.ConstantCallSite");
        check(c.getSuperclass() == CallSite.class, "extends CallSite");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 1, "one public constructor (MethodHandle)");
        java.lang.reflect.Method getT = c.getDeclaredMethod("getTarget");
        java.lang.reflect.Method setT = c.getDeclaredMethod("setTarget", MethodHandle.class);
        java.lang.reflect.Method dyn = c.getDeclaredMethod("dynamicInvoker");
        check(java.lang.reflect.Modifier.isFinal(getT.getModifiers()), "getTarget final");
        check(java.lang.reflect.Modifier.isFinal(setT.getModifiers()), "setTarget final");
        check(java.lang.reflect.Modifier.isFinal(dyn.getModifiers()), "dynamicInvoker final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle target = lookup.findStatic(ConstantCallSiteTest.class, "three",
                MethodType.methodType(int.class));
        ConstantCallSite site = new ConstantCallSite(target);

        // the target is fixed at construction
        check((int) site.getTarget().invokeExact() == 3, "target invocable");
        check(site.type() == MethodType.methodType(int.class), "type");
        check((int) site.dynamicInvoker().invokeExact() == 3, "dynamicInvoker");

        // setTarget is permanently rejected
        MethodHandle other = lookup.findStatic(ConstantCallSiteTest.class, "four",
                MethodType.methodType(int.class));
        try {
            site.setTarget(other);
            check(false, "setTarget must throw");
        } catch (UnsupportedOperationException e) {
            check(true, "setTarget UOE");
        }
        check((int) site.getTarget().invokeExact() == 3, "target unchanged after UOE");
        check(site.getTarget().type() == target.type(), "still the original target type");

        // a site whose target returns a reference type works identically
        ConstantCallSite refSite = new ConstantCallSite(
                lookup.findStatic(ConstantCallSiteTest.class, "str",
                        MethodType.methodType(String.class)));
        check("s".equals((String) refSite.getTarget().invokeExact()), "reference-type target");
        check(refSite.type() == MethodType.methodType(String.class), "reference type()");

        // constructor with null throws
        try {
            new ConstantCallSite(null);
            check(false, "null target rejected");
        } catch (NullPointerException expected) {
            check(true, "null target NPE");
        }
    }

    static String str() { return "s"; }

    // ---- Part 3: deepened contract (2026-11-05) ----

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

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

    static int hookCalls;
    static Object hookReceiver;
    static MethodHandle hookResult;

    static class Hook {
        public static MethodHandle make(Object site) {
            hookCalls++;
            hookReceiver = site;
            return hookResult;
        }
        public static MethodHandle makeBadType(Object site) {
            return MethodHandles.constant(String.class, "bad");
        }
        public static MethodHandle makeNull(Object site) { return null; }
        public static Object makeNotAHandle(Object site) { return "not-a-handle"; }
    }

    static class SelfBoundCS extends ConstantCallSite {
        SelfBoundCS(MethodType mt, MethodHandle hook) throws Throwable { super(mt, hook); }
    }

    // The frozen target: repeated reads and the dynamic invoker agree on the
    // very same object, and every invocation returns the exact value.
    static void testPermanentTargetIdentity() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle target = lookup.findStatic(ConstantCallSiteTest.class, "str",
                MethodType.methodType(String.class));
        ConstantCallSite site = new ConstantCallSite(target);
        check(site.getTarget() == site.getTarget(), "repeated getTarget identical");
        check(site.dynamicInvoker() == site.getTarget(),
              "dynamicInvoker is getTarget itself");
        check(site.dynamicInvoker() == site.dynamicInvoker(),
              "repeated dynamicInvoker identical");
        check(site.type().toString().equals("()String"), "type toString exact");
        check("s".equals((String) site.dynamicInvoker().invokeExact()), "invoke exact 1");
        check("s".equals((String) site.dynamicInvoker().invokeExact()), "invoke exact 2");
    }

    // UOE for every setTarget argument, with no detail message; the wrong-type
    // case is UOE (not WMTE) because the argument is never even considered.
    static void testSetTargetAlwaysRejects() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle target = lookup.findStatic(ConstantCallSiteTest.class, "str",
                MethodType.methodType(String.class));
        ConstantCallSite site = new ConstantCallSite(target);
        try {
            site.setTarget(null);
            check(false, "setTarget(null) must throw");
        } catch (UnsupportedOperationException e) {
            check(e.getMessage() == null, "setTarget(null) UOE message-less");
        }
        try {
            site.setTarget(target);
            check(false, "setTarget(same) must throw");
        } catch (UnsupportedOperationException e) {
            check(e.getMessage() == null, "setTarget(same) UOE message-less");
        }
        checkThrows(UnsupportedOperationException.class, () -> site.setTarget(
                lookup.findStatic(ConstantCallSiteTest.class, "three",
                        MethodType.methodType(int.class))),
              "setTarget(wrong type) UOE not WMTE");
        check(site.getTarget() == target, "target never changes");
    }

    // JDK helpful NPE message names the null parameter.
    static void testNullConstructorMessage() {
        checkThrowsMsg(NullPointerException.class, () -> new ConstantCallSite(null),
              "\"target\" is null", "ConstantCallSite(null) NPE message");
    }

    // Protected hook constructor: hook runs once per site, receives the site
    // under construction, and its exact-type result is frozen as the target.
    static void testProtectedHookConstructor() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        hookCalls = 0;
        hookReceiver = null;
        hookResult = lookup.findStatic(ConstantCallSiteTest.class, "str",
                MethodType.methodType(String.class));
        MethodHandle hook = lookup.findStatic(Hook.class, "make",
                MethodType.methodType(MethodHandle.class, Object.class));
        SelfBoundCS site = new SelfBoundCS(MethodType.methodType(String.class), hook);
        check(hookCalls == 1, "hook invoked exactly once");
        check(hookReceiver == site, "hook received the site itself");
        check(site.getTarget() == hookResult, "hook result frozen as target");
        check(site.type() == MethodType.methodType(String.class), "declared targetType");
        check("s".equals((String) site.dynamicInvoker().invokeExact()), "hook site invokes");
        hookCalls = 0;
        SelfBoundCS again = new SelfBoundCS(MethodType.methodType(String.class), hook);
        check(hookCalls == 1, "second site re-invokes hook once");
        check(again.getTarget() == hookResult, "second site same frozen target");
    }

    // Negative hook results, exact exceptions and messages.
    static void testProtectedHookNegatives() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        checkThrowsMsg(WrongMethodTypeException.class, () -> new SelfBoundCS(
                        MethodType.methodType(int.class),
                        lookup.findStatic(Hook.class, "makeBadType",
                                MethodType.methodType(MethodHandle.class, Object.class))),
                "should be of type", "hook wrong return type WMTE");
        checkThrowsMsg(NullPointerException.class, () -> new SelfBoundCS(
                        MethodType.methodType(int.class),
                        lookup.findStatic(Hook.class, "makeNull",
                                MethodType.methodType(MethodHandle.class, Object.class))),
                "\"newTarget\" is null", "hook null return NPE message");
        checkThrowsMsg(ClassCastException.class, () -> new SelfBoundCS(
                        MethodType.methodType(String.class),
                        lookup.findStatic(Hook.class, "makeNotAHandle",
                                MethodType.methodType(Object.class, Object.class))),
                "cannot be cast to", "hook non-handle return CCE");
        checkThrowsMsg(WrongMethodTypeException.class, () -> new SelfBoundCS(
                        MethodType.methodType(String.class),
                        MethodHandles.constant(String.class, "x")),
                "cannot convert MethodHandle", "hook arity mismatch WMTE");
        checkThrowsMsg(NullPointerException.class,
                () -> new SelfBoundCS(MethodType.methodType(String.class), null),
                "\"createTargetHook\" is null", "null hook NPE message");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testPermanentTargetIdentity();
        testSetTargetAlwaysRejects();
        testNullConstructorMessage();
        testProtectedHookConstructor();
        testProtectedHookNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
