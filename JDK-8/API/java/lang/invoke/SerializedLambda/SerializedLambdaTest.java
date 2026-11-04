/*
 * @test
 * @summary java.lang.invoke.SerializedLambda conformance: field accessors.
 * @run main SerializedLambdaTest
 */

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;

public class SerializedLambdaTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSerializedLambda() {
        Object[] args = {1, "two"};
        SerializedLambda sl = new SerializedLambda(
                SerializedLambdaTest.class, "java.lang.Runnable", "run", "()V",
                6, "impl/Class", "runImpl", "()V", "()V", args);
        check(SerializedLambdaTest.class.getName().equals(sl.getCapturingClass()),
              "getCapturingClass");
        check("java.lang.Runnable".equals(sl.getFunctionalInterfaceClass()),
              "getFunctionalInterfaceClass");
        check("run".equals(sl.getFunctionalInterfaceMethodName()),
              "getFunctionalInterfaceMethodName");
        check("()V".equals(sl.getFunctionalInterfaceMethodSignature()),
              "getFunctionalInterfaceMethodSignature");
        check("impl/Class".equals(sl.getImplClass()), "getImplClass");
        check("runImpl".equals(sl.getImplMethodName()), "getImplMethodName");
        check("()V".equals(sl.getImplMethodSignature()), "getImplMethodSignature");
        check(sl.getImplMethodKind() == 6, "getImplMethodKind");
        check("()V".equals(sl.getInstantiatedMethodType()), "getInstantiatedMethodType");
        check(sl.getCapturedArgCount() == 2, "getCapturedArgCount");
        check(sl.getCapturedArg(0).equals(1), "getCapturedArg 0");
        check(sl.getCapturedArg(1).equals("two"), "getCapturedArg 1");
        check(sl instanceof Serializable, "implements Serializable");
        check(sl.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testSerializedLambda();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
