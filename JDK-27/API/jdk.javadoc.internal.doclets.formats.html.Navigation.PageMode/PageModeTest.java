/*
 * @test
 * @summary jdk.javadoc.internal.doclets.formats.html.Navigation.PageMode conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.formats.html:+open
 * @run main PageModeTest
 */
public class PageModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.formats.html.Navigation$PageMode", false, PageModeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 22, "22 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 21, "21 enum constants");
        check(ec.contains("ALL_CLASSES"), "enum constant ALL_CLASSES");
        check(ec.contains("ALL_PACKAGES"), "enum constant ALL_PACKAGES");
        check(ec.contains("CLASS"), "enum constant CLASS");
        check(ec.contains("CONSTANT_VALUES"), "enum constant CONSTANT_VALUES");
        check(ec.contains("DEPRECATED"), "enum constant DEPRECATED");
        check(ec.contains("DOC_FILE"), "enum constant DOC_FILE");
        check(ec.contains("EXTERNAL_SPECS"), "enum constant EXTERNAL_SPECS");
        check(ec.contains("HELP"), "enum constant HELP");
        check(ec.contains("INDEX"), "enum constant INDEX");
        check(ec.contains("MODULE"), "enum constant MODULE");
        check(ec.contains("NEW"), "enum constant NEW");
        check(ec.contains("OVERVIEW"), "enum constant OVERVIEW");
        check(ec.contains("PACKAGE"), "enum constant PACKAGE");
        check(ec.contains("PREVIEW"), "enum constant PREVIEW");
        check(ec.contains("RESTRICTED"), "enum constant RESTRICTED");
        check(ec.contains("SEARCH"), "enum constant SEARCH");
        check(ec.contains("SEARCH_TAGS"), "enum constant SEARCH_TAGS");
        check(ec.contains("SERIALIZED_FORM"), "enum constant SERIALIZED_FORM");
        check(ec.contains("SYSTEM_PROPERTIES"), "enum constant SYSTEM_PROPERTIES");
        check(ec.contains("TREE"), "enum constant TREE");
        check(ec.contains("USE"), "enum constant USE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
