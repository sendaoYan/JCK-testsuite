/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor.OfField conformance: isArray/isPrimitive/componentType/arrayType.
 * @run main OfFieldTest
 */
import java.lang.invoke.TypeDescriptor;

public class OfFieldTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(TypeDescriptor.OfField.class.isInterface(), "is interface");
        TypeDescriptor.OfField<?> i = int.class;
        check(i.isPrimitive(), "int primitive");
        check(!i.isArray(), "int not array");
        TypeDescriptor.OfField<?> sa = String[].class;
        check(sa.isArray(), "String[] array");
        check(sa.componentType() == String.class, "componentType");
        check(int.class.arrayType() == int[].class, "arrayType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
