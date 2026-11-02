/*
 * @test
 * @summary java.lang.constant.MethodTypeDesc conformance: factories, accessors,
 *          transform methods, and resolution to MethodType.
 * @run main MethodTypeDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodTypeDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(MethodTypeDesc.class.isInterface(), "MethodTypeDesc is an interface");

        MethodTypeDesc mt = MethodTypeDesc.of(
                ClassDesc.ofDescriptor("I"), ClassDesc.of("java.lang.String"));
        check("(Ljava/lang/String;)I".equals(mt.descriptorString()), "descriptorString");
        check("I".equals(mt.returnType().descriptorString()), "returnType");
        check(mt.parameterCount() == 1, "parameterCount");
        check("Ljava/lang/String;".equals(mt.parameterType(0).descriptorString()),
              "parameterType(0)");
        check(mt.parameterList().size() == 1, "parameterList size");
        check(mt.parameterArray().length == 1, "parameterArray length");

        MethodTypeDesc two = MethodTypeDesc.ofDescriptor("(IJ)Ljava/lang/String;");
        check(two.parameterCount() == 2 &&
              "Ljava/lang/String;".equals(two.returnType().descriptorString()),
              "ofDescriptor parses");

        check("(Ljava/lang/String;)J".equals(
                mt.changeReturnType(ClassDesc.ofDescriptor("J")).descriptorString()),
              "changeReturnType");
        check("(ILjava/lang/String;)I".equals(
                mt.insertParameterTypes(0, ClassDesc.ofDescriptor("I")).descriptorString()),
              "insertParameterTypes");
        check("(Ljava/lang/String;)I".equals(
                mt.insertParameterTypes(0, ClassDesc.ofDescriptor("I"))
                  .dropParameterTypes(0, 1).descriptorString()),
              "dropParameterTypes");

        MethodType resolved = mt.resolveConstantDesc(MethodHandles.lookup());
        check(resolved.returnType() == int.class &&
              resolved.parameterType(0) == String.class, "resolves to MethodType");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
