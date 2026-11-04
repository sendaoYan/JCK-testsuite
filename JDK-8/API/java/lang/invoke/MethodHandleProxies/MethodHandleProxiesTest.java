/*
 * @test
 * @summary java.lang.invoke.MethodHandleProxies conformance: interface wrapper.
 * @run main MethodHandleProxiesTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleProxiesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public interface IntSupplier {
        int get();
    }

    static void testMethodHandleProxies() throws Throwable {
        MethodHandle target = MethodHandles.constant(int.class, 42);
        IntSupplier supplier = MethodHandleProxies.asInterfaceInstance(IntSupplier.class, target);
        check(supplier.get() == 42, "asInterfaceInstance invoke");
        check(MethodHandleProxies.isWrapperInstance(supplier), "isWrapperInstance");
        MethodHandle back = MethodHandleProxies.wrapperInstanceTarget(supplier);
        check((int) back.invoke() == 42, "wrapperInstanceTarget");
        check(MethodHandleProxies.wrapperInstanceType(supplier) == IntSupplier.class,
              "wrapperInstanceType");
    }

    public static void main(String[] args) throws Throwable {
        testMethodHandleProxies();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
