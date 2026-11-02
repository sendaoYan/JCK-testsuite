/*
 * @test
 * @summary javax.security.sasl.RealmChoiceCallback conformance: SASL realm choice callback.
 * @run main RealmChoiceCallbackTest
 */

import javax.security.sasl.*;

public class RealmChoiceCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        RealmChoiceCallback c = new RealmChoiceCallback("prompt", new String[]{"R1", "R2"}, 0, false);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(c.getChoices().length == 2, "getChoices length");
        check(c.getDefaultChoice() == 0, "getDefaultChoice");
        check(!c.allowMultipleSelections(), "allowMultipleSelections false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
