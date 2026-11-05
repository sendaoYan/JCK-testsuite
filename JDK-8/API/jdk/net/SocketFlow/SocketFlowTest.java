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
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 深化(2026-11-05)：优先级合法域恰为 {1,2}、带宽非负域、覆盖语义、Status 联动（经 JDK8u372 实测）----
    static void deepen() throws Exception {
        // 1. create() 每次新实例，默认三元组 (priority=1, bandwidth=-1, status=NO_STATUS)
        jdk.net.SocketFlow a = jdk.net.SocketFlow.create();
        jdk.net.SocketFlow b = jdk.net.SocketFlow.create();
        check(a != b, "create returns distinct instances");
        check(a.priority() == 1 && a.bandwidth() == -1L && a.status() == jdk.net.SocketFlow.Status.NO_STATUS,
                "fresh instance defaults");
        check(b.priority() == 1 && b.bandwidth() == -1L, "second instance defaults");
        check(a.getClass() == jdk.net.SocketFlow.class, "create returns SocketFlow");
        check(a.toString() != null, "toString non-null");
        // 2. priority 合法域：仅 NORMAL_PRIORITY(1)/HIGH_PRIORITY(2)——实测 5/10 亦拒绝
        int[] illegal = {-1, 0, 3, 5, 10, 11, 100};
        for (int v : illegal) {
            try { a.priority(v); check(false, "priority(" + v + ") should IAE"); }
            catch (IllegalArgumentException e) { check("invalid priority".equals(e.getMessage()), "priority(" + v + ") IAE 'invalid priority'"); }
        }
        check(a.priority() == 1, "illegal priority leaves value unchanged");
        a.priority(jdk.net.SocketFlow.NORMAL_PRIORITY);
        check(a.priority() == 1, "NORMAL_PRIORITY round-trip");
        a.priority(jdk.net.SocketFlow.HIGH_PRIORITY);
        check(a.priority() == 2, "HIGH_PRIORITY round-trip");
        a.priority(2);
        check(a.priority() == 2, "priority(2) round-trip");
        a.priority(1);
        check(a.priority() == 1, "priority(1) round-trip");
        // 3. bandwidth 合法域：>=0（-1 是 UNSET 哨兵，用户不可设）
        long[] badBw = {-1L, -2L, Long.MIN_VALUE};
        for (long v : badBw) {
            try { a.bandwidth(v); check(false, "bandwidth(" + v + ") should IAE"); }
            catch (IllegalArgumentException e) { check("invalid bandwidth".equals(e.getMessage()), "bandwidth(" + v + ") IAE 'invalid bandwidth'"); }
        }
        check(a.bandwidth() == -1L, "illegal bandwidth leaves value unchanged");
        a.bandwidth(0L);
        check(a.bandwidth() == 0L, "bandwidth(0) round-trip");
        a.bandwidth(Long.MAX_VALUE);
        check(a.bandwidth() == Long.MAX_VALUE, "bandwidth(Long.MAX_VALUE) round-trip");
        // 4. 覆盖语义：后值生效
        a.bandwidth(100L).priority(2);
        check(a.bandwidth() == 100L && a.priority() == 2, "chained setters");
        a.bandwidth(200L);
        check(a.bandwidth() == 200L, "bandwidth overwrite");
        a.priority(1);
        check(a.priority() == 1, "priority overwrite");
        // 5. status 不受 setter 影响（仅内核 SO_FLOW_SLA 生效后由实现更新）
        check(a.status() == jdk.net.SocketFlow.Status.NO_STATUS, "status stays NO_STATUS after setters");
        check(b.status() == jdk.net.SocketFlow.Status.NO_STATUS, "independent instance status");
        // 6. 实例互不干扰
        b.priority(2).bandwidth(50L);
        check(a.priority() == 1 && a.bandwidth() == 200L, "a unaffected by b");
        // 7. SO_FLOW_SLA 真实通道：Linux 上 Sockets.setOption 拒绝（UnsupportedOperationException）
        java.net.Socket s = new java.net.Socket();
        try {
            try {
                jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.SO_FLOW_SLA,
                        jdk.net.SocketFlow.create().priority(1).bandwidth(1024));
                check(false, "SO_FLOW_SLA should be unsupported on Linux");
            } catch (UnsupportedOperationException e) { check(true, "SO_FLOW_SLA UOE on Linux"); }
        } finally { s.close(); }
        // 8. 反射面：无公开构造器、方法签名存在性
        check(jdk.net.SocketFlow.class.getConstructors().length == 0, "no public constructors");
        check(jdk.net.SocketFlow.class.getMethod("create") != null, "static create");
        check(jdk.net.SocketFlow.class.getMethod("priority", int.class) != null, "priority(int)");
        check(jdk.net.SocketFlow.class.getMethod("bandwidth", long.class) != null, "bandwidth(long)");
        check(!jdk.net.SocketFlow.class.isEnum(), "not an enum");
    }
}
