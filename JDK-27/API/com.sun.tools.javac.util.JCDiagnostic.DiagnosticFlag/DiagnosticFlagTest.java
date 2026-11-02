/*
 * @test
 * @summary com.sun.tools.javac.util.JCDiagnostic.DiagnosticFlag conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main DiagnosticFlagTest
 */
public class DiagnosticFlagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.JCDiagnostic$DiagnosticFlag", false, DiagnosticFlagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.JCDiagnostic$DiagnosticFlag"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 12, "enum constants count");
        check(ec.contains("AGGREGATE"), "enum constant AGGREGATE");
        check(ec.contains("API"), "enum constant API");
        check(ec.contains("COMPRESSED"), "enum constant COMPRESSED");
        check(ec.contains("DEFAULT_ENABLED"), "enum constant DEFAULT_ENABLED");
        check(ec.contains("DEPRECATION_SENSITIVE"), "enum constant DEPRECATION_SENSITIVE");
        check(ec.contains("MANDATORY"), "enum constant MANDATORY");
        check(ec.contains("NON_DEFERRABLE"), "enum constant NON_DEFERRABLE");
        check(ec.contains("RECOVERABLE"), "enum constant RECOVERABLE");
        check(ec.contains("RESOLVE_ERROR"), "enum constant RESOLVE_ERROR");
        check(ec.contains("SOURCE_LEVEL"), "enum constant SOURCE_LEVEL");
        check(ec.contains("STRICT"), "enum constant STRICT");
        check(ec.contains("SYNTAX"), "enum constant SYNTAX");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
