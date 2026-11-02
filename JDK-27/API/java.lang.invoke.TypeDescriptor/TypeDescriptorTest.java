/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor conformance: descriptorString via Class.
 * @run main TypeDescriptorTest
 */
import java.lang.invoke.TypeDescriptor;

public class TypeDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(TypeDescriptor.class.isInterface(), "is interface");
        check(((TypeDescriptor) int.class).descriptorString().equals("I"), "int descriptor I");
        check(((TypeDescriptor) String.class).descriptorString().equals("Ljava/lang/String;"), "String descriptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
