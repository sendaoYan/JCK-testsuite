/*
 * @test
 * @summary java.lang.invoke.MutableCallSite conformance: mutable target,
 *          branch switching with immediate visibility through previously
 *          captured dynamicInvoker handles, getTarget/setTarget identity
 *          round-trip, negative matrix (setTarget(null) NPE / type mismatch
 *          WMTE "should be of type" / null-constructor NPEs), MethodType-
 *          constructor placeholder IllegalStateException, syncAll behavior
 *          (empty/no-op/null-array NPE), and structural white-box checks.
 * @run main MutableCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MutableCallSiteTest {
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

    static void testMutableCallSite() throws Throwable {
        MethodHandle first = MethodHandles.constant(String.class, "a");
        MutableCallSite cs = new MutableCallSite(first);
        check(cs instanceof CallSite, "is CallSite");
        check(cs.getTarget() == first, "getTarget");
        MethodHandle second = MethodHandles.constant(String.class, "b");
        cs.setTarget(second);
        check(cs.getTarget() == second, "setTarget");
        check("b".equals((String) cs.dynamicInvoker().invoke()), "dynamicInvoker");
    }

    // Interleaved setTarget/invokeExact: every switch is immediately visible
    // through both fresh and previously captured invoker handles.
    static void testBranchSwitching() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mhOne = lookup.findStatic(MutableCallSiteTest.class, "one",
                MethodType.methodType(int.class));
        MethodHandle mhTwo = lookup.findStatic(MutableCallSiteTest.class, "two",
                MethodType.methodType(int.class));
        MutableCallSite site = new MutableCallSite(mhOne);
        MethodHandle cached1 = site.dynamicInvoker();
        MethodHandle cached2 = site.dynamicInvoker();
        check((int) cached1.invokeExact() == 1, "branch one first");
        site.setTarget(mhTwo);
        check((int) cached1.invokeExact() == 2, "cached1 follows to branch two");
        check((int) cached2.invokeExact() == 2, "cached2 follows to branch two");
        check((int) site.dynamicInvoker().invokeExact() == 2, "fresh invoker branch two");
        site.setTarget(mhOne);
        check((int) cached1.invokeExact() == 1, "switch back to one visible");
        site.setTarget(mhTwo);
        check((int) cached2.invokeExact() == 2, "second flip visible");
        // getTarget().invoke is equivalent to dynamicInvoker().invoke
        check((int) site.getTarget().invokeExact() == 2, "getTarget().invokeExact equivalent");
    }

    // getTarget returns exactly the handle installed by setTarget.
    static void testGetTargetRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle a = lookup.findStatic(MutableCallSiteTest.class, "one",
                MethodType.methodType(int.class));
        MethodHandle b = lookup.findStatic(MutableCallSiteTest.class, "two",
                MethodType.methodType(int.class));
        MutableCallSite site = new MutableCallSite(a);
        check(site.getTarget() == site.getTarget(), "getTarget repeatable");
        site.setTarget(b);
        check(site.getTarget() == b, "identity after setTarget");
        site.setTarget(a);
        check(site.getTarget() == a, "identity restored");
        check(site.type() == a.type(), "type fixed by construction");
    }

    // Negative matrix with exact exception types and message fragments.
    static void testNegativeMatrix() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mhOne = lookup.findStatic(MutableCallSiteTest.class, "one",
                MethodType.methodType(int.class));
        MethodHandle mhStr = lookup.findStatic(MutableCallSiteTest.class, "str",
                MethodType.methodType(String.class));
        MutableCallSite site = new MutableCallSite(mhOne);
        checkThrows(NullPointerException.class, () -> site.setTarget(null),
              "setTarget(null) NPE");
        checkThrowsMsg(WrongMethodTypeException.class, () -> site.setTarget(mhStr),
              "should be of type", "setTarget type-mismatch WMTE message");
        check(site.getTarget() == mhOne, "failed setTarget leaves site intact");
        checkThrows(NullPointerException.class,
              () -> new MutableCallSite((MethodHandle) null), "(MethodHandle)null NPE");
        checkThrows(NullPointerException.class,
              () -> new MutableCallSite((MethodType) null), "(MethodType)null NPE");
    }

    // MethodType constructor: placeholder target of exactly that type which
    // refuses invocation with IllegalStateException until setTarget is called.
    static void testPlaceholderSemantics() throws Throwable {
        MutableCallSite site = new MutableCallSite(MethodType.methodType(String.class));
        check(site.type() == MethodType.methodType(String.class), "type ctor type exact");
        check(site.getTarget().type() == MethodType.methodType(String.class),
              "placeholder type matches");
        check(site.getTarget() == site.getTarget(), "placeholder stable until replaced");
        checkThrowsMsg(IllegalStateException.class,
              () -> { Object o = site.getTarget().invoke(); },
              "uninitialized call site", "placeholder invoke ISE");
        checkThrowsMsg(IllegalStateException.class,
              () -> { Object o = site.dynamicInvoker().invoke(); },
              "uninitialized call site", "placeholder dynamicInvoker ISE");
        site.setTarget(MethodHandles.constant(String.class, "z"));
        check("z".equals((String) site.dynamicInvoker().invokeExact()),
              "usable after setTarget");
    }

    // syncAll: empty array no-op, real array keeps sites working, null array
    // and null element are both NPE.
    static void testSyncAll() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mhOne = lookup.findStatic(MutableCallSiteTest.class, "one",
                MethodType.methodType(int.class));
        MutableCallSite site = new MutableCallSite(mhOne);
        MutableCallSite.syncAll(new MutableCallSite[0]);
        check(true, "syncAll(empty) accepted");
        MutableCallSite.syncAll(new MutableCallSite[]{site});
        check((int) site.dynamicInvoker().invokeExact() == 1, "site still runs after syncAll");
        checkThrows(NullPointerException.class,
              () -> MutableCallSite.syncAll(null), "syncAll(null) NPE");
        checkThrows(NullPointerException.class,
              () -> MutableCallSite.syncAll(new MutableCallSite[]{null}),
              "syncAll({null}) NPE");
    }

    // Structure: two public constructors, final getTarget/dynamicInvoker,
    // overridable setTarget, static syncAll.
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = MutableCallSite.class;
        check(c.getSuperclass() == CallSite.class, "extends CallSite");
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
        Method syncAll = c.getDeclaredMethod("syncAll", MutableCallSite[].class);
        check(Modifier.isStatic(syncAll.getModifiers()), "syncAll static");
        check(Modifier.isPublic(syncAll.getModifiers()), "syncAll public");
    }

    public static void main(String[] args) throws Throwable {
        testMutableCallSite();
        testBranchSwitching();
        testGetTargetRoundTrip();
        testNegativeMatrix();
        testPlaceholderSemantics();
        testSyncAll();
        testWhiteBoxStructure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
