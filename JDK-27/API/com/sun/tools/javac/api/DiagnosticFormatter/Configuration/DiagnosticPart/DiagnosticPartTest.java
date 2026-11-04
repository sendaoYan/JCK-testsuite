/*
 * @test
 * @summary com.sun.tools.javac.api.DiagnosticFormatter.Configuration.DiagnosticPart conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.api:+open
 * @run main DiagnosticPartTest
 */
public class DiagnosticPartTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.api.DiagnosticFormatter$Configuration$DiagnosticPart", false, DiagnosticPartTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.api.DiagnosticFormatter$Configuration$DiagnosticPart"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 5, "enum constants count");
        check(ec.contains("DETAILS"), "enum constant DETAILS");
        check(ec.contains("JLS"), "enum constant JLS");
        check(ec.contains("SOURCE"), "enum constant SOURCE");
        check(ec.contains("SUBDIAGNOSTICS"), "enum constant SUBDIAGNOSTICS");
        check(ec.contains("SUMMARY"), "enum constant SUMMARY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
