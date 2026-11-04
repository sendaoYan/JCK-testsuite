/*
 * @test
 * @summary java.lang.invoke.VolatileCallSite conformance: volatile mutable target,
 *          full Mutable-parity semantics (branch switching visible to cached
 *          invokers, getTarget identity round-trip, both constructors, negative
 *          matrix NPE/WMTE with messages, placeholder IllegalStateException),
 *          sibling relation to MutableCallSite, structural white-box checks.
 * @run main VolatileCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.VolatileCallSite;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Modifier;

public class VolatileCallSiteTest {
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

    static int seven() { return 7; }
    static int eight() { return 8; }
    static String str() { return "s"; }

    static void testVolatileCallSite() throws Throwable {
        MethodHandle first = MethodHandles.constant(String.class, "a");
        VolatileCallSite cs = new VolatileCallSite(first);
        check(cs instanceof CallSite, "is CallSite");
        check(cs.getTarget() == first, "getTarget");
        MethodHandle second = MethodHandles.constant(String.class, "b");
        cs.setTarget(second);
        check(cs.getTarget() == second, "setTarget");
        check("b".equals((String) cs.dynamicInvoker().invoke()), "dynamicInvoker");
    }

    // Every setTarget is immediately visible to previously captured invokers.
    static void testBranchSwitching() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mh7 = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        MethodHandle mh8 = lookup.findStatic(VolatileCallSiteTest.class, "eight",
                MethodType.methodType(int.class));
        VolatileCallSite site = new VolatileCallSite(mh7);
        MethodHandle cached1 = site.dynamicInvoker();
        MethodHandle cached2 = site.dynamicInvoker();
        check((int) cached1.invokeExact() == 7, "initial branch seven");
        site.setTarget(mh8);
        check((int) cached1.invokeExact() == 8, "cached1 follows to eight");
        check((int) cached2.invokeExact() == 8, "cached2 follows to eight");
        site.setTarget(mh7);
        check((int) cached1.invokeExact() == 7, "switch back immediately visible");
        site.setTarget(mh8);
        check((int) site.dynamicInvoker().invokeExact() == 8, "fresh invoker sees eight");
        check((int) site.getTarget().invokeExact() == 8, "getTarget().invokeExact equivalent");
    }

    // getTarget identity semantics across installs.
    static void testGetTargetRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mh7 = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        MethodHandle mh8 = lookup.findStatic(VolatileCallSiteTest.class, "eight",
                MethodType.methodType(int.class));
        VolatileCallSite site = new VolatileCallSite(mh7);
        check(site.getTarget() == site.getTarget(), "getTarget repeatable");
        site.setTarget(mh8);
        check(site.getTarget() == mh8, "identity after setTarget");
        site.setTarget(mh7);
        check(site.getTarget() == mh7, "identity restored");
        check(site.type() == mh7.type(), "type fixed at construction");
    }

    // Negative matrix: null / wrong-type setTarget and null constructor args.
    static void testNegativeMatrix() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mh7 = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        MethodHandle mhStr = lookup.findStatic(VolatileCallSiteTest.class, "str",
                MethodType.methodType(String.class));
        VolatileCallSite site = new VolatileCallSite(mh7);
        checkThrows(NullPointerException.class, () -> site.setTarget(null),
              "setTarget(null) NPE");
        checkThrowsMsg(WrongMethodTypeException.class, () -> site.setTarget(mhStr),
              "should be of type", "setTarget type-mismatch WMTE message");
        check(site.getTarget() == mh7, "failed setTarget leaves site intact");
        check((int) site.dynamicInvoker().invokeExact() == 7, "still runs old target");
        checkThrows(NullPointerException.class,
              () -> new VolatileCallSite((MethodHandle) null), "(MethodHandle)null NPE");
        checkThrows(NullPointerException.class,
              () -> new VolatileCallSite((MethodType) null), "(MethodType)null NPE");
    }

    // MethodType constructor placeholder: exact type, ISE on invocation,
    // fully usable after setTarget.
    static void testPlaceholderSemantics() throws Throwable {
        VolatileCallSite site = new VolatileCallSite(MethodType.methodType(int.class));
        check(site.type() == MethodType.methodType(int.class), "type ctor exact");
        check(site.getTarget().type() == MethodType.methodType(int.class),
              "placeholder type matches");
        check(site.getTarget() == site.getTarget(), "placeholder stable until replaced");
        checkThrowsMsg(IllegalStateException.class,
              () -> { int v = (int) site.getTarget().invoke(); },
              "uninitialized call site", "placeholder invoke ISE");
        checkThrowsMsg(IllegalStateException.class,
              () -> { int v = (int) site.dynamicInvoker().invoke(); },
              "uninitialized call site", "placeholder dynamicInvoker ISE");
        site.setTarget(MethodHandles.constant(int.class, 99));
        check((int) site.dynamicInvoker().invokeExact() == 99, "usable after setTarget");
    }

    // Both constructors agree on type/dynamicInvoker semantics.
    static void testConstructorParity() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mh7 = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        VolatileCallSite fromHandle = new VolatileCallSite(mh7);
        VolatileCallSite fromType = new VolatileCallSite(MethodType.methodType(int.class));
        fromType.setTarget(mh7);
        check(fromHandle.type() == fromType.type(), "both ctors same type");
        check((int) fromHandle.dynamicInvoker().invokeExact() == 7, "handle-ctor site runs");
        check((int) fromType.dynamicInvoker().invokeExact() == 7, "type-ctor site runs");
        check(fromHandle.dynamicInvoker().type() == fromHandle.type(),
              "handle-ctor invoker type");
        check(fromType.dynamicInvoker().type() == fromType.type(),
              "type-ctor invoker type");
    }

    // VolatileCallSite and MutableCallSite are siblings under CallSite.
    static void testTypeRelation() {
        check(VolatileCallSite.class.getSuperclass() == CallSite.class,
              "extends CallSite directly");
        check(MutableCallSite.class.getSuperclass() == CallSite.class,
              "MutableCallSite also extends CallSite");
        check(!MutableCallSite.class.isAssignableFrom(VolatileCallSite.class),
              "VolatileCallSite is not a MutableCallSite");
        check(!VolatileCallSite.class.isAssignableFrom(MutableCallSite.class),
              "MutableCallSite is not a VolatileCallSite");
        check(CallSite.class.isAssignableFrom(VolatileCallSite.class),
              "CallSite parent of VolatileCallSite");
    }

    // Structure: two public ctors, final getTarget/dynamicInvoker.
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = VolatileCallSite.class;
        int publicCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) publicCtors++;
        }
        check(publicCtors == 2, "two public constructors");
        check(Modifier.isFinal(c.getDeclaredMethod("getTarget").getModifiers()),
              "getTarget final");
        check(Modifier.isFinal(c.getDeclaredMethod("dynamicInvoker").getModifiers()),
              "dynamicInvoker final");
        check(!Modifier.isFinal(c.getDeclaredMethod("setTarget", MethodHandle.class).getModifiers()),
              "setTarget overridable");
    }

    public static void main(String[] args) throws Throwable {
        testVolatileCallSite();
        testBranchSwitching();
        testGetTargetRoundTrip();
        testNegativeMatrix();
        testPlaceholderSemantics();
        testConstructorParity();
        testTypeRelation();
        testWhiteBoxStructure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
