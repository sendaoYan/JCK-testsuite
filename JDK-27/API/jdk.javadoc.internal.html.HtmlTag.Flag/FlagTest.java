/*
 * @test
 * @summary jdk.javadoc.internal.html.HtmlTag.Flag conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.html:+open
 * @run main FlagTest
 */
public class FlagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.html.HtmlTag$Flag", false, FlagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 5, "5 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 4, "4 enum constants");
        check(ec.contains("ACCEPTS_BLOCK"), "enum constant ACCEPTS_BLOCK");
        check(ec.contains("ACCEPTS_INLINE"), "enum constant ACCEPTS_INLINE");
        check(ec.contains("EXPECT_CONTENT"), "enum constant EXPECT_CONTENT");
        check(ec.contains("NO_NEST"), "enum constant NO_NEST");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
