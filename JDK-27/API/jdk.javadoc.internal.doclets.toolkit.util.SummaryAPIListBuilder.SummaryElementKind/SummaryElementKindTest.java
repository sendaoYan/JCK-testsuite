/*
 * @test
 * @summary jdk.javadoc.internal.doclets.toolkit.util.SummaryAPIListBuilder.SummaryElementKind conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.toolkit.util:+open
 * @run main SummaryElementKindTest
 */
public class SummaryElementKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.toolkit.util.SummaryAPIListBuilder$SummaryElementKind", false, SummaryElementKindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 14, "14 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 13, "13 enum constants");
        check(ec.contains("ANNOTATION_TYPE"), "enum constant ANNOTATION_TYPE");
        check(ec.contains("ANNOTATION_TYPE_MEMBER"), "enum constant ANNOTATION_TYPE_MEMBER");
        check(ec.contains("CLASS"), "enum constant CLASS");
        check(ec.contains("CONSTRUCTOR"), "enum constant CONSTRUCTOR");
        check(ec.contains("ENUM"), "enum constant ENUM");
        check(ec.contains("ENUM_CONSTANT"), "enum constant ENUM_CONSTANT");
        check(ec.contains("EXCEPTION_CLASS"), "enum constant EXCEPTION_CLASS");
        check(ec.contains("FIELD"), "enum constant FIELD");
        check(ec.contains("INTERFACE"), "enum constant INTERFACE");
        check(ec.contains("METHOD"), "enum constant METHOD");
        check(ec.contains("MODULE"), "enum constant MODULE");
        check(ec.contains("PACKAGE"), "enum constant PACKAGE");
        check(ec.contains("RECORD_CLASS"), "enum constant RECORD_CLASS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
