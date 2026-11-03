/*
 * @test
 * @summary java.lang.invoke.WrongMethodTypeException conformance: white-box + black-box behavior.
 * @run main WrongMethodTypeExceptionTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

public class WrongMethodTypeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.WrongMethodTypeException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) ctors++;
        }
        check(ctors == 2, "two public constructors");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        check(new WrongMethodTypeException() instanceof RuntimeException, "RuntimeException");
        check(new WrongMethodTypeException() instanceof RuntimeException,
              "direct RuntimeException subclass");
        WrongMethodTypeException e0 = new WrongMethodTypeException();
        check(e0.getMessage() == null, "no-arg message null");
        WrongMethodTypeException e1 = new WrongMethodTypeException("type mismatch");
        check("type mismatch".equals(e1.getMessage()), "String ctor message");
        check(e1.toString().startsWith("java.lang.invoke.WrongMethodTypeException: "),
              "toString format");

        // real trigger 1: invokeExact with statically wrong parameter types
        MethodHandle id = MethodHandles.identity(int.class);
        try {
            int r = (int) id.invokeExact("not an int");
            check(r == 0, "unreachable");
            check(false, "invokeExact wrong arg must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "invokeExact wrong static type -> WrongMethodTypeException");
        }

        // real trigger 2: invokeWithArguments with wrong arity
        try {
            id.invokeWithArguments(1, 2, 3);
            check(false, "arity mismatch must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "arity -> WrongMethodTypeException");
        }

        // real trigger 3: asType requiring an impossible conversion
        MethodHandle len = MethodHandles.lookup().findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        try {
            len.asType(MethodType.methodType(int.class, int.class));
            check(false, "impossible conversion must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "asType -> WrongMethodTypeException");
        }

        // real trigger 4: dynamic invoker of a CallSite called at the wrong type
        java.lang.invoke.MutableCallSite mcs =
                new java.lang.invoke.MutableCallSite(MethodType.methodType(int.class));
        MethodHandle inv = mcs.dynamicInvoker();
        try {
            inv.invokeExact("wrong");
            check(false, "dynamic invoker wrong type must throw");
        } catch (WrongMethodTypeException e) {
            check(true, "dynamic invoker -> WrongMethodTypeException");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
