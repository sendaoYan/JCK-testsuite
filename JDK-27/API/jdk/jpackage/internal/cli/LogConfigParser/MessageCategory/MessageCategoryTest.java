/*
 * @test
 * @summary jdk.jpackage.internal.cli.LogConfigParser.MessageCategory conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.cli:+open
 * @run main MessageCategoryTest
 */
public class MessageCategoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.cli.LogConfigParser$MessageCategory", false, MessageCategoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 6, "6 declared methods");
        check(c.getDeclaredFields().length == 9, "9 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 8, "8 enum constants");
        check(ec.contains("ERRORS"), "enum constant ERRORS");
        check(ec.contains("PROGRESS"), "enum constant PROGRESS");
        check(ec.contains("RESOURCES"), "enum constant RESOURCES");
        check(ec.contains("SUMMARY"), "enum constant SUMMARY");
        check(ec.contains("SYSTEM_LOGGER"), "enum constant SYSTEM_LOGGER");
        check(ec.contains("TOOLS"), "enum constant TOOLS");
        check(ec.contains("TRACE"), "enum constant TRACE");
        check(ec.contains("WARNINGS"), "enum constant WARNINGS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
