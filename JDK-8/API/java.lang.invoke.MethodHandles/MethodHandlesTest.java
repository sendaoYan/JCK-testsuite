/*
 * @test
 * @summary java.lang.invoke.MethodHandles conformance: lookups and factories.
 * @run main MethodHandlesTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandlesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMethodHandles() throws Throwable {
        check(MethodHandles.lookup() != null, "lookup");
        check(MethodHandles.publicLookup() != null, "publicLookup");

        MethodHandle id = MethodHandles.identity(int.class);
        check((int) id.invokeExact(42) == 42, "identity");

        MethodHandle cnst = MethodHandles.constant(String.class, "x");
        check("x".equals((String) cnst.invoke()), "constant");

        MethodHandle arrayGet = MethodHandles.arrayElementGetter(int[].class);
        int[] arr = {1, 2, 3};
        check((int) arrayGet.invoke(arr, 2) == 3, "arrayElementGetter");

        MethodHandle arrSet = MethodHandles.arrayElementSetter(int[].class);
        arrSet.invoke(arr, 1, 99);
        check(arr[1] == 99, "arrayElementSetter");

        MethodHandle permute = MethodHandles.permuteArguments(
                MethodHandles.identity(int.class), MethodType.methodType(int.class, int.class),
                0);
        check((int) permute.invokeExact(7) == 7, "permuteArguments");

        check((int) MethodHandles.constant(int.class, 0).invoke() == 0, "constant int");
    }

    public static void main(String[] args) throws Throwable {
        testMethodHandles();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
