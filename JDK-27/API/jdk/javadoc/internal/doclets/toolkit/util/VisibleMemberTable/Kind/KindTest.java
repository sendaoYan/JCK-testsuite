/*
 * @test
 * @summary jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.toolkit.util:+open
 * @run main KindTest
 */
public class KindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable$Kind", false, KindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 5, "5 declared methods");
        check(c.getDeclaredFields().length == 16, "16 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 9, "9 enum constants");
        check(ec.contains("ANNOTATION_TYPE_MEMBER"), "enum constant ANNOTATION_TYPE_MEMBER");
        check(ec.contains("ANNOTATION_TYPE_MEMBER_OPTIONAL"), "enum constant ANNOTATION_TYPE_MEMBER_OPTIONAL");
        check(ec.contains("ANNOTATION_TYPE_MEMBER_REQUIRED"), "enum constant ANNOTATION_TYPE_MEMBER_REQUIRED");
        check(ec.contains("CONSTRUCTORS"), "enum constant CONSTRUCTORS");
        check(ec.contains("ENUM_CONSTANTS"), "enum constant ENUM_CONSTANTS");
        check(ec.contains("FIELDS"), "enum constant FIELDS");
        check(ec.contains("METHODS"), "enum constant METHODS");
        check(ec.contains("NESTED_CLASSES"), "enum constant NESTED_CLASSES");
        check(ec.contains("PROPERTIES"), "enum constant PROPERTIES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
