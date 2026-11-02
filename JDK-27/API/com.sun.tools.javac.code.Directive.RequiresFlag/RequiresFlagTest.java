/*
 * @test
 * @summary com.sun.tools.javac.code.Directive.RequiresFlag conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main RequiresFlagTest
 */
public class RequiresFlagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Directive$RequiresFlag", false, RequiresFlagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Directive$RequiresFlag"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 5, "enum constants count");
        check(ec.contains("EXTRA"), "enum constant EXTRA");
        check(ec.contains("MANDATED"), "enum constant MANDATED");
        check(ec.contains("STATIC_PHASE"), "enum constant STATIC_PHASE");
        check(ec.contains("SYNTHETIC"), "enum constant SYNTHETIC");
        check(ec.contains("TRANSITIVE"), "enum constant TRANSITIVE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
