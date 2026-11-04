/*
 * @test
 * @summary java.lang.invoke.MutableCallSite conformance: white-box + black-box behavior.
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

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
