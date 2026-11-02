/*
 * @test
 * @summary sun.security.util.BitArray conformance: type shape + behavioral (headless-safe).
 * @run main BitArrayTest
 */

public class BitArrayTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.BitArray", false, BitArrayTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "isInterface");
        check(!c.isEnum(), "isEnum");
        check(!c.isAnnotation(), "isAnnotation");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        sun.security.util.BitArray b = new sun.security.util.BitArray(new boolean[]{true,false,true});
        check(b.length() == 3, "BitArray length");
        check(b.get(0) && !b.get(1) && b.get(2), "BitArray get");
        b.set(1, true); check(b.get(1), "BitArray set");
        check(b.toByteArray().length >= 1, "BitArray toByteArray");
        check(b.toString() != null, "BitArray toString");
        check(b.truncate() != null, "BitArray truncate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
