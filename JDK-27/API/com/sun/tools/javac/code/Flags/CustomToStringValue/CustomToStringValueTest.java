/*
 * @test
 * @summary com.sun.tools.javac.code.Flags.CustomToStringValue conformance: internal annotation reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main CustomToStringValueTest
 */
public class CustomToStringValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Flags$CustomToStringValue", false, CustomToStringValueTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Flags$CustomToStringValue"), "name");
        check(c.isInterface(), "interface");
        check(c.isAnnotation(), "annotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "java.lang.annotation.Annotation"), "interface java.lang.annotation.Annotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
