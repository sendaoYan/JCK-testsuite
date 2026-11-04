/*
 * @test
 * @summary javax.smartcardio.CardTerminals.State conformance: white-box structure + black-box behavior.
 * @run main StateTest
 */

import java.lang.reflect.Modifier;

public class StateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.smartcardio.CardTerminals$State");
        check(c.isEnum(), "State is enum");
        check(c.getSuperclass() == Enum.class, "State extends Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 5, "State has 5 constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.smartcardio.CardTerminals.State[] all = javax.smartcardio.CardTerminals.State.values();
        check(all.length == 5, "State.values() length 5");
        check(all[0] == javax.smartcardio.CardTerminals.State.ALL, "State values()[0] ALL");
        check(all[1] == javax.smartcardio.CardTerminals.State.CARD_PRESENT, "State values()[1] CARD_PRESENT");
        check(all[2] == javax.smartcardio.CardTerminals.State.CARD_ABSENT, "State values()[2] CARD_ABSENT");
        check(all[3] == javax.smartcardio.CardTerminals.State.CARD_INSERTION, "State values()[3] CARD_INSERTION");
        check(all[4] == javax.smartcardio.CardTerminals.State.CARD_REMOVAL, "State values()[4] CARD_REMOVAL");

        // name / ordinal
        check("ALL".equals(javax.smartcardio.CardTerminals.State.ALL.name()), "State ALL name");
        check(javax.smartcardio.CardTerminals.State.ALL.ordinal() == 0, "State ALL ordinal");
        check("CARD_PRESENT".equals(javax.smartcardio.CardTerminals.State.CARD_PRESENT.name()), "State CARD_PRESENT name");
        check(javax.smartcardio.CardTerminals.State.CARD_PRESENT.ordinal() == 1, "State CARD_PRESENT ordinal");
        check(javax.smartcardio.CardTerminals.State.CARD_REMOVAL.ordinal() == 4, "State CARD_REMOVAL ordinal");

        // valueOf round-trip
        check(javax.smartcardio.CardTerminals.State.valueOf("ALL") == javax.smartcardio.CardTerminals.State.ALL, "State valueOf ALL");
        check(javax.smartcardio.CardTerminals.State.valueOf("CARD_ABSENT") == javax.smartcardio.CardTerminals.State.CARD_ABSENT, "State valueOf CARD_ABSENT");

        // valueOf invalid -> IllegalArgumentException
        boolean threw = false;
        try { javax.smartcardio.CardTerminals.State.valueOf("NOPE"); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "State valueOf invalid IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
