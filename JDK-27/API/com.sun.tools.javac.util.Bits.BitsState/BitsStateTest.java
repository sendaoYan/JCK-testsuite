/*
 * @test
 * @summary com.sun.tools.javac.util.Bits.BitsState conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main BitsStateTest
 */
public class BitsStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Bits$BitsState", false, BitsStateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Bits$BitsState"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 3, "enum constants count");
        check(ec.contains("NORMAL"), "enum constant NORMAL");
        check(ec.contains("UNINIT"), "enum constant UNINIT");
        check(ec.contains("UNKNOWN"), "enum constant UNKNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
