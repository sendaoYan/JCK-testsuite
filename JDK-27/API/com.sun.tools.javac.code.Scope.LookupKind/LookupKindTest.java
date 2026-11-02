/*
 * @test
 * @summary com.sun.tools.javac.code.Scope.LookupKind conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main LookupKindTest
 */
public class LookupKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Scope$LookupKind", false, LookupKindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Scope$LookupKind"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 2, "enum constants count");
        check(ec.contains("NON_RECURSIVE"), "enum constant NON_RECURSIVE");
        check(ec.contains("RECURSIVE"), "enum constant RECURSIVE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
