/*
 * @test
 * @summary com.sun.tools.javac.util.DefinedBy.Api conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main ApiTest
 */
public class ApiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.DefinedBy$Api", false, ApiTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.DefinedBy$Api"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 4, "enum constants count");
        check(ec.contains("ANNOTATION_PROCESSING"), "enum constant ANNOTATION_PROCESSING");
        check(ec.contains("COMPILER"), "enum constant COMPILER");
        check(ec.contains("COMPILER_TREE"), "enum constant COMPILER_TREE");
        check(ec.contains("LANGUAGE_MODEL"), "enum constant LANGUAGE_MODEL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
