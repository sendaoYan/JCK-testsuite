/*
 * @test
 * @summary com.sun.tools.javac.code.Attribute.Enum conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main EnumTest
 */
public class EnumTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Attribute$Enum", false, EnumTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Attribute$Enum"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.code.Attribute"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
