/*
 * @test
 * @summary org.ietf.jgss.MessageProp conformance: message prop QOP and privacy flags.
 * @run main MessagePropTest
 */

import org.ietf.jgss.*;

public class MessagePropTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        MessageProp mp = new MessageProp(2, true);
        check(mp.getQOP() == 2, "getQOP");
        check(mp.getPrivacy(), "getPrivacy");
        mp.setQOP(0);
        mp.setPrivacy(false);
        check(mp.getQOP() == 0 && !mp.getPrivacy(), "setters");
        MessageProp mp2 = new MessageProp(true);
        check(mp2.getQOP() == 0 && mp2.getPrivacy(), "boolean ctor");
        mp.setSupplementaryStates(true, true, true, true, 3, "minor");
        check(mp.isDuplicateToken() && mp.isOldToken() && mp.isUnseqToken() && mp.isGapToken(), "supplementary states");
        check(mp.getMinorStatus() == 3, "getMinorStatus");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
