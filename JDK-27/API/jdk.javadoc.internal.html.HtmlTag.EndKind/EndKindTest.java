/*
 * @test
 * @summary jdk.javadoc.internal.html.HtmlTag.EndKind conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.html:+open
 * @run main EndKindTest
 */
public class EndKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.html.HtmlTag$EndKind", false, EndKindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 4, "4 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 3, "3 enum constants");
        check(ec.contains("NONE"), "enum constant NONE");
        check(ec.contains("OPTIONAL"), "enum constant OPTIONAL");
        check(ec.contains("REQUIRED"), "enum constant REQUIRED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
