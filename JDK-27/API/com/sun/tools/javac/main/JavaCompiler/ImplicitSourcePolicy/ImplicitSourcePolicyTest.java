/*
 * @test
 * @summary com.sun.tools.javac.main.JavaCompiler.ImplicitSourcePolicy conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.main:+open
 * @run main ImplicitSourcePolicyTest
 */
public class ImplicitSourcePolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.main.JavaCompiler$ImplicitSourcePolicy", false, ImplicitSourcePolicyTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.main.JavaCompiler$ImplicitSourcePolicy"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 3, "enum constants count");
        check(ec.contains("CLASS"), "enum constant CLASS");
        check(ec.contains("NONE"), "enum constant NONE");
        check(ec.contains("UNSET"), "enum constant UNSET");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
