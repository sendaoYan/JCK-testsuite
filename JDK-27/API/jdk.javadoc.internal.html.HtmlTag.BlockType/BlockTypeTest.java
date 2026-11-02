/*
 * @test
 * @summary jdk.javadoc.internal.html.HtmlTag.BlockType conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.html:+open
 * @run main BlockTypeTest
 */
public class BlockTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.html.HtmlTag$BlockType", false, BlockTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 6, "6 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 5, "5 enum constants");
        check(ec.contains("BLOCK"), "enum constant BLOCK");
        check(ec.contains("INLINE"), "enum constant INLINE");
        check(ec.contains("LIST_ITEM"), "enum constant LIST_ITEM");
        check(ec.contains("OTHER"), "enum constant OTHER");
        check(ec.contains("TABLE_ITEM"), "enum constant TABLE_ITEM");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
