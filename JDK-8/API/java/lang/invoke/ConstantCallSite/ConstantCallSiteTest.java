/*
 * @test
 * @summary java.lang.invoke.ConstantCallSite conformance: target immutability,
 *          permanent-target semantics (getTarget/dynamicInvoker identity),
 *          setTarget always UOE regardless of argument (null/wrong type/same
 *          handle), null-target constructor NPE, and the protected
 *          (MethodType, MethodHandle) hook constructor: hook invoked exactly
 *          once with the site under construction, wrong-type/null/non-handle
 *          hook returns -> WMTE/NPE/CCE with message fragments.
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

    // hook machinery for the protected (MethodType, MethodHandle) constructor
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

    static MethodHandle hookHandle() throws Exception {
        return MethodHandles.lookup().findStatic(Hook.class, "make",
                MethodType.methodType(MethodHandle.class, Object.class));
    }

    static void testConstantCallSite() throws Throwable {
        MethodHandle target = MethodHandles.constant(String.class, "x");
        ConstantCallSite cs = new ConstantCallSite(target);
        check(cs instanceof CallSite, "is CallSite");
        check(cs.getTarget() == target, "getTarget");
        try {
            cs.setTarget(MethodHandles.constant(String.class, "y"));
            check(false, "setTarget should throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "setTarget throws UnsupportedOperationException");
        }
        check("x".equals((String) cs.dynamicInvoker().invoke()), "dynamicInvoker");
    }

    // The target is frozen at construction: repeated reads agree and the
    // dynamic invoker is the target itself.
    static void testPermanentTargetIdentity() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle target = lookup.findStatic(ConstantCallSiteTest.class, "value",
                MethodType.methodType(String.class));
        ConstantCallSite site = new ConstantCallSite(target);
        check(site.getTarget() == target, "getTarget is the constructor handle");
        check(site.getTarget() == site.getTarget(), "repeated getTarget identical");
        check(site.dynamicInvoker() == site.getTarget(),
              "dynamicInvoker is getTarget for a constant site");
        check(site.dynamicInvoker() == site.dynamicInvoker(),
              "repeated dynamicInvoker identical");
        check(site.type() == target.type(), "type equals target type");
        check(site.type().toString().equals("()String"), "type toString exact");
        check("v".equals((String) site.dynamicInvoker().invokeExact()),
              "dynamicInvoker invoke exact");
        check("v".equals((String) site.dynamicInvoker().invokeExact()),
              "dynamicInvoker invoke exact again");
    }

    static String value() { return "v"; }
    static int three() { return 3; }

    // setTarget rejects every argument with UOE: even null and even the very
    // same target handle; the exception carries no detail message.
    static void testSetTargetAlwaysRejects() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle target = lookup.findStatic(ConstantCallSiteTest.class, "value",
                MethodType.methodType(String.class));
        ConstantCallSite site = new ConstantCallSite(target);
        try {
            site.setTarget(null);
            check(false, "setTarget(null) must throw");
        } catch (UnsupportedOperationException e) {
            check(e.getMessage() == null, "setTarget(null) UOE has no message");
        }
        try {
            site.setTarget(target);
            check(false, "setTarget(same) must throw");
        } catch (UnsupportedOperationException e) {
            check(e.getMessage() == null, "setTarget(same) UOE has no message");
        }
        checkThrows(UnsupportedOperationException.class, () -> site.setTarget(
                lookup.findStatic(ConstantCallSiteTest.class, "three",
                        MethodType.methodType(int.class))),
              "setTarget(wrong type) UOE not WMTE");
        check(site.getTarget() == target, "target unchanged by failed setTarget calls");
        check("v".equals((String) site.dynamicInvoker().invokeExact()), "still runs original");
    }

    // null target at construction is rejected before anything is frozen.
    static void testNullConstructor() {
        checkThrows(NullPointerException.class, () -> new ConstantCallSite(null),
              "ConstantCallSite(null) NPE");
    }

    // The protected (MethodType, MethodHandle) constructor: the hook runs once,
    // receives the site under construction, and its result becomes the target.
    static void testProtectedHookConstructor() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        hookCalls = 0;
        hookReceiver = null;
        hookResult = lookup.findStatic(ConstantCallSiteTest.class, "value",
                MethodType.methodType(String.class));
        SelfBoundCS site = new SelfBoundCS(MethodType.methodType(String.class), hookHandle());
        check(hookCalls == 1, "hook invoked exactly once");
        check(hookReceiver == site, "hook received the site under construction");
        check(site.getTarget() == hookResult, "hook result is the permanent target");
        check(site.type() == MethodType.methodType(String.class),
              "type is the declared targetType");
        check("v".equals((String) site.dynamicInvoker().invokeExact()), "hook-built site runs");
        checkThrows(UnsupportedOperationException.class, () -> site.setTarget(hookResult),
              "hook-built site also immutable");
        // a second site re-invokes its own hook once
        hookCalls = 0;
        SelfBoundCS again = new SelfBoundCS(MethodType.methodType(String.class), hookHandle());
        check(hookCalls == 1, "each site invokes its hook once");
        check(again.getTarget() == hookResult, "second hook-built site same target");
    }

    // Negative hook results: wrong type -> WMTE, null -> NPE, non-MethodHandle
    // -> CCE, wrong-arity hook itself -> WMTE, null hook -> NPE.
    static void testProtectedHookNegatives() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        checkThrowsMsg(WrongMethodTypeException.class, () -> new SelfBoundCS(
                        MethodType.methodType(int.class),
                        lookup.findStatic(Hook.class, "makeBadType",
                                MethodType.methodType(MethodHandle.class, Object.class))),
                "should be of type", "hook wrong return type WMTE");
        checkThrows(NullPointerException.class, () -> new SelfBoundCS(
                        MethodType.methodType(int.class),
                        lookup.findStatic(Hook.class, "makeNull",
                                MethodType.methodType(MethodHandle.class, Object.class))),
                "hook null return NPE");
        checkThrowsMsg(ClassCastException.class, () -> new SelfBoundCS(
                        MethodType.methodType(String.class),
                        lookup.findStatic(Hook.class, "makeNotAHandle",
                                MethodType.methodType(Object.class, Object.class))),
                "cannot be cast to", "hook non-handle return CCE");
        checkThrowsMsg(WrongMethodTypeException.class, () -> new SelfBoundCS(
                        MethodType.methodType(String.class),
                        MethodHandles.constant(String.class, "x")),
                "cannot convert MethodHandle", "hook arity mismatch WMTE");
        checkThrows(NullPointerException.class,
                () -> new SelfBoundCS(MethodType.methodType(String.class), null),
                "null hook NPE");
    }

    public static void main(String[] args) throws Throwable {
        testConstantCallSite();
        testPermanentTargetIdentity();
        testSetTargetAlwaysRejects();
        testNullConstructor();
        testProtectedHookConstructor();
        testProtectedHookNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
