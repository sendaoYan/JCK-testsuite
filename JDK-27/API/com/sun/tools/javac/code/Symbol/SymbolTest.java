/*
 * @test
 * @summary com.sun.tools.javac.code.Symbol conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main SymbolTest
 */
public class SymbolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Symbol", false, SymbolTest.class.getClassLoader());
        check(c != null, "loads");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.code.AnnoConstruct"), "superclass");
        check(c.getInterfaces().length >= 2, "implements Element+PoolConstant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
