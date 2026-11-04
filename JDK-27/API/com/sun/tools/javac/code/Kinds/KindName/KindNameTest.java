/*
 * @test
 * @summary com.sun.tools.javac.code.Kinds.KindName conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main KindNameTest
 */
public class KindNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Kinds$KindName", false, KindNameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Kinds$KindName"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.api.Formattable"), "interface com.sun.tools.javac.api.Formattable");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 17, "enum constants count");
        check(ec.contains("ANNOTATION"), "enum constant ANNOTATION");
        check(ec.contains("BOUND"), "enum constant BOUND");
        check(ec.contains("CLASS"), "enum constant CLASS");
        check(ec.contains("CONSTRUCTOR"), "enum constant CONSTRUCTOR");
        check(ec.contains("ENUM"), "enum constant ENUM");
        check(ec.contains("INSTANCE_INIT"), "enum constant INSTANCE_INIT");
        check(ec.contains("INTERFACE"), "enum constant INTERFACE");
        check(ec.contains("METHOD"), "enum constant METHOD");
        check(ec.contains("MODULE"), "enum constant MODULE");
        check(ec.contains("PACKAGE"), "enum constant PACKAGE");
        check(ec.contains("RECORD"), "enum constant RECORD");
        check(ec.contains("RECORD_COMPONENT"), "enum constant RECORD_COMPONENT");
        check(ec.contains("STATIC"), "enum constant STATIC");
        check(ec.contains("STATIC_INIT"), "enum constant STATIC_INIT");
        check(ec.contains("TYPEVAR"), "enum constant TYPEVAR");
        check(ec.contains("VAL"), "enum constant VAL");
        check(ec.contains("VAR"), "enum constant VAR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
