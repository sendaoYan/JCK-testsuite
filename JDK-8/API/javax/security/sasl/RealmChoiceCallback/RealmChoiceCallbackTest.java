/*
 * @test
 * @summary javax.security.sasl.RealmChoiceCallback conformance: constructor and getters.
 * @run main RealmChoiceCallbackTest
 */

public class RealmChoiceCallbackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.sasl.RealmChoiceCallback c = new javax.security.sasl.RealmChoiceCallback(
            "prompt", new String[]{"r1","r2"}, 0, false);
        check(java.util.Arrays.equals(new String[]{"r1","r2"}, c.getChoices()), "getChoices");
        check(c.getDefaultChoice() == 0, "getDefaultChoice");
        check(!c.allowMultipleSelections(), "allowMultipleSelections");
        c.setSelectedIndex(1);
        check(c.getSelectedIndexes()[0] == 1, "set/getSelectedIndexes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
