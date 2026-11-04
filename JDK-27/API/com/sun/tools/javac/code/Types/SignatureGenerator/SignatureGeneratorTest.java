/*
 * @test
 * @summary com.sun.tools.javac.code.Types.SignatureGenerator conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main SignatureGeneratorTest
 */
public class SignatureGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Types$SignatureGenerator", false, SignatureGeneratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Types$SignatureGenerator"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
