/*
 * @test
 * @summary jdk.javadoc.internal.doclint.Checker.Flag conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclint:+open
 * @run main FlagTest
 */
public class FlagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclint.Checker$Flag", false, FlagTest.class.getClassLoader());
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
        check(ec.contains("HAS_ELEMENT"), "enum constant HAS_ELEMENT");
        check(ec.contains("HAS_HEADING"), "enum constant HAS_HEADING");
        check(ec.contains("HAS_INLINE_TAG"), "enum constant HAS_INLINE_TAG");
        check(ec.contains("HAS_TEXT"), "enum constant HAS_TEXT");
        check(ec.contains("REPORTED_BAD_INLINE"), "enum constant REPORTED_BAD_INLINE");
        check(ec.contains("TABLE_HAS_CAPTION"), "enum constant TABLE_HAS_CAPTION");
        check(ec.contains("TABLE_IS_PRESENTATION"), "enum constant TABLE_IS_PRESENTATION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
