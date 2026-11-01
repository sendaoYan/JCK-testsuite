/*
 * @test
 * @summary java.lang.invoke.MethodHandle conformance: invoke/invokeExact/type.
 * @run main MethodHandleTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMethodHandle() throws Throwable {
        MethodHandle mh = MethodHandles.lookup().findVirtual(
                String.class, "length", MethodType.methodType(int.class));
        check(mh.type().returnType() == int.class, "type returnType");
        check(mh.type().parameterCount() == 1, "type parameterCount");
        int len = (int) mh.invoke("abc");
        check(len == 3, "invoke");
        int lenExact = (int) mh.invokeExact((String) "abcd");
        check(lenExact == 4, "invokeExact");
        MethodHandle bound = mh.bindTo("abcde");
        check((int) bound.invoke() == 5, "bindTo");
        check(!mh.isVarargsCollector(), "isVarargsCollector false");
    }

    public static void main(String[] args) throws Throwable {
        testMethodHandle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
