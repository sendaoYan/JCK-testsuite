/*
 * @test
 * @summary JLS 8.9: enum constants may be used as case labels in a switch
 *          statement.
 * @run main EnumSwitch
 */

public class EnumSwitch {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    static boolean isRed(Suit s) {
        switch (s) {
            case DIAMONDS:
            case HEARTS:
                return true;
            default:
                return false;
        }
    }

    public static void main(String[] args) {
        check(isRed(Suit.HEARTS), "hearts is red");
        check(isRed(Suit.DIAMONDS), "diamonds is red");
        check(!isRed(Suit.CLUBS), "clubs is black");
        check(!isRed(Suit.SPADES), "spades is black");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
