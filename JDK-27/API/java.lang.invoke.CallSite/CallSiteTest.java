/*
 * @test
 * @summary java.lang.invoke.CallSite conformance: type/getTarget/dynamicInvoker.
 * @run main CallSiteTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CallSiteTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCallSite() throws Throwable {
        MethodHandle target = MethodHandles.constant(String.class, "value");
        CallSite cs = new ConstantCallSite(target);
        check(cs.type().returnType() == String.class, "type returnType");
        check(cs.getTarget() == target, "getTarget");
        MethodHandle invoker = cs.dynamicInvoker();
        check("value".equals((String) invoker.invoke()), "dynamicInvoker");
        check(cs.type().equals(MethodType.methodType(String.class)), "type equals");
    }

    public static void main(String[] args) throws Throwable {
        testCallSite();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
