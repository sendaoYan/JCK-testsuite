/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor.OfMethod conformance: parameter/return transforms via MethodType.
 * @run main OfMethodTest
 */
import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;

public class OfMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(TypeDescriptor.OfMethod.class.isInterface(), "is interface");
        MethodType mt = MethodType.methodType(int.class, long.class);
        check(mt.parameterCount() == 1, "parameterCount");
        check(mt.returnType() == int.class, "returnType");
        check(mt.parameterType(0) == long.class, "parameterType");
        check(mt.parameterArray().length == 1, "parameterArray");
        check(mt.parameterList().size() == 1, "parameterList");
        check(mt.changeReturnType(long.class).returnType() == long.class, "changeReturnType");
        check(mt.changeParameterType(0, double.class).parameterType(0) == double.class, "changeParameterType");
        check(mt.dropParameterTypes(0, 1).parameterCount() == 0, "dropParameterTypes");
        check(mt.insertParameterTypes(0, int.class).parameterCount() == 2, "insertParameterTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
