/*
 * @test
 * @summary JVMS 4.6: ACC_NATIVE (0x0100) is a valid method_info flag; the
 *          method has no Code attribute and no Java implementation, is
 *          reported as native by reflection, and invoking it before any
 *          native library providing the implementation has been loaded
 *          fails with UnsatisfiedLinkError.
 * @compile NativeMethodNoCode.jasm
 * @run main/othervm NativeMethodNoCodeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class NativeMethodNoCodeTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("NativeMethodNoCode");
        Method nat = c.getMethod("nat", int.class);

        check(Modifier.isNative(nat.getModifiers()),
                "ACC_NATIVE is visible through Modifier.isNative");
        check(nat.getReturnType() == int.class, "descriptor (I)I gives int return");
        check(nat.getParameterTypes().length == 1
                        && nat.getParameterTypes()[0] == int.class,
                "descriptor (I)I gives one int parameter");

        // No Code attribute means no Java body; with no library loaded the
        // JVM cannot bind an implementation (JVMS 5.6) and the invocation
        // fails with UnsatisfiedLinkError naming the method.
        try {
            nat.invoke(null, 1);
            check(false, "unbound native method must fail");
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            check(cause instanceof UnsatisfiedLinkError,
                    "invoke before load -> UnsatisfiedLinkError (got: " + cause + ")");
            check(String.valueOf(cause.getMessage()).contains("nat"),
                    "ULE names the method (got: " + cause.getMessage() + ")");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
