/*
 * @test
 * @summary java.lang.invoke.SwitchPoint conformance: white-box + black-box behavior.
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

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
