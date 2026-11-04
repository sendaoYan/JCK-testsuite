/*
 * @test
 * @summary java.lang.invoke.CallSite conformance: type/getTarget/dynamicInvoker,
 *          abstract-base contract (via subclasses), setTarget negative matrix
 *          (NPE null target / WrongMethodTypeException type mismatch with message),
 *          dynamicInvoker polymorphic indirection (cached handles follow setTarget),
 *          placeholder IllegalStateException, subclass hierarchy (siblings),
 *          protected/package constructor evidence via reflection.
 * @run main CallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.VolatileCallSite;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class CallSiteTest {
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

    static int one() { return 1; }
    static int two() { return 2; }
    static String str() { return "s"; }

    static void testCallSite() throws Throwable {
        MethodHandle target = MethodHandles.constant(String.class, "value");
        CallSite cs = new ConstantCallSite(target);
        check(cs.type().returnType() == String.class, "type returnType");
        check(cs.getTarget() == target, "getTarget");
        MethodHandle invoker = cs.dynamicInvoker();
        check("value".equals((String) invoker.invoke()), "dynamicInvoker");
        check(cs.type().equals(MethodType.methodType(String.class)), "type equals");
    }

    // setTarget(null) -> NPE; setTarget(wrong type) -> WrongMethodTypeException
    // carrying "should be of type"; failed setTarget leaves the site untouched.
    static void testSetTargetNegativeMatrix() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mhOne = lookup.findStatic(CallSiteTest.class, "one",
                MethodType.methodType(int.class));
        MethodHandle mhStr = lookup.findStatic(CallSiteTest.class, "str",
                MethodType.methodType(String.class));
        MutableCallSite site = new MutableCallSite(mhOne);
        checkThrows(NullPointerException.class, () -> site.setTarget(null),
              "setTarget(null) NPE");
        checkThrowsMsg(WrongMethodTypeException.class, () -> site.setTarget(mhStr),
              "should be of type", "setTarget wrong type WMTE message");
        check(site.getTarget() == mhOne, "getTarget unchanged after failed setTarget");
        check((int) site.getTarget().invokeExact() == 1, "target still invokes old value");
    }

    // dynamicInvoker() shares the site type and each produced handle keeps
    // dispatching to the current target (polymorphic indirection).
    static void testDynamicInvokerContract() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mhOne = lookup.findStatic(CallSiteTest.class, "one",
                MethodType.methodType(int.class));
        MethodHandle mhTwo = lookup.findStatic(CallSiteTest.class, "two",
                MethodType.methodType(int.class));
        MutableCallSite site = new MutableCallSite(mhOne);
        check(site.dynamicInvoker().type() == site.type(),
              "dynamicInvoker type identical to site type");
        MethodHandle cached = site.dynamicInvoker();
        check((int) cached.invokeExact() == 1, "cached invoker sees branch one");
        site.setTarget(mhTwo);
        check((int) cached.invokeExact() == 2, "cached invoker follows setTarget");
        check((int) site.dynamicInvoker().invokeExact() == 2,
              "fresh invoker sees new branch too");
        site.setTarget(mhOne);
        check((int) cached.invokeExact() == 1, "switch back is immediately visible");
        // wrong-type invokeExact through dynamicInvoker -> WMTE
        MutableCallSite strSite = new MutableCallSite(
                lookup.findStatic(CallSiteTest.class, "str",
                        MethodType.methodType(String.class)));
        final MethodHandle dyn = strSite.dynamicInvoker();
        checkThrowsMsg(WrongMethodTypeException.class,
              () -> { Object o = dyn.invokeExact(); },
              "but found ()Object", "dynamicInvoker wrong-type invokeExact WMTE");
    }

    // A site built from a MethodType starts with a placeholder of exactly that
    // type; invoking it fails with IllegalStateException, not WMTE.
    static void testTypeConstructorPlaceholder() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MutableCallSite site = new MutableCallSite(MethodType.methodType(String.class, int.class));
        check(site.type() == MethodType.methodType(String.class, int.class),
              "type() is the exact constructed MethodType");
        check(site.dynamicInvoker().type() == site.type(), "dynamicInvoker type");
        check(site.getTarget().type() == MethodType.methodType(String.class, int.class),
              "placeholder target type matches");
        checkThrowsMsg(IllegalStateException.class,
              () -> { Object o = site.getTarget().invoke(1); },
              "uninitialized call site", "placeholder invocation ISE");
        site.setTarget(lookup.findStatic(CallSiteTest.class, "dec",
                MethodType.methodType(String.class, int.class)));
        check("d1".equals((String) site.dynamicInvoker().invokeExact(1)),
              "site usable after real target is installed");
    }

    static String dec(int v) { return "d" + v; }

    // Abstract base class: abstract members, no public constructor, subclasses.
    static void testAbstractContract() throws Exception {
        Class<?> c = CallSite.class;
        check(Modifier.isAbstract(c.getModifiers()), "CallSite is abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("getTarget").getModifiers())
              && Modifier.isAbstract(c.getDeclaredMethod("setTarget", MethodHandle.class).getModifiers())
              && Modifier.isAbstract(c.getDeclaredMethod("dynamicInvoker").getModifiers()),
              "getTarget/setTarget/dynamicInvoker abstract on the base class");
        check(!Modifier.isAbstract(c.getDeclaredMethod("type").getModifiers()),
              "type() is concrete on the base class");
        int publicCtors = 0;
        int declaredCtors = 0;
        for (Constructor<?> ct : c.getDeclaredConstructors()) {
            declaredCtors++;
            if (Modifier.isPublic(ct.getModifiers())) publicCtors++;
        }
        check(publicCtors == 0, "no public CallSite constructor");
        check(declaredCtors >= 1, "constructors exist but are not public");
        // ConstantCallSite exposes the protected (MethodType, MethodHandle)
        // constructor reserved for subclasses that bind the site itself.
        boolean hasProtectedHookCtor = false;
        for (Constructor<?> ct : ConstantCallSite.class.getDeclaredConstructors()) {
            if (Modifier.isProtected(ct.getModifiers())
                  && ct.getParameterTypes().length == 2
                  && ct.getParameterTypes()[0] == MethodType.class
                  && ct.getParameterTypes()[1] == MethodHandle.class) {
                hasProtectedHookCtor = true;
            }
        }
        check(hasProtectedHookCtor, "ConstantCallSite protected (MethodType,MethodHandle) ctor");
    }

    // The three shipped subclasses are siblings, all direct children of CallSite.
    static void testSubclassHierarchy() {
        check(ConstantCallSite.class.getSuperclass() == CallSite.class, "ConstantCallSite direct child");
        check(MutableCallSite.class.getSuperclass() == CallSite.class, "MutableCallSite direct child");
        check(VolatileCallSite.class.getSuperclass() == CallSite.class, "VolatileCallSite direct child");
        check(!MutableCallSite.class.isAssignableFrom(VolatileCallSite.class)
              && !VolatileCallSite.class.isAssignableFrom(MutableCallSite.class),
              "Mutable and Volatile are siblings, not parent/child");
        check(!ConstantCallSite.class.isAssignableFrom(MutableCallSite.class)
              && !MutableCallSite.class.isAssignableFrom(ConstantCallSite.class),
              "Constant and Mutable are siblings");
        check(CallSite.class.isAssignableFrom(ConstantCallSite.class), "CallSite parent of Constant");
    }

    // getTarget/setTarget core round-trip identity via the mutable carrier.
    static void testTargetRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle a = lookup.findStatic(CallSiteTest.class, "one",
                MethodType.methodType(int.class));
        MethodHandle b = lookup.findStatic(CallSiteTest.class, "two",
                MethodType.methodType(int.class));
        MutableCallSite site = new MutableCallSite(a);
        site.setTarget(b);
        check(site.getTarget() == b, "getTarget identity after setTarget");
        check(site.getTarget() == site.getTarget(), "getTarget repeatable identical");
        site.setTarget(a);
        check(site.getTarget() == a, "setTarget back restores identity");
    }

    public static void main(String[] args) throws Throwable {
        testCallSite();
        testSetTargetNegativeMatrix();
        testDynamicInvokerContract();
        testTypeConstructorPlaceholder();
        testAbstractContract();
        testSubclassHierarchy();
        testTargetRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
