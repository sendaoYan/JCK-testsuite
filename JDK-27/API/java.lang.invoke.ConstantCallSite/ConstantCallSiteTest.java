/*
 * @test
 * @summary java.lang.invoke.ConstantCallSite conformance: white-box + black-box behavior.
 * @run main ConstantCallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ConstantCallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int three() { return 3; }
    static int four() { return 4; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.ConstantCallSite");
        check(c.getSuperclass() == CallSite.class, "extends CallSite");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 1, "one public constructor (MethodHandle)");
        java.lang.reflect.Method getT = c.getDeclaredMethod("getTarget");
        java.lang.reflect.Method setT = c.getDeclaredMethod("setTarget", MethodHandle.class);
        java.lang.reflect.Method dyn = c.getDeclaredMethod("dynamicInvoker");
        check(java.lang.reflect.Modifier.isFinal(getT.getModifiers()), "getTarget final");
        check(java.lang.reflect.Modifier.isFinal(setT.getModifiers()), "setTarget final");
        check(java.lang.reflect.Modifier.isFinal(dyn.getModifiers()), "dynamicInvoker final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle target = lookup.findStatic(ConstantCallSiteTest.class, "three",
                MethodType.methodType(int.class));
        ConstantCallSite site = new ConstantCallSite(target);

        // the target is fixed at construction
        check((int) site.getTarget().invokeExact() == 3, "target invocable");
        check(site.type() == MethodType.methodType(int.class), "type");
        check((int) site.dynamicInvoker().invokeExact() == 3, "dynamicInvoker");

        // setTarget is permanently rejected
        MethodHandle other = lookup.findStatic(ConstantCallSiteTest.class, "four",
                MethodType.methodType(int.class));
        try {
            site.setTarget(other);
            check(false, "setTarget must throw");
        } catch (UnsupportedOperationException e) {
            check(true, "setTarget UOE");
        }
        check((int) site.getTarget().invokeExact() == 3, "target unchanged after UOE");
        check(site.getTarget().type() == target.type(), "still the original target type");

        // a site whose target returns a reference type works identically
        ConstantCallSite refSite = new ConstantCallSite(
                lookup.findStatic(ConstantCallSiteTest.class, "str",
                        MethodType.methodType(String.class)));
        check("s".equals((String) refSite.getTarget().invokeExact()), "reference-type target");
        check(refSite.type() == MethodType.methodType(String.class), "reference type()");

        // constructor with null throws
        try {
            new ConstantCallSite(null);
            check(false, "null target rejected");
        } catch (NullPointerException expected) {
            check(true, "null target NPE");
        }
    }

    static String str() { return "s"; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
