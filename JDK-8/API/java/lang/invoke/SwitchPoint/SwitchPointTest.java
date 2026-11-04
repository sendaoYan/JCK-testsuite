/*
 * @test
 * @summary java.lang.invoke.SwitchPoint conformance: invalidation and guard,
 *          multi-guard flip semantics (all pre-existing guards plus later ones
 *          route to fallback after invalidateAll), invalidateAll idempotence
 *          and cross-switch-point independence, guardWithTest negatives on a
 *          still-valid switch point (type mismatch IAE message / null NPE),
 *          invalidateAll null matrix, and the javadoc-permitted behavior that
 *          an invalidated switch point may ignore the target entirely.
 * @run main SwitchPointTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;

public class SwitchPointTest {
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

    static String str() { return "s"; }
    static int one() { return 1; }
    static String concat(String a, String b) { return a + b; }

    static void testSwitchPoint() throws Throwable {
        SwitchPoint sp = new SwitchPoint();
        check(!sp.hasBeenInvalidated(), "initially valid");
        MethodHandle target = MethodHandles.constant(String.class, "target");
        MethodHandle fallback = MethodHandles.constant(String.class, "fallback");
        MethodHandle guard = sp.guardWithTest(target, fallback);
        check("target".equals((String) guard.invoke()), "guard picks target while valid");
        SwitchPoint.invalidateAll(new SwitchPoint[] {sp});
        check(sp.hasBeenInvalidated(), "invalidated");
        check("fallback".equals((String) guard.invoke()), "guard picks fallback after invalidation");
    }

    // Two guards built before invalidation both flip; a guard built afterwards
    // starts on the fallback branch; the flip is stable across repeated calls.
    static void testMultiGuardFlip() throws Throwable {
        SwitchPoint sp = new SwitchPoint();
        MethodHandle t = MethodHandles.constant(String.class, "T");
        MethodHandle f = MethodHandles.constant(String.class, "F");
        MethodHandle g1 = sp.guardWithTest(t, f);
        MethodHandle g2 = sp.guardWithTest(t, f);
        check(g1 != g2, "each guardWithTest call yields a fresh handle");
        check(g1.type() == t.type() && g2.type() == t.type(), "guard type equals target type");
        check("T".equals((String) g1.invokeExact()) && "T".equals((String) g2.invokeExact()),
              "both guards take target while valid");
        SwitchPoint.invalidateAll(new SwitchPoint[]{sp});
        check(sp.hasBeenInvalidated(), "hasBeenInvalidated flipped");
        check("F".equals((String) g1.invokeExact()), "guard1 flipped to fallback");
        check("F".equals((String) g2.invokeExact()), "guard2 flipped to fallback");
        check("F".equals((String) sp.guardWithTest(t, f).invokeExact()),
              "guard created after invalidation starts on fallback");
        check("F".equals((String) g1.invokeExact()) && "F".equals((String) g1.invokeExact()),
              "flip is stable across repeated invocations");
    }

    // Invalidation is one-way and permanent; invalidating twice changes
    // nothing, and other switch points are unaffected.
    static void testIdempotenceAndIndependence() throws Throwable {
        SwitchPoint a = new SwitchPoint();
        SwitchPoint b = new SwitchPoint();
        MethodHandle t = MethodHandles.constant(String.class, "T");
        MethodHandle f = MethodHandles.constant(String.class, "F");
        MethodHandle ga = a.guardWithTest(t, f);
        MethodHandle gb = b.guardWithTest(t, f);
        SwitchPoint.invalidateAll(new SwitchPoint[]{a});
        SwitchPoint.invalidateAll(new SwitchPoint[]{a}); // second call is a no-op
        check(a.hasBeenInvalidated(), "still invalidated after second invalidateAll");
        check("F".equals((String) ga.invokeExact()), "guard still on fallback");
        check(!b.hasBeenInvalidated(), "other switch point untouched");
        check("T".equals((String) gb.invokeExact()), "other guard still on target");
        SwitchPoint.invalidateAll(new SwitchPoint[0]); // empty array is a no-op
        check(!b.hasBeenInvalidated(), "empty invalidateAll changes nothing");
        check("T".equals((String) gb.invokeExact()), "other guard unaffected by empty call");
    }

    // guardWithTest negatives on a still-valid switch point: types must match
    // exactly (IAE), both handles must be non-null (NPE); invalidateAll with a
    // null array or a null element is NPE.
    static void testNegatives() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle strHandle = lookup.findStatic(SwitchPointTest.class, "str",
                MethodType.methodType(String.class));
        MethodHandle intHandle = lookup.findStatic(SwitchPointTest.class, "one",
                MethodType.methodType(int.class));
        SwitchPoint sp = new SwitchPoint();
        checkThrowsMsg(IllegalArgumentException.class,
              () -> sp.guardWithTest(strHandle, intHandle),
              "target and fallback types must match",
              "guardWithTest type mismatch IAE message");
        checkThrows(NullPointerException.class, () -> sp.guardWithTest(null, strHandle),
              "guardWithTest(null, y) NPE");
        checkThrows(NullPointerException.class, () -> sp.guardWithTest(strHandle, null),
              "guardWithTest(x, null) NPE");
        checkThrows(NullPointerException.class, () -> SwitchPoint.invalidateAll(null),
              "invalidateAll(null) NPE");
        checkThrows(NullPointerException.class,
              () -> SwitchPoint.invalidateAll(new SwitchPoint[]{null}),
              "invalidateAll({null}) NPE");
        check(!sp.hasBeenInvalidated(), "failed guardWithTest does not invalidate");
    }

    // Javadoc: once invalidated, guardWithTest "may ignore T and return F";
    // the observed behavior is that the returned handle has the fallback type.
    static void testInvalidatedIgnoresTarget() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle strHandle = lookup.findStatic(SwitchPointTest.class, "str",
                MethodType.methodType(String.class));
        MethodHandle intHandle = lookup.findStatic(SwitchPointTest.class, "one",
                MethodType.methodType(int.class));
        SwitchPoint sp = new SwitchPoint();
        SwitchPoint.invalidateAll(new SwitchPoint[]{sp});
        MethodHandle guard = sp.guardWithTest(strHandle, intHandle);
        check(guard.type() == intHandle.type(),
              "invalidated switch point returns fallback-typed handle");
        check((int) guard.invokeExact() == 1, "and it computes the fallback value");
    }

    // Guards pass arguments through to the selected branch unchanged.
    static void testGuardArgumentPassThrough() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle cat = lookup.findStatic(SwitchPointTest.class, "concat",
                MethodType.methodType(String.class, String.class, String.class));
        MethodHandle reversed = MethodHandles.permuteArguments(cat,
                MethodType.methodType(String.class, String.class, String.class), 1, 0);
        SwitchPoint sp = new SwitchPoint();
        MethodHandle guard = sp.guardWithTest(cat, reversed);
        check("ab".equals((String) guard.invokeExact("a", "b")), "args in order while valid");
        SwitchPoint.invalidateAll(new SwitchPoint[]{sp});
        check("ba".equals((String) guard.invokeExact("a", "b")),
              "args still in order on the fallback branch");
    }

    // Structure: exactly one public constructor, static invalidateAll,
    // and the class stays subclassable (not final, not abstract).
    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = SwitchPoint.class;
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "subclassable (javadoc)");
        int publicCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) publicCtors++;
        }
        check(publicCtors == 1, "single public constructor");
        java.lang.reflect.Method invalidateAll =
              c.getDeclaredMethod("invalidateAll", SwitchPoint[].class);
        check(java.lang.reflect.Modifier.isStatic(invalidateAll.getModifiers()),
              "invalidateAll static");
        check(java.lang.reflect.Modifier.isPublic(invalidateAll.getModifiers()),
              "invalidateAll public");
    }

    public static void main(String[] args) throws Throwable {
        testSwitchPoint();
        testMultiGuardFlip();
        testIdempotenceAndIndependence();
        testNegatives();
        testInvalidatedIgnoresTarget();
        testGuardArgumentPassThrough();
        testWhiteBoxStructure();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
