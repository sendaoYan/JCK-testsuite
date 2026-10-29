/*
 * @test
 * @summary JLS 5.1.8: the eight unboxing conversions (wrapper -> primitive).
 *          Unboxing a null reference throws a NullPointerException.
 * @run main UnboxingConversion
 */

public class UnboxingConversion {
    public static void main(String[] args) {
        boolean z = Boolean.TRUE;
        byte b = Byte.valueOf((byte) 1);
        short s = Short.valueOf((short) 2);
        char c = Character.valueOf('c');
        int i = Integer.valueOf(3);
        long l = Long.valueOf(4L);
        float f = Float.valueOf(5.0f);
        double d = Double.valueOf(6.0);

        if (!z || b != 1 || s != 2 || c != 'c' || i != 3 || l != 4L) {
            throw new AssertionError("unboxing conversions");
        }
        if (f != 5.0f || d != 6.0) throw new AssertionError("float/double unboxing");

        // Unboxing a null reference throws NullPointerException.
        Integer nil = null;
        try {
            int x = nil;
            throw new AssertionError("expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }

        System.out.println("PASS");
    }
}
