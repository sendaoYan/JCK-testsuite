/*
 * @test
 * @summary com.sun.tools.javac.util.Utf8NameTable conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main Utf8NameTableTest
 */
public class Utf8NameTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Utf8NameTable", false, Utf8NameTableTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Utf8NameTable"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.util.Name$Table"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
