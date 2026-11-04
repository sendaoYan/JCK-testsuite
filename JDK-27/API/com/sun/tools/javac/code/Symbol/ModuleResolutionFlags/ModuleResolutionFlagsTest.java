/*
 * @test
 * @summary com.sun.tools.javac.code.Symbol.ModuleResolutionFlags conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main ModuleResolutionFlagsTest
 */
public class ModuleResolutionFlagsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Symbol$ModuleResolutionFlags", false, ModuleResolutionFlagsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Symbol$ModuleResolutionFlags"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 4, "enum constants count");
        check(ec.contains("DO_NOT_RESOLVE_BY_DEFAULT"), "enum constant DO_NOT_RESOLVE_BY_DEFAULT");
        check(ec.contains("WARN_DEPRECATED"), "enum constant WARN_DEPRECATED");
        check(ec.contains("WARN_DEPRECATED_REMOVAL"), "enum constant WARN_DEPRECATED_REMOVAL");
        check(ec.contains("WARN_INCUBATING"), "enum constant WARN_INCUBATING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
