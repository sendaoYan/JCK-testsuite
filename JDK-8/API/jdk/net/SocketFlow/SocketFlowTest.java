/*
 * @test
 * @summary jdk.net.SocketFlow conformance: socket flow.
 * @run main SocketFlowTest
 */

public class SocketFlowTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(jdk.net.SocketFlow.NORMAL_PRIORITY == 1, "NORMAL_PRIORITY");
        check(jdk.net.SocketFlow.HIGH_PRIORITY == 2, "HIGH_PRIORITY");
        jdk.net.SocketFlow f = jdk.net.SocketFlow.create();
        check(f != null, "create");
        check(f.priority() == jdk.net.SocketFlow.NORMAL_PRIORITY, "default priority");
        check(f.bandwidth() == -1, "default bandwidth");
        check(f.status() == jdk.net.SocketFlow.Status.NO_STATUS, "status NO_STATUS");
        check(f.priority(2) == f, "priority returns this");
        check(f.priority() == 2, "priority set");
        check(f.bandwidth(1024) == f, "bandwidth returns this");
        check(f.bandwidth() == 1024, "bandwidth set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
