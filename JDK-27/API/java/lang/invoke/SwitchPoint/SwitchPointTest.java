/*
 * @test
 * @summary java.lang.invoke.SwitchPoint conformance: white-box + black-box behavior.
 *          Deepened: guardWithTest negatives on a still-valid switch point
 *          (type mismatch IAE message, null target/fallback NPE), invalidateAll
 *          null matrix, two pre-existing guards flipping together, the
 *          javadoc-permitted "invalidated switch point may ignore the target"
 *          behavior, and argument pass-through on both branches.
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

    static String yes() { return "yes"; }
    static String no() { return "no"; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.SwitchPoint");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 1, "single public constructor");
        for (String n : new String[]{"hasBeenInvalidated", "guardWithTest", "invalidateAll"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
        check(java.lang.reflect.Modifier.isStatic(
                      c.getDeclaredMethod("invalidateAll", SwitchPoint[].class).getModifiers()),
              "invalidateAll is static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle yes = lookup.findStatic(SwitchPointTest.class, "yes",
                MethodType.methodType(String.class));
        MethodHandle no = lookup.findStatic(SwitchPointTest.class, "no",
                MethodType.methodType(String.class));

        // fresh switch point routes to the true branch
        SwitchPoint sp = new SwitchPoint();
        check(!sp.hasBeenInvalidated(), "fresh is not invalidated");
        MethodHandle guard = sp.guardWithTest(yes, no);
        check("yes".equals((String) guard.invokeExact()), "true branch taken");

        // invalidation is permanent and flips the guard
        SwitchPoint.invalidateAll(new SwitchPoint[]{sp});
        check(sp.hasBeenInvalidated(), "invalidated after invalidateAll");
        check("no".equals((String) guard.invokeExact()),
              "previously created guard flips to false branch");
        check("no".equals((String) sp.guardWithTest(yes, no).invokeExact()),
              "new guards route to false branch");

        // invalidation is one-way
        SwitchPoint.invalidateAll(new SwitchPoint[]{sp});
        check(sp.hasBeenInvalidated(), "still invalidated");

        // independent switch points do not interfere
        SwitchPoint other = new SwitchPoint();
        check(!other.hasBeenInvalidated(), "other untouched");
        check("yes".equals((String) other.guardWithTest(yes, no).invokeExact()),
              "other still takes true branch");

        // invalidateAll with empty array is a no-op
        SwitchPoint.invalidateAll(new SwitchPoint[0]);
        check(!other.hasBeenInvalidated(), "empty invalidateAll changes nothing");

        // guards built from different handles are themselves distinct
        MethodHandle guard2 = other.guardWithTest(yes, no);
        check(guard2 != other.guardWithTest(yes, no), "guards are fresh handles");
        check(guard2.type() == MethodType.methodType(String.class), "guard type ()String");
        // invalidated guard still routes correctly after many calls
        check("no".equals((String) guard.invokeExact())
              && "no".equals((String) guard.invokeExact()),
              "guard is stable after invalidation");
    }

    static int seven() { return 7; }
    static String concat(String a, String b) { return a + b; }

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

    // guardWithTest negatives on a still-valid switch point.
    static void testNegatives() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle yes = lookup.findStatic(SwitchPointTest.class, "yes",
                MethodType.methodType(String.class));
        MethodHandle seven = lookup.findStatic(SwitchPointTest.class, "seven",
                MethodType.methodType(int.class));
        SwitchPoint sp = new SwitchPoint();
        checkThrowsMsg(IllegalArgumentException.class,
                () -> sp.guardWithTest(yes, seven),
                "target and fallback types must match",
                "guardWithTest type mismatch IAE message");
        checkThrows(NullPointerException.class, () -> sp.guardWithTest(null, yes),
                "guardWithTest(null, y) NPE");
        checkThrows(NullPointerException.class, () -> sp.guardWithTest(yes, null),
                "guardWithTest(x, null) NPE");
        checkThrowsMsg(NullPointerException.class, () -> SwitchPoint.invalidateAll(null),
                "\"switchPoints\" is null", "invalidateAll(null) NPE message");
        checkThrows(NullPointerException.class,
                () -> SwitchPoint.invalidateAll(new SwitchPoint[]{null}),
                "invalidateAll({null}) NPE");
        check(!sp.hasBeenInvalidated(), "failed guardWithTest does not invalidate");
    }

    // Two guards built while valid flip together; a later guard starts on the
    // fallback branch.
    static void testTwoGuardsFlipTogether() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle yes = lookup.findStatic(SwitchPointTest.class, "yes",
                MethodType.methodType(String.class));
        MethodHandle no = lookup.findStatic(SwitchPointTest.class, "no",
                MethodType.methodType(String.class));
        SwitchPoint sp = new SwitchPoint();
        MethodHandle g1 = sp.guardWithTest(yes, no);
        MethodHandle g2 = sp.guardWithTest(yes, no);
        check("yes".equals((String) g1.invokeExact()), "guard1 on target");
        check("yes".equals((String) g2.invokeExact()), "guard2 on target");
        SwitchPoint.invalidateAll(new SwitchPoint[]{sp});
        check("no".equals((String) g1.invokeExact()), "guard1 flipped");
        check("no".equals((String) g2.invokeExact()), "guard2 flipped");
        check("no".equals((String) sp.guardWithTest(yes, no).invokeExact()),
              "post-invalidation guard starts on fallback");
    }

    // Javadoc: after invalidation guardWithTest may ignore the target; the
    // observed result is a fallback-typed handle.
    static void testInvalidatedIgnoresTarget() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle yes = lookup.findStatic(SwitchPointTest.class, "yes",
                MethodType.methodType(String.class));
        MethodHandle seven = lookup.findStatic(SwitchPointTest.class, "seven",
                MethodType.methodType(int.class));
        SwitchPoint sp = new SwitchPoint();
        SwitchPoint.invalidateAll(new SwitchPoint[]{sp});
        MethodHandle guard = sp.guardWithTest(yes, seven);
        check(guard.type() == seven.type(), "fallback-typed handle returned");
        check((int) guard.invokeExact() == 7, "fallback value computed");
    }

    // Arguments flow to the selected branch unchanged.
    static void testGuardArgumentPassThrough() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle cat = lookup.findStatic(SwitchPointTest.class, "concat",
                MethodType.methodType(String.class, String.class, String.class));
        MethodHandle reversed = MethodHandles.permuteArguments(cat,
                MethodType.methodType(String.class, String.class, String.class), 1, 0);
        SwitchPoint sp = new SwitchPoint();
        MethodHandle guard = sp.guardWithTest(cat, reversed);
        check("ab".equals((String) guard.invokeExact("a", "b")), "ordered args while valid");
        SwitchPoint.invalidateAll(new SwitchPoint[]{sp});
        check("ba".equals((String) guard.invokeExact("a", "b")),
              "same call order on fallback branch");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testNegatives();
        testTwoGuardsFlipTogether();
        testInvalidatedIgnoresTarget();
        testGuardArgumentPassThrough();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
