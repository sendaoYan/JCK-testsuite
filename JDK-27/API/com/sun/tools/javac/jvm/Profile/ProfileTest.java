/*
 * @test
 * @summary com.sun.tools.javac.jvm.Profile conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.jvm:+open
 * @run main ProfileTest
 */
public class ProfileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.jvm.Profile", false, ProfileTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.jvm.Profile"), "name");
        check(c.isEnum(), "enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 4, "enum constants count");
        check(ec.contains("COMPACT1"), "enum constant COMPACT1");
        check(ec.contains("COMPACT2"), "enum constant COMPACT2");
        check(ec.contains("COMPACT3"), "enum constant COMPACT3");
        check(ec.contains("DEFAULT"), "enum constant DEFAULT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
