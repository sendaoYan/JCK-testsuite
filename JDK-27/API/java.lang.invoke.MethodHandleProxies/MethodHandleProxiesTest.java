/*
 * @test
 * @summary java.lang.invoke.MethodHandleProxies conformance: white-box + black-box behavior.
 * @run main MethodHandleProxiesTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodHandleProxiesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public interface StringIntFunction {
        int apply(String s);
    }

    static volatile boolean ran;

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandleProxies");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        String[] names = {"asInterfaceInstance", "isWrapperInstance", "wrapperInstanceTarget",
                "wrapperInstanceType"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // Runnable over a void no-arg handle
        ran = false;
        MethodHandle setFlag = lookup.findStatic(MethodHandleProxiesTest.class, "mark",
                MethodType.methodType(void.class));
        Runnable r = MethodHandleProxies.asInterfaceInstance(Runnable.class, setFlag);
        check(r instanceof Runnable, "proxy implements the interface");
        r.run();
        check(ran, "run() dispatched to the handle");

        // function with argument and return value
        MethodHandle len = lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        StringIntFunction f = MethodHandleProxies.asInterfaceInstance(
                StringIntFunction.class, len);
        check(f.apply("method") == 6, "argument and return flow through");

        // wrapper instance introspection
        check(MethodHandleProxies.isWrapperInstance(r), "isWrapperInstance true");
        check(!MethodHandleProxies.isWrapperInstance("plain"), "isWrapperInstance false");
        check(MethodHandleProxies.wrapperInstanceTarget(r).type()
                      .toMethodDescriptorString().equals("()V"),
              "wrapperInstanceTarget type");
        check(MethodHandleProxies.wrapperInstanceType(r) == Runnable.class,
              "wrapperInstanceType is the interface");
        check((int) MethodHandleProxies.wrapperInstanceTarget(f)
                      .invokeExact((String) "xyz") == 3,
              "wrapperInstanceTarget still computes");

        // non-wrapper input to the introspectors throws
        try {
            MethodHandleProxies.wrapperInstanceTarget(new Object());
            check(false, "wrapperInstanceTarget(non-wrapper) must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "wrapperInstanceTarget IAE");
        }
        try {
            MethodHandleProxies.wrapperInstanceType(new Object());
            check(false, "wrapperInstanceType(non-wrapper) must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "wrapperInstanceType IAE");
        }

        // bad arguments: null interface, non-interface
        try {
            MethodHandleProxies.asInterfaceInstance(null, setFlag);
            check(false, "null interface rejected");
        } catch (NullPointerException expected) {
            check(true, "null interface NPE");
        }
        try {
            MethodHandleProxies.asInterfaceInstance(String.class, len);
            check(false, "non-interface rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "non-interface IAE");
        }
        // handle type not compatible with the single abstract method
        try {
            MethodHandleProxies.asInterfaceInstance(StringIntFunction.class, setFlag);
            check(false, "incompatible handle rejected");
        } catch (RuntimeException expected) {
            check(expected instanceof IllegalArgumentException
                  || expected instanceof WrongMethodTypeException,
                  "incompatible handle rejected with " + expected.getClass().getSimpleName());
        }
    }

    static void mark() { ran = true; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
