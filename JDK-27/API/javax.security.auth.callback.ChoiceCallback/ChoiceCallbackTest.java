/*
 * @test
 * @summary javax.security.auth.callback.ChoiceCallback conformance: choice callback choices and selection.
 * @run main ChoiceCallbackTest
 */

import javax.security.auth.callback.*;
import java.util.Arrays;

public class ChoiceCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        String[] choices = new String[]{"A", "B"};
        ChoiceCallback c = new ChoiceCallback("prompt", choices, 1, true);
        check("prompt".equals(c.getPrompt()), "getPrompt");
        check(Arrays.equals(choices, c.getChoices()), "getChoices");
        check(c.getDefaultChoice() == 1, "getDefaultChoice");
        check(c.allowMultipleSelections(), "allowMultipleSelections");
        c.setSelectedIndex(0);
        check(c.getSelectedIndexes()[0] == 0, "getSelectedIndexes");
        c.setSelectedIndexes(new int[]{0, 1});
        check(c.getSelectedIndexes().length == 2, "setSelectedIndexes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
