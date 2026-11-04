/*
 * @test
 * @summary java.lang.invoke.CallSite conformance: white-box + black-box behavior.
 *          Deepened: setTarget(null) NPE with JDK helpful message,
 *          setTarget type-mismatch WMTE message ("should be of type"),
 *          dynamicInvoker wrong-type WMTE ("handle's method type ... but found"),
 *          MethodType-constructor placeholder IllegalStateException
 *          ("uninitialized call site"), cached dynamicInvoker polymorphic
 *          indirection across setTarget, sibling subclass hierarchy, and the
 *          protected (MethodType, MethodHandle) ConstantCallSite constructor.
 * @run main CallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int one() { return 1; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.CallSite");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor (abstract)");
        String[] names = {"type", "getTarget", "setTarget", "dynamicInvoker"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
        check(Modifier.isAbstract(c.getDeclaredMethod("getTarget").getModifiers())
              && Modifier.isAbstract(c.getDeclaredMethod("setTarget", MethodHandle.class).getModifiers())
              && Modifier.isAbstract(c.getDeclaredMethod("dynamicInvoker").getModifiers()),
              "getTarget/setTarget/dynamicInvoker are abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("type").getModifiers()),
              "type() is concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle target = lookup.findStatic(CallSiteTest.class, "one",
                MethodType.methodType(int.class));
        CallSite site = new java.lang.invoke.MutableCallSite(target);

        // type() comes from the target
        check(site.type() == MethodType.methodType(int.class), "type from target");

        // getTarget/dynamicInvoker share the target
        check(site.getTarget() == target || site.getTarget().type().equals(target.type()),
              "getTarget");
        check((int) site.dynamicInvoker().invokeExact() == 1, "dynamicInvoker dispatches");

        // dynamicInvoker tracks target updates (polymorphic indirection)
        MethodHandle two = lookup.findStatic(CallSiteTest.class, "two",
                MethodType.methodType(int.class));
        site.setTarget(two);
        check((int) site.dynamicInvoker().invokeExact() == 2,
              "dynamicInvoker follows target change");
        check(site.type() == MethodType.methodType(int.class), "type unchanged");

        // type mismatch is rejected
        try {
            site.setTarget(lookup.findStatic(CallSiteTest.class, "str",
                    MethodType.methodType(String.class)));
            check(false, "wrong-type target rejected");
        } catch (java.lang.invoke.WrongMethodTypeException e) {
            check(true, "setTarget type mismatch");
        }

        // dynamicInvoker has the site's type
        check(site.dynamicInvoker().type() == site.type(), "dynamicInvoker type");

        // subclasses exposed by the JDK
        check(!java.lang.invoke.ConstantCallSite.class.isAnnotationPresent(Deprecated.class),
              "ConstantCallSite present");
        check(Class.forName("java.lang.invoke.VolatileCallSite").getSuperclass() == CallSite.class,
              "VolatileCallSite extends CallSite");
        check(Class.forName("java.lang.invoke.MutableCallSite").getSuperclass() == CallSite.class,
              "MutableCallSite extends CallSite");
    }

    static int two() { return 2; }
    static String str() { return "s"; }
    static String dec(int v) { return "d" + v; }

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

    // Exact exception messages for the negative setTarget matrix; the site
    // is untouched when the call fails.
    static void testSetTargetNegativeMessages() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle one = lookup.findStatic(CallSiteTest.class, "one",
                MethodType.methodType(int.class));
        java.lang.invoke.MutableCallSite site = new java.lang.invoke.MutableCallSite(one);
        MethodHandle bad = lookup.findStatic(CallSiteTest.class, "str",
                MethodType.methodType(String.class));
        checkThrowsMsg(NullPointerException.class, () -> site.setTarget(null),
                "\"newTarget\" is null", "setTarget(null) NPE message");
        checkThrowsMsg(java.lang.invoke.WrongMethodTypeException.class,
                () -> site.setTarget(bad), "should be of type",
                "setTarget wrong-type WMTE message");
        check(site.getTarget() == one, "site unchanged after failed setTarget calls");
        check((int) site.getTarget().invokeExact() == 1, "old target still runs");
    }

    // Wrong-type invokeExact through dynamicInvoker fails with WMTE naming the
    // site's own type first (JDK-27 wording).
    static void testDynamicInvokerWrongType() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        java.lang.invoke.MutableCallSite site = new java.lang.invoke.MutableCallSite(
                lookup.findStatic(CallSiteTest.class, "str",
                        MethodType.methodType(String.class)));
        MethodHandle dyn = site.dynamicInvoker();
        checkThrowsMsg(java.lang.invoke.WrongMethodTypeException.class,
                () -> { Object o = dyn.invokeExact(); },
                "but found ()Object", "dynamicInvoker wrong-type invokeExact WMTE");
    }

    // A MethodType-built site starts on a placeholder that reports the exact
    // type and throws IllegalStateException until a real target is installed.
    static void testTypeConstructorPlaceholder() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        java.lang.invoke.MutableCallSite site = new java.lang.invoke.MutableCallSite(
                MethodType.methodType(String.class, int.class));
        check(site.type() == MethodType.methodType(String.class, int.class),
              "type() identical to constructed MethodType");
        check(site.dynamicInvoker().type() == site.type(), "dynamicInvoker shares the type");
        check(site.getTarget().type() == MethodType.methodType(String.class, int.class),
              "placeholder target has the declared type");
        check(site.getTarget() == site.getTarget(), "placeholder is stable until replaced");
        checkThrowsMsg(IllegalStateException.class,
                () -> { Object o = site.getTarget().invoke(7); },
                "uninitialized call site", "placeholder invocation ISE");
        site.setTarget(lookup.findStatic(CallSiteTest.class, "dec",
                MethodType.methodType(String.class, int.class)));
        check("d7".equals((String) site.dynamicInvoker().invokeExact(7)),
              "site usable once a real target is installed");
    }

    // A dynamicInvoker handle captured earlier keeps re-dispatching through the
    // site: every setTarget is visible to previously-created invokers.
    static void testCachedInvokerFollowsTarget() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle one = lookup.findStatic(CallSiteTest.class, "one",
                MethodType.methodType(int.class));
        MethodHandle two = lookup.findStatic(CallSiteTest.class, "two",
                MethodType.methodType(int.class));
        java.lang.invoke.MutableCallSite site = new java.lang.invoke.MutableCallSite(one);
        MethodHandle cached = site.dynamicInvoker();
        check((int) cached.invokeExact() == 1, "cached invoker initial branch");
        site.setTarget(two);
        check((int) cached.invokeExact() == 2, "cached invoker follows update");
        site.setTarget(one);
        check((int) cached.invokeExact() == 1, "cached invoker follows switch back");
        check(site.dynamicInvoker().type() == site.type(), "type never drifts");
    }

    // MutableCallSite and VolatileCallSite are siblings under CallSite.
    static void testSiblingHierarchy() {
        check(!java.lang.invoke.MutableCallSite.class.isAssignableFrom(
                      java.lang.invoke.VolatileCallSite.class)
              && !java.lang.invoke.VolatileCallSite.class.isAssignableFrom(
                      java.lang.invoke.MutableCallSite.class),
              "Mutable and Volatile are siblings, not parent/child");
        check(java.lang.invoke.ConstantCallSite.class.getSuperclass() == CallSite.class,
              "ConstantCallSite is also a direct child");
    }

    // Reflection evidence for the protected (MethodType, MethodHandle)
    // constructor reserved for self-binding subclasses.
    static void testProtectedHookConstructor() throws Exception {
        boolean found = false;
        for (Constructor<?> ct : java.lang.invoke.ConstantCallSite.class.getDeclaredConstructors()) {
            if (Modifier.isProtected(ct.getModifiers())
                  && ct.getParameterTypes().length == 2
                  && ct.getParameterTypes()[0] == MethodType.class
                  && ct.getParameterTypes()[1] == MethodHandle.class) {
                found = true;
            }
        }
        check(found, "protected (MethodType, MethodHandle) ctor exists");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testSetTargetNegativeMessages();
        testDynamicInvokerWrongType();
        testTypeConstructorPlaceholder();
        testCachedInvokerFollowsTarget();
        testSiblingHierarchy();
        testProtectedHookConstructor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
