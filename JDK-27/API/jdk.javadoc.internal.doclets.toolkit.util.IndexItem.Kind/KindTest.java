/*
 * @test
 * @summary jdk.javadoc.internal.doclets.toolkit.util.IndexItem.Kind conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.toolkit.util:+open
 * @run main KindTest
 */
public class KindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.toolkit.util.IndexItem$Kind", false, KindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 5, "5 declared methods");
        check(c.getDeclaredFields().length == 20, "20 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 19, "19 enum constants");
        check(ec.contains("ANNOTATION_ELEMENT"), "enum constant ANNOTATION_ELEMENT");
        check(ec.contains("ANNOTATION_TYPE"), "enum constant ANNOTATION_TYPE");
        check(ec.contains("CLASS"), "enum constant CLASS");
        check(ec.contains("CONSTRUCTOR"), "enum constant CONSTRUCTOR");
        check(ec.contains("ENUM"), "enum constant ENUM");
        check(ec.contains("ENUM_CONSTANT"), "enum constant ENUM_CONSTANT");
        check(ec.contains("EXCEPTION_CLASS"), "enum constant EXCEPTION_CLASS");
        check(ec.contains("EXTERNAL_SPEC"), "enum constant EXTERNAL_SPEC");
        check(ec.contains("FIELD"), "enum constant FIELD");
        check(ec.contains("INTERFACE"), "enum constant INTERFACE");
        check(ec.contains("METHOD"), "enum constant METHOD");
        check(ec.contains("RECORD"), "enum constant RECORD");
        check(ec.contains("RECORD_COMPONENT"), "enum constant RECORD_COMPONENT");
        check(ec.contains("SEARCH_ITEM"), "enum constant SEARCH_ITEM");
        check(ec.contains("SECTION"), "enum constant SECTION");
        check(ec.contains("STATIC_FIELD"), "enum constant STATIC_FIELD");
        check(ec.contains("STATIC_METHOD"), "enum constant STATIC_METHOD");
        check(ec.contains("SUMMARY_PAGE"), "enum constant SUMMARY_PAGE");
        check(ec.contains("SYSTEM_PROPERTY"), "enum constant SYSTEM_PROPERTY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
