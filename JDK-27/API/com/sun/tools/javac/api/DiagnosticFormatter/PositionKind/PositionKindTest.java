/*
 * @test
 * @summary com.sun.tools.javac.api.DiagnosticFormatter.PositionKind conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.api:+open
 * @run main PositionKindTest
 */
public class PositionKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.api.DiagnosticFormatter$PositionKind", false, PositionKindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.api.DiagnosticFormatter$PositionKind"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 5, "enum constants count");
        check(ec.contains("COLUMN"), "enum constant COLUMN");
        check(ec.contains("END"), "enum constant END");
        check(ec.contains("LINE"), "enum constant LINE");
        check(ec.contains("OFFSET"), "enum constant OFFSET");
        check(ec.contains("START"), "enum constant START");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
