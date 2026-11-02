/*
 * @test
 * @summary com.sun.tools.javac.util.Log.WriterKind conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main WriterKindTest
 */
public class WriterKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Log$WriterKind", false, WriterKindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Log$WriterKind"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 5, "enum constants count");
        check(ec.contains("ERROR"), "enum constant ERROR");
        check(ec.contains("NOTICE"), "enum constant NOTICE");
        check(ec.contains("STDERR"), "enum constant STDERR");
        check(ec.contains("STDOUT"), "enum constant STDOUT");
        check(ec.contains("WARNING"), "enum constant WARNING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
