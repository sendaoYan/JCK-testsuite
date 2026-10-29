/*
 * @test
 * @summary JLS 5.1.4: the conversion byte -> char first widens byte to int,
 *          then narrows int to char (the only widening-and-narrowing primitive
 *          conversion).
 * @run main WideningAndNarrowingPrimitiveConversion
 */

public class WideningAndNarrowingPrimitiveConversion {
    public static void main(String[] args) {
        byte b = (byte) 0xff;     // -1
        // byte -> char: widen byte to int (-1 = 0xffffffff), then narrow to char
        // (low 16 bits = 0xffff = 65535).
        char c = (char) b;
        if (c != 0xffff) throw new AssertionError("byte -> char must be 0xffff, got " + (int) c);

        byte pos = 65;            // 'A'
        char cp = (char) pos;     // widen to int 65, narrow to char 'A'
        if (cp != 'A') throw new AssertionError("byte -> char positive");

        System.out.println("PASS");
    }
}
