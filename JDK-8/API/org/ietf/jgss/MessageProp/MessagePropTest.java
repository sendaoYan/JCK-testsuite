/*
 * @test
 * @summary org.ietf.jgss.MessageProp conformance: constructors and getters.
 * @run main MessagePropTest
 */

public class MessagePropTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.ietf.jgss.MessageProp m = new org.ietf.jgss.MessageProp(2, true);
        check(m.getQOP() == 2, "getQOP");
        check(m.getPrivacy(), "getPrivacy");
        check(!m.isOldToken(), "isOldToken false");
        m.setPrivacy(false);
        check(!m.getPrivacy(), "setPrivacy");
        org.ietf.jgss.MessageProp m2 = new org.ietf.jgss.MessageProp(true);
        check(m2.getQOP() == 0, "default QOP 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
