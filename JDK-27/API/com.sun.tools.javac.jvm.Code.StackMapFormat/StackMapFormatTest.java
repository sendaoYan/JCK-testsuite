/*
 * @test
 * @summary com.sun.tools.javac.jvm.Code.StackMapFormat conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.jvm:+open
 * @run main StackMapFormatTest
 */
public class StackMapFormatTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.jvm.Code$StackMapFormat", false, StackMapFormatTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.jvm.Code$StackMapFormat"), "name");
        check(c.isEnum(), "enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 3, "enum constants count");
        check(ec.contains("CLDC"), "enum constant CLDC");
        check(ec.contains("JSR202"), "enum constant JSR202");
        check(ec.contains("NONE"), "enum constant NONE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
