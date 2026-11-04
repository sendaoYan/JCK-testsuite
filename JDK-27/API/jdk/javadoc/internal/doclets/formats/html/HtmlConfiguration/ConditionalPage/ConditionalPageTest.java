/*
 * @test
 * @summary jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration.ConditionalPage conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.formats.html:+open
 * @run main ConditionalPageTest
 */
public class ConditionalPageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration$ConditionalPage", false, ConditionalPageTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 10, "10 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 9, "9 enum constants");
        check(ec.contains("CONSTANT_VALUES"), "enum constant CONSTANT_VALUES");
        check(ec.contains("DEPRECATED"), "enum constant DEPRECATED");
        check(ec.contains("EXTERNAL_SPECS"), "enum constant EXTERNAL_SPECS");
        check(ec.contains("NEW"), "enum constant NEW");
        check(ec.contains("PREVIEW"), "enum constant PREVIEW");
        check(ec.contains("RESTRICTED"), "enum constant RESTRICTED");
        check(ec.contains("SEARCH_TAGS"), "enum constant SEARCH_TAGS");
        check(ec.contains("SERIALIZED_FORM"), "enum constant SERIALIZED_FORM");
        check(ec.contains("SYSTEM_PROPERTIES"), "enum constant SYSTEM_PROPERTIES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
