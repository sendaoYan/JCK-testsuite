/*
 * @test
 * @summary com.sun.tools.javac.code.Symbol.ClassSymbol conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main ClassSymbolTest
 */
public class ClassSymbolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Symbol$ClassSymbol", false, ClassSymbolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Symbol$ClassSymbol"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.code.Symbol$TypeSymbol"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "javax.lang.model.element.TypeElement"), "interface javax.lang.model.element.TypeElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
