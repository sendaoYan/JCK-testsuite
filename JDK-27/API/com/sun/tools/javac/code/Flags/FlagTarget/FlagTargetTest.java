/*
 * @test
 * @summary com.sun.tools.javac.code.Flags.FlagTarget conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main FlagTargetTest
 */
public class FlagTargetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Flags$FlagTarget", false, FlagTargetTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Flags$FlagTarget"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 7, "enum constants count");
        check(ec.contains("BLOCK"), "enum constant BLOCK");
        check(ec.contains("CLASS"), "enum constant CLASS");
        check(ec.contains("METHOD"), "enum constant METHOD");
        check(ec.contains("MODULE"), "enum constant MODULE");
        check(ec.contains("PACKAGE"), "enum constant PACKAGE");
        check(ec.contains("TYPE_VAR"), "enum constant TYPE_VAR");
        check(ec.contains("VARIABLE"), "enum constant VARIABLE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
