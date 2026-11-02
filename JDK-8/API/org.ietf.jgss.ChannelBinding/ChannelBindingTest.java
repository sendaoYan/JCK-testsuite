/*
 * @test
 * @summary org.ietf.jgss.ChannelBinding conformance: constructor and getters.
 * @run main ChannelBindingTest
 */

public class ChannelBindingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.ietf.jgss.ChannelBinding cb = new org.ietf.jgss.ChannelBinding(new byte[]{1,2,3});
        check(java.util.Arrays.equals(new byte[]{1,2,3}, cb.getApplicationData()), "getApplicationData");
        check(cb.getInitiatorAddress() == null, "getInitiatorAddress null");
        check(cb.getAcceptorAddress() == null, "getAcceptorAddress null");
        check(cb.equals(cb), "equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
