/*
 * @test
 * @summary jdk.javadoc.internal.tool.AccessLevel conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.tool:+open
 * @run main AccessLevelTest
 */
public class AccessLevelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.tool.AccessLevel", false, AccessLevelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 4, "4 declared methods");
        check(c.getDeclaredFields().length == 5, "5 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 4, "4 enum constants");
        check(ec.contains("PACKAGE"), "enum constant PACKAGE");
        check(ec.contains("PRIVATE"), "enum constant PRIVATE");
        check(ec.contains("PROTECTED"), "enum constant PROTECTED");
        check(ec.contains("PUBLIC"), "enum constant PUBLIC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
