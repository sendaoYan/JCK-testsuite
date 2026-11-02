/*
 * @test
 * @summary jdk.javadoc.internal.doclets.formats.html.taglets.ParamTaglet.ParamKind conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.formats.html.taglets:+open
 * @run main ParamKindTest
 */
public class ParamKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.formats.html.taglets.ParamTaglet$ParamKind", false, ParamKindTest.class.getClassLoader());
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
        check(ec.contains("PARAMETER"), "enum constant PARAMETER");
        check(ec.contains("RECORD_COMPONENT"), "enum constant RECORD_COMPONENT");
        check(ec.contains("TYPE_PARAMETER"), "enum constant TYPE_PARAMETER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
