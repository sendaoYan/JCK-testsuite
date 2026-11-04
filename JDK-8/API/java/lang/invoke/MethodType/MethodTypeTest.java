/*
 * @test
 * @summary java.lang.invoke.MethodType conformance: factory and accessors.
 * @run main MethodTypeTest
 */

import java.lang.invoke.MethodType;
import java.util.List;

public class MethodTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMethodType() {
        MethodType mt = MethodType.methodType(int.class, String.class, int.class);
        check(mt.returnType() == int.class, "returnType");
        check(mt.parameterCount() == 2, "parameterCount");
        check(mt.parameterType(0) == String.class, "parameterType 0");
        check(mt.parameterType(1) == int.class, "parameterType 1");
        List<Class<?>> list = mt.parameterList();
        check(list.size() == 2, "parameterList size");
        check(mt.parameterArray().length == 2, "parameterArray length");
        check(mt.changeReturnType(void.class).returnType() == void.class, "changeReturnType");
        check(mt.changeParameterType(0, Object.class).parameterType(0) == Object.class,
              "changeParameterType");
        check(mt.insertParameterTypes(0, boolean.class).parameterCount() == 3,
              "insertParameterTypes");
        check(mt.appendParameterTypes(long.class).parameterCount() == 3, "appendParameterTypes");
        check(mt.dropParameterTypes(0, 1).parameterCount() == 1, "dropParameterTypes");
        check(mt.erase().parameterType(0) == Object.class, "erase");
        check(mt.generic().parameterType(0) == Object.class, "generic");
        check(mt.wrap().parameterType(0) == String.class, "wrap");
        check(mt.unwrap().parameterType(0) == String.class, "unwrap");
        check(MethodType.methodType(int.class, int.class).hasPrimitives(), "hasPrimitives");
        check(!MethodType.methodType(Object.class).hasPrimitives(), "no primitives");
        MethodType fromDesc = MethodType.fromMethodDescriptorString("(Ljava/lang/String;)I", null);
        check(fromDesc.returnType() == int.class, "fromMethodDescriptorString return");
        check(fromDesc.toMethodDescriptorString().equals("(Ljava/lang/String;)I"),
              "toMethodDescriptorString");
    }

    public static void main(String[] args) {
        testMethodType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
