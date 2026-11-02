/*
 * @test
 * @summary com.sun.tools.javac.main.Option.PkgInfo conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.main:+open
 * @run main PkgInfoTest
 */
public class PkgInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.main.Option$PkgInfo", false, PkgInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.main.Option$PkgInfo"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 3, "enum constants count");
        check(ec.contains("ALWAYS"), "enum constant ALWAYS");
        check(ec.contains("LEGACY"), "enum constant LEGACY");
        check(ec.contains("NONEMPTY"), "enum constant NONEMPTY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
