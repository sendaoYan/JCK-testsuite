/*
 * @test
 * @summary com.sun.tools.javac.code.Symbol.TypeVariableSymbol conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main TypeVariableSymbolTest
 */
public class TypeVariableSymbolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Symbol$TypeVariableSymbol", false, TypeVariableSymbolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Symbol$TypeVariableSymbol"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.code.Symbol$TypeSymbol"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "javax.lang.model.element.TypeParameterElement"), "interface javax.lang.model.element.TypeParameterElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
