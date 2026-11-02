/*
 * @test
 * @summary com.sun.tools.javac.util.RichDiagnosticFormatter.RichConfiguration.RichFormatterFeature conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main RichFormatterFeatureTest
 */
public class RichFormatterFeatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.RichDiagnosticFormatter$RichConfiguration$RichFormatterFeature", false, RichFormatterFeatureTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.RichDiagnosticFormatter$RichConfiguration$RichFormatterFeature"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 3, "enum constants count");
        check(ec.contains("SIMPLE_NAMES"), "enum constant SIMPLE_NAMES");
        check(ec.contains("UNIQUE_TYPEVAR_NAMES"), "enum constant UNIQUE_TYPEVAR_NAMES");
        check(ec.contains("WHERE_CLAUSES"), "enum constant WHERE_CLAUSES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
