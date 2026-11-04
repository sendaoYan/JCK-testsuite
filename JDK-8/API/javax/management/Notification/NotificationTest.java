/*
 * @test
 * @summary javax.management.Notification conformance: type/source/sequence/time/message.
 * @run main NotificationTest
 */

import javax.management.*;
public class NotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Notification n = new Notification("test.type", "src", 1L, 100L, "msg");
        check("test.type".equals(n.getType()), "type");
        check("src".equals(n.getSource()), "source");
        check(n.getSequenceNumber() == 1L, "sequence number");
        check(n.getTimeStamp() == 100L, "time stamp");
        check("msg".equals(n.getMessage()), "message");
        n.setSequenceNumber(2L);
        n.setUserData("data");
        n.setSource("src2");
        check(n.getSequenceNumber() == 2L, "setSequenceNumber");
        check("data".equals(n.getUserData()), "user data");
        check("src2".equals(n.getSource()), "setSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
