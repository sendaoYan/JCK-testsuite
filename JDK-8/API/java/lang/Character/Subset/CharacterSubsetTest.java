/*
 * @test
 * @summary java.lang.Character.Subset API conformance: toString of a named subset, and
 *          UnicodeBlock being a Subset with of(char).
 * @run main CharacterSubsetTest
 */

public class CharacterSubsetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSubset() {
        Character.Subset s = new Character.Subset("test") {};
        check("test".equals(s.toString()), "toString");
        Character.Subset latin = Character.UnicodeBlock.BASIC_LATIN;
        check(latin instanceof Character.Subset, "UnicodeBlock is a Subset");
        check(Character.UnicodeBlock.of('A') == Character.UnicodeBlock.BASIC_LATIN, "of(char)");
    }

    public static void main(String[] args) {
        testSubset();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
