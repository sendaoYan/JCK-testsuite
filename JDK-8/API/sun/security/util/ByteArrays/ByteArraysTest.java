/*
 * @test
 * @summary sun.security.util.ByteArrays conformance: type shape + behavioral (headless-safe).
 * @run main ByteArraysTest
 */

public class ByteArraysTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.ByteArrays", false, ByteArraysTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        byte[] xa = new byte[]{1,2,3,4};
        byte[] xb = new byte[]{1,2,3,4};
        byte[] xc = new byte[]{9,9,9,9};
        check(sun.security.util.ByteArrays.isEqual(xa,0,4,xb,0,4), "ByteArrays isEqual true");
        check(!sun.security.util.ByteArrays.isEqual(xa,0,4,xb,0,3), "ByteArrays isEqual false (length)");
        check(!sun.security.util.ByteArrays.isEqual(xa,0,4,xc,0,4), "ByteArrays isEqual false (content)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
