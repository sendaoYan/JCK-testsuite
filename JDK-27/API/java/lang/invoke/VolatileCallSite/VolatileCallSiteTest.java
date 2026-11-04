/*
 * @test
 * @summary java.lang.invoke.VolatileCallSite conformance: white-box + black-box behavior.
 * @run main VolatileCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VolatileCallSite;

public class VolatileCallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int seven() { return 7; }
    static int eight() { return 8; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.VolatileCallSite");
        check(c.getSuperclass() == CallSite.class, "extends CallSite");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 2, "two public constructors");
        c.getDeclaredConstructor(MethodType.class);
        c.getDeclaredConstructor(MethodHandle.class);
        java.lang.reflect.Method getT = c.getDeclaredMethod("getTarget");
        java.lang.reflect.Method dyn = c.getDeclaredMethod("dynamicInvoker");
        check(java.lang.reflect.Modifier.isFinal(getT.getModifiers()), "getTarget final");
        check(java.lang.reflect.Modifier.isFinal(dyn.getModifiers()), "dynamicInvoker final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // target-based constructor and mutable updates (volatile visibility)
        MethodHandle seven = lookup.findStatic(VolatileCallSiteTest.class, "seven",
                MethodType.methodType(int.class));
        VolatileCallSite site = new VolatileCallSite(seven);
        check(site.type() == MethodType.methodType(int.class), "type");
        check((int) site.getTarget().invokeExact() == 7, "initial target");
        check((int) site.dynamicInvoker().invokeExact() == 7, "dynamicInvoker initial");

        MethodHandle eight = lookup.findStatic(VolatileCallSiteTest.class, "eight",
                MethodType.methodType(int.class));
        site.setTarget(eight);
        check((int) site.getTarget().invokeExact() == 8, "volatile target replaced");
        check((int) site.dynamicInvoker().invokeExact() == 8, "dynamicInvoker after update");

        // type-based constructor
        VolatileCallSite typed = new VolatileCallSite(MethodType.methodType(long.class));
        check(typed.type() == MethodType.methodType(long.class), "type constructor");
        check(typed.getTarget().type() == MethodType.methodType(long.class),
              "placeholder has declared type");

        // wrong-type replacement rejected
        try {
            site.setTarget(lookup.findStatic(VolatileCallSiteTest.class, "str",
                    MethodType.methodType(String.class)));
            check(false, "wrong type rejected");
        } catch (java.lang.invoke.WrongMethodTypeException e) {
            check(true, "setTarget wrong type");
        }

        // null target constructor rejects
        try {
            new VolatileCallSite((MethodHandle) null);
            check(false, "null target rejected");
        } catch (NullPointerException expected) {
            check(true, "null target NPE");
        }

        // two sites with the same target stay independent
        VolatileCallSite a = new VolatileCallSite(seven);
        VolatileCallSite b = new VolatileCallSite(seven);
        b.setTarget(eight);
        check((int) a.getTarget().invokeExact() == 7 && (int) b.getTarget().invokeExact() == 8,
              "sites are independent");
    }

    static String str() { return "s"; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
