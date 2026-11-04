/*
 * @test
 * @summary com.sun.tools.javac.comp.ExhaustivenessComputer.BindingPattern conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.comp:+open
 * @run main BindingPatternTest
 */
public class BindingPatternTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.comp.ExhaustivenessComputer$BindingPattern", false, BindingPatternTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.comp.ExhaustivenessComputer$BindingPattern"), "name");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.comp.ExhaustivenessComputer$PatternDescription"), "interface com.sun.tools.javac.comp.ExhaustivenessComputer$PatternDescription");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
