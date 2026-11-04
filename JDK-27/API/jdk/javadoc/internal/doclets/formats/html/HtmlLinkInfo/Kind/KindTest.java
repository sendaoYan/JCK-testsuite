/*
 * @test
 * @summary jdk.javadoc.internal.doclets.formats.html.HtmlLinkInfo.Kind conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.formats.html:+open
 * @run main KindTest
 */
public class KindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.formats.html.HtmlLinkInfo$Kind", false, KindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 8, "8 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 7, "7 enum constants");
        check(ec.contains("LINK_TYPE_PARAMS"), "enum constant LINK_TYPE_PARAMS");
        check(ec.contains("LINK_TYPE_PARAMS_AND_BOUNDS"), "enum constant LINK_TYPE_PARAMS_AND_BOUNDS");
        check(ec.contains("PLAIN"), "enum constant PLAIN");
        check(ec.contains("SHOW_PREVIEW"), "enum constant SHOW_PREVIEW");
        check(ec.contains("SHOW_TYPE_PARAMS"), "enum constant SHOW_TYPE_PARAMS");
        check(ec.contains("SHOW_TYPE_PARAMS_AND_BOUNDS"), "enum constant SHOW_TYPE_PARAMS_AND_BOUNDS");
        check(ec.contains("SHOW_TYPE_PARAMS_IN_LABEL"), "enum constant SHOW_TYPE_PARAMS_IN_LABEL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
