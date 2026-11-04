/*
 * @test
 * @summary com.sun.tools.javac.main.Main.Result conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.main:+open
 * @run main ResultTest
 */
public class ResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.main.Main$Result", false, ResultTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.main.Main$Result"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 5, "enum constants count");
        check(ec.contains("ABNORMAL"), "enum constant ABNORMAL");
        check(ec.contains("CMDERR"), "enum constant CMDERR");
        check(ec.contains("ERROR"), "enum constant ERROR");
        check(ec.contains("OK"), "enum constant OK");
        check(ec.contains("SYSERR"), "enum constant SYSERR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
