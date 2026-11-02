/*
 * @test
 * @summary java.lang.runtime.ObjectMethods conformance: final class and the
 *          record ObjectMethods bootstrap entry point signature.
 * @run main ObjectMethodsTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.TypeDescriptor;
import java.lang.runtime.ObjectMethods;

public class ObjectMethodsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(!ObjectMethods.class.isInterface(), "ObjectMethods is a final class");

        java.lang.reflect.Method b = ObjectMethods.class.getMethod("bootstrap",
                MethodHandles.Lookup.class, String.class, TypeDescriptor.class,
                Class.class, String.class, MethodHandle[].class);
        check(b != null, "bootstrap(Lookup,String,TypeDescriptor,Class,String,MethodHandle[])");
        check(Object.class.equals(b.getReturnType()), "bootstrap returns Object");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
