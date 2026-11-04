/*
 * @test
 * @summary java.lang.invoke.CallSite conformance: white-box + black-box behavior.
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

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
