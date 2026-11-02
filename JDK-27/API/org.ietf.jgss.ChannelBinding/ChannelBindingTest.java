/*
 * @test
 * @summary org.ietf.jgss.ChannelBinding conformance: channel binding addresses and data.
 * @run main ChannelBindingTest
 */

import org.ietf.jgss.*;
import java.util.Arrays;

public class ChannelBindingTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ChannelBinding cb = new ChannelBinding(null, null, new byte[]{1, 2, 3});
        check(cb.getInitiatorAddress() == null, "initiator null");
        check(cb.getAcceptorAddress() == null, "acceptor null");
        check(Arrays.equals(new byte[]{1, 2, 3}, cb.getApplicationData()), "getApplicationData");
        check(cb.equals(cb), "equals self");
        check(new ChannelBinding(new byte[]{1}).equals(new ChannelBinding(new byte[]{1})), "equals by data");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
