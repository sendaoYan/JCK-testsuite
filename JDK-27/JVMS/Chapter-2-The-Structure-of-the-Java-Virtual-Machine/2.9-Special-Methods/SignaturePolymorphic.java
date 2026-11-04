/*
 * @test
 * @summary JVMS 2.9.3: MethodHandle.invokeExact and invoke are signature
 *          polymorphic: invokeExact succeeds only when the call site type
 *          matches the method handle's type exactly and otherwise throws
 *          WrongMethodTypeException.
 * @run main SignaturePolymorphic
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

public class SignaturePolymorphic {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int add(int a, int b) { return a + b; }

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle add = lookup.findStatic(SignaturePolymorphic.class, "add",
                MethodType.methodType(int.class, int.class, int.class));

        // Exact match: call site type (int,int)int equals the handle type.
        check(((int) add.invokeExact(3, 4)) == 7,
                "invokeExact with an exact signature returns the result");

        // Mismatched return type: (int,int)long vs (int,int)int must fail.
        boolean threw = false;
        try {
            long r = (long) add.invokeExact(3, 4);
        } catch (WrongMethodTypeException e) {
            threw = true;
        }
        check(threw, "invokeExact with a mismatched type throws WrongMethodTypeException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
