/*
 * @test
 * @summary com.sun.tools.javac.code.Symbol.DynamicVarSymbol conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main DynamicVarSymbolTest
 */
public class DynamicVarSymbolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Symbol$DynamicVarSymbol", false, DynamicVarSymbolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Symbol$DynamicVarSymbol"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.code.Symbol$VarSymbol"), "superclass");
        check(c.getInterfaces().length == 2, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.jvm.PoolConstant$Dynamic"), "interface com.sun.tools.javac.jvm.PoolConstant$Dynamic");
        check(hasInterface(c, "com.sun.tools.javac.jvm.PoolConstant$LoadableConstant"), "interface com.sun.tools.javac.jvm.PoolConstant$LoadableConstant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
