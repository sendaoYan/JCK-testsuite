/*
 * @test
 * @summary jdk.net.Sockets conformance: socket options helpers.
 * @run main SocketsTest
 */

public class SocketsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        java.net.Socket s = new java.net.Socket();
        jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.TCP_NODELAY, true);
        check(java.lang.Boolean.TRUE.equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.TCP_NODELAY)), "setOption/getOption");
        java.util.Set<java.net.SocketOption<?>> opts = jdk.net.Sockets.supportedOptions(java.net.Socket.class);
        check(opts != null, "supportedOptions");
        check(opts.contains(java.net.StandardSocketOptions.TCP_NODELAY), "contains TCP_NODELAY");
        s.close();
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 深化(2026-11-05)：四类 socket 支持面矩阵 + 真实回读闭环 + 异常面（经 JDK8u372 Linux 实测）----
    static void deepen() throws Exception {
        java.net.Socket s = new java.net.Socket();
        try {
            // 1. 真实 set/get 闭环（未连接 socket，本地内核操作）
            jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_RCVBUF, 65536);
            check(Integer.valueOf(65536).equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_RCVBUF)), "SO_RCVBUF 65536 round-trip");
            jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_SNDBUF, 32768);
            check(Integer.valueOf(32768).equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_SNDBUF)), "SO_SNDBUF 32768 round-trip");
            jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            check(Boolean.TRUE.equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_REUSEADDR)), "SO_REUSEADDR true round-trip");
            jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_REUSEADDR, Boolean.FALSE);
            check(Boolean.FALSE.equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_REUSEADDR)), "SO_REUSEADDR false round-trip");
            jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.TCP_NODELAY, Boolean.FALSE);
            check(Boolean.FALSE.equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.TCP_NODELAY)), "TCP_NODELAY false round-trip");
            jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
            check(Boolean.TRUE.equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_KEEPALIVE)), "SO_KEEPALIVE round-trip");
            jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_KEEPALIVE, Boolean.FALSE);
            check(Boolean.FALSE.equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_KEEPALIVE)), "SO_KEEPALIVE off");
            check(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_RCVBUF) instanceof Integer, "SO_RCVBUF value Integer");
            check(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.TCP_NODELAY) instanceof Boolean, "TCP_NODELAY value Boolean");
            // 2. 扩展选项经 Sockets 通道闭环
            jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, 100);
            check(Integer.valueOf(100).equals(jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE)), "TCP_KEEPIDLE via Sockets");
            // 3. null 参数：null 选项 → NPE；null socket → IllegalArgumentException（JDK-8 反射通道实测）
            try { jdk.net.Sockets.setOption(s, null, 1); check(false, "setOption null option should NPE"); }
            catch (NullPointerException e) { check(true, "setOption null option NPE"); }
            try { jdk.net.Sockets.getOption(s, null); check(false, "getOption null option should NPE"); }
            catch (NullPointerException e) { check(true, "getOption null option NPE"); }
            try { jdk.net.Sockets.setOption((java.net.Socket) null, java.net.StandardSocketOptions.SO_RCVBUF, 1); check(false, "setOption null socket"); }
            catch (IllegalArgumentException e) { check(true, "setOption null socket IAE (JDK-8)"); }
            catch (NullPointerException e) { check(true, "setOption null socket NPE"); }
            try { jdk.net.Sockets.getOption((java.net.Socket) null, java.net.StandardSocketOptions.SO_RCVBUF); check(false, "getOption null socket"); }
            catch (IllegalArgumentException e) { check(true, "getOption null socket IAE (JDK-8)"); }
            catch (NullPointerException e) { check(true, "getOption null socket NPE"); }
            // 4. 不支持的选项 → UnsupportedOperationException（选项名作消息）：IP_MULTICAST_TTL 不在 Socket 支持集
            try { jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.IP_MULTICAST_TTL, 2); check(false, "Socket IP_MULTICAST_TTL should UOE"); }
            catch (UnsupportedOperationException e) { check("IP_MULTICAST_TTL".equals(e.getMessage()), "Socket IP_MULTICAST_TTL UOE message"); }
        } finally {
            s.close();
        }
        // 5. 已关闭 socket → SocketException("Socket Closed")
        try { jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_RCVBUF, 4096); check(false, "closed set should throw"); }
        catch (java.net.SocketException e) { check("Socket Closed".equals(e.getMessage()), "closed set 'Socket Closed'"); }
        try { jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_RCVBUF); check(false, "closed get should throw"); }
        catch (java.net.SocketException e) { check("Socket Closed".equals(e.getMessage()), "closed get 'Socket Closed'"); }
        // 6. supportedOptions 四类矩阵（Linux JDK-8）
        java.util.Set<java.net.SocketOption<?>> sk = jdk.net.Sockets.supportedOptions(java.net.Socket.class);
        java.net.SocketOption<?>[] sockMust = {
            java.net.StandardSocketOptions.SO_KEEPALIVE, java.net.StandardSocketOptions.SO_SNDBUF,
            java.net.StandardSocketOptions.SO_RCVBUF, java.net.StandardSocketOptions.SO_REUSEADDR,
            java.net.StandardSocketOptions.SO_LINGER, java.net.StandardSocketOptions.IP_TOS,
            java.net.StandardSocketOptions.TCP_NODELAY };
        for (java.net.SocketOption<?> o : sockMust) check(sk.contains(o), "Socket set contains " + o.name());
        check(sk.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE)
           && sk.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL)
           && sk.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT), "Socket set contains TCP_KEEP*");
        check(!sk.contains(jdk.net.ExtendedSocketOptions.SO_FLOW_SLA), "Socket set lacks SO_FLOW_SLA on Linux");
        java.util.Set<java.net.SocketOption<?>> ss = jdk.net.Sockets.supportedOptions(java.net.ServerSocket.class);
        check(ss.contains(java.net.StandardSocketOptions.SO_RCVBUF), "ServerSocket contains SO_RCVBUF");
        check(ss.contains(java.net.StandardSocketOptions.SO_REUSEADDR), "ServerSocket contains SO_REUSEADDR");
        check(ss.contains(java.net.StandardSocketOptions.IP_TOS), "ServerSocket contains IP_TOS");
        check(ss.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE), "ServerSocket contains TCP_KEEPIDLE");
        check(!ss.contains(java.net.StandardSocketOptions.TCP_NODELAY), "ServerSocket lacks TCP_NODELAY");
        check(!ss.contains(java.net.StandardSocketOptions.SO_SNDBUF), "ServerSocket lacks SO_SNDBUF");
        java.util.Set<java.net.SocketOption<?>> ds = jdk.net.Sockets.supportedOptions(java.net.DatagramSocket.class);
        check(ds.contains(java.net.StandardSocketOptions.SO_SNDBUF), "DatagramSocket contains SO_SNDBUF");
        check(ds.contains(java.net.StandardSocketOptions.SO_RCVBUF), "DatagramSocket contains SO_RCVBUF");
        check(ds.contains(java.net.StandardSocketOptions.SO_REUSEADDR), "DatagramSocket contains SO_REUSEADDR");
        check(!ds.contains(java.net.StandardSocketOptions.TCP_NODELAY), "DatagramSocket lacks TCP_NODELAY");
        check(!ds.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE), "DatagramSocket lacks TCP_KEEPIDLE");
        java.util.Set<java.net.SocketOption<?>> ms = jdk.net.Sockets.supportedOptions(java.net.MulticastSocket.class);
        check(ms.contains(java.net.StandardSocketOptions.IP_MULTICAST_TTL), "MulticastSocket contains IP_MULTICAST_TTL");
        check(ms.contains(java.net.StandardSocketOptions.IP_MULTICAST_IF), "MulticastSocket contains IP_MULTICAST_IF");
        check(ms.contains(java.net.StandardSocketOptions.IP_MULTICAST_LOOP), "MulticastSocket contains IP_MULTICAST_LOOP");
        // 7. 集不可变、稳定、未知类型 IAE
        try { sk.add(java.net.StandardSocketOptions.SO_SNDBUF); check(false, "set immutable"); }
        catch (UnsupportedOperationException e) { check(true, "supported set immutable"); }
        check(sk.equals(jdk.net.Sockets.supportedOptions(java.net.Socket.class)), "supported set stable");
        try { jdk.net.Sockets.supportedOptions(String.class); check(false, "unknown socket type should IAE"); }
        catch (IllegalArgumentException e) { check("unknown socket type".equals(e.getMessage()), "unknown type message"); }
        try { jdk.net.Sockets.supportedOptions(null); check(false, "null socket type"); }
        catch (IllegalArgumentException e) { check(true, "null socket type IAE"); }
        catch (NullPointerException e) { check(true, "null socket type NPE"); }
        // 8. ServerSocket 真实闭环（临时端口，finally 关闭）
        java.net.ServerSocket srv = new java.net.ServerSocket(0);
        try {
            jdk.net.Sockets.setOption(srv, java.net.StandardSocketOptions.SO_RCVBUF, 8192);
            int got = jdk.net.Sockets.getOption(srv, java.net.StandardSocketOptions.SO_RCVBUF);
            check(got >= 1024, "ServerSocket SO_RCVBUF readback >= 1024: " + got);
            jdk.net.Sockets.setOption(srv, java.net.StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            check(Boolean.TRUE.equals(jdk.net.Sockets.getOption(srv, java.net.StandardSocketOptions.SO_REUSEADDR)), "ServerSocket SO_REUSEADDR");
            try { jdk.net.Sockets.setOption(srv, java.net.StandardSocketOptions.TCP_NODELAY, Boolean.TRUE); check(false, "SS TCP_NODELAY should UOE"); }
            catch (UnsupportedOperationException e) { check("TCP_NODELAY".equals(e.getMessage()), "SS TCP_NODELAY UOE message"); }
            check(srv.getLocalPort() > 0, "ServerSocket bound port");
        } finally { srv.close(); }
        // 9. DatagramSocket / MulticastSocket 真实闭环（未绑端口）
        java.net.DatagramSocket dg = new java.net.DatagramSocket((java.net.SocketAddress) null);
        try {
            jdk.net.Sockets.setOption(dg, java.net.StandardSocketOptions.SO_RCVBUF, 8192);
            check(jdk.net.Sockets.getOption(dg, java.net.StandardSocketOptions.SO_RCVBUF) instanceof Integer, "DatagramSocket SO_RCVBUF Integer");
            try { jdk.net.Sockets.setOption(dg, java.net.StandardSocketOptions.TCP_NODELAY, Boolean.TRUE); check(false, "DS TCP_NODELAY should UOE"); }
            catch (UnsupportedOperationException e) { check(true, "DatagramSocket TCP_NODELAY UOE"); }
        } finally { dg.close(); }
        java.net.MulticastSocket mc = new java.net.MulticastSocket((java.net.SocketAddress) null);
        try {
            jdk.net.Sockets.setOption(mc, java.net.StandardSocketOptions.IP_MULTICAST_TTL, 2);
            check(Integer.valueOf(2).equals(jdk.net.Sockets.getOption(mc, java.net.StandardSocketOptions.IP_MULTICAST_TTL)), "MulticastSocket TTL round-trip");
            jdk.net.Sockets.setOption(mc, java.net.StandardSocketOptions.IP_MULTICAST_LOOP, Boolean.FALSE);
            check(Boolean.FALSE.equals(jdk.net.Sockets.getOption(mc, java.net.StandardSocketOptions.IP_MULTICAST_LOOP)), "MulticastSocket LOOP false");
        } finally { mc.close(); }
        // 10. java.net.Socket 无 SocketOption 版 setOption/getOption（JDK-8 方法面差异）
        try { java.net.Socket.class.getMethod("setOption", java.net.SocketOption.class, Object.class); check(false, "Socket.setOption(SocketOption) absent on JDK-8"); }
        catch (NoSuchMethodException e) { check(true, "Socket lacks setOption(SocketOption) on JDK-8"); }
        check(jdk.net.Sockets.class.getConstructors().length == 0, "Sockets not instantiable");
    }
}
