/*
 * @test
 * @summary jdk.javadoc.internal.html.HtmlAttr.InputType conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.html:+open
 * @run main InputTypeTest
 */
public class InputTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.html.HtmlAttr$InputType", false, InputTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 4, "4 declared methods");
        check(c.getDeclaredFields().length == 6, "6 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 4, "4 enum constants");
        check(ec.contains("CHECKBOX"), "enum constant CHECKBOX");
        check(ec.contains("RADIO"), "enum constant RADIO");
        check(ec.contains("RESET"), "enum constant RESET");
        check(ec.contains("TEXT"), "enum constant TEXT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
