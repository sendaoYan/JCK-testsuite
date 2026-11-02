/*
 * @test
 * @summary javax.security.auth.callback.ChoiceCallback conformance: constructor and getters.
 * @run main ChoiceCallbackTest
 */

public class ChoiceCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.ChoiceCallback c = new javax.security.auth.callback.ChoiceCallback(
            "prompt", new String[]{"a","b"}, 1, false);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(java.util.Arrays.equals(new String[]{"a","b"}, c.getChoices()), "getChoices");
        check(c.getDefaultChoice() == 1, "getDefaultChoice");
        check(!c.allowMultipleSelections(), "allowMultipleSelections");
        c.setSelectedIndex(0);
        check(c.getSelectedIndexes()[0] == 0, "set/getSelectedIndexes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
