/*
 * @test
 * @summary sun.nio.cs.ext.JIS_X_0212_Solaris.EncodeHolder conformance: internal class reflection.
 * @modules jdk.charsets/sun.nio.cs.ext:+open
 * @run main EncodeHolderTest
 */
public class EncodeHolderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.JIS_X_0212_Solaris$EncodeHolder", false, EncodeHolderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 0, "0 declared methods");
        check(c.getDeclaredFields().length == 2, "2 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
