/*
 * @test
 * @summary JLS 5.1.4 (Java SE 8): the single widening-and-narrowing primitive
 *          conversion is byte to char: byte is first widened to int, then the
 *          int is narrowed to char (taking the low 16 bits).
 * @run main ByteToChar
 */

public class ByteToChar {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // byte -> char requires an explicit cast (widening then narrowing).
        byte b = (byte) 0xff;        // -1
        char c = (char) b;           // int -1 narrowed to char = 0xffff
        check(c == (char) 0xffff, "byte -1 -> char 0xffff via widening+narrowing");

        byte positive = 65;          // 'A'
        char c2 = (char) positive;
        check(c2 == 'A', "byte 65 -> char 'A'");

        // byte 0x80 = -128 -> char 0xff80 (65535 - 127 = 65408).
        byte neg = (byte) 0x80;
        check((char) neg == 0xff80, "byte -128 -> char 0xff80");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
