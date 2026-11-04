/*
 * @test
 * @summary sun.nio.cs.ext.SJIS_0213.Encoder conformance: internal class reflection.
 * @modules jdk.charsets/sun.nio.cs.ext:+open
 * @run main EncoderTest
 */
public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.SJIS_0213$Encoder", false, EncoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetEncoder"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 10, "10 declared methods");
        check(c.getDeclaredFields().length == 4, "4 declared fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
