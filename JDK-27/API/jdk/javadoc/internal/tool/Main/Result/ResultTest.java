/*
 * @test
 * @summary jdk.javadoc.internal.tool.Main.Result conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.tool:+open
 * @run main ResultTest
 */
public class ResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.tool.Main$Result", false, ResultTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 5, "5 declared methods");
        check(c.getDeclaredFields().length == 7, "7 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 5, "5 enum constants");
        check(ec.contains("ABNORMAL"), "enum constant ABNORMAL");
        check(ec.contains("CMDERR"), "enum constant CMDERR");
        check(ec.contains("ERROR"), "enum constant ERROR");
        check(ec.contains("OK"), "enum constant OK");
        check(ec.contains("SYSERR"), "enum constant SYSERR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
