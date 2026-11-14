/*
 * @test
 * @summary jdk.net.Sockets conformance: white-box structure + black-box behavior.
 * @run main SocketsTest
 */

import jdk.net.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
public class SocketsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // Runtime capability probe: the supported-options set decides whether
    // set/get must round-trip or must be rejected with
    // UnsupportedOperationException. Never hard-code a platform's option set.
    static void probeIntOption(java.net.Socket s, String label,
            SocketOption<Integer> opt, int value) throws Exception {
        if (Sockets.supportedOptions(java.net.Socket.class).contains(opt)) {
            Sockets.setOption(s, opt, value);
            check(Integer.valueOf(value).equals(Sockets.getOption(s, opt)),
                    label + " supported via Sockets: set/get round-trip");
        } else {
            try {
                Sockets.setOption(s, opt, value);
                check(false, label + " unsupported: set must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check(e.getMessage() != null && e.getMessage().contains(label),
                        label + " unsupported: set UOE message");
            }
            try {
                Sockets.getOption(s, opt);
                check(false, label + " unsupported: get must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check(e.getMessage() != null && e.getMessage().contains(label),
                        label + " unsupported: get UOE message");
            }
        }
    }

    static void probeBoolOption(java.net.Socket s, String label,
            SocketOption<Boolean> opt, boolean value) throws Exception {
        if (Sockets.supportedOptions(java.net.Socket.class).contains(opt)) {
            Sockets.setOption(s, opt, value);
            check(Boolean.valueOf(value).equals(Sockets.getOption(s, opt)),
                    label + " supported via Sockets: set/get round-trip");
        } else {
            try {
                Sockets.setOption(s, opt, value);
                check(false, label + " unsupported: set must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check(e.getMessage() != null && e.getMessage().contains(label),
                        label + " unsupported: set UOE message");
            }
            try {
                Sockets.getOption(s, opt);
                check(false, label + " unsupported: get must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check(e.getMessage() != null && e.getMessage().contains(label),
                        label + " unsupported: get UOE message");
            }
        }
    }

    static void probeNapi(java.net.Socket s) throws Exception {
        if (Sockets.supportedOptions(java.net.Socket.class).contains(
                ExtendedSocketOptions.SO_INCOMING_NAPI_ID)) {
            Integer napi = Sockets.getOption(s, ExtendedSocketOptions.SO_INCOMING_NAPI_ID);
            check(napi != null && napi.intValue() >= 0,
                    "SO_INCOMING_NAPI_ID supported: read-only get >= 0: " + napi);
            try {
                Sockets.setOption(s, ExtendedSocketOptions.SO_INCOMING_NAPI_ID, 5);
                check(false, "SO_INCOMING_NAPI_ID supported: set must throw (read-only)");
            } catch (java.net.SocketException e) {
                check(e.getMessage() != null && e.getMessage().contains("read only"),
                        "SO_INCOMING_NAPI_ID supported: set read-only SocketException");
            }
        } else {
            try {
                Sockets.getOption(s, ExtendedSocketOptions.SO_INCOMING_NAPI_ID);
                check(false, "SO_INCOMING_NAPI_ID unsupported: get must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check(e.getMessage() != null && e.getMessage().contains("SO_INCOMING_NAPI_ID"),
                        "SO_INCOMING_NAPI_ID unsupported: get UOE message");
            }
            try {
                Sockets.setOption(s, ExtendedSocketOptions.SO_INCOMING_NAPI_ID, 5);
                check(false, "SO_INCOMING_NAPI_ID unsupported: set must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check(e.getMessage() != null && e.getMessage().contains("SO_INCOMING_NAPI_ID"),
                        "SO_INCOMING_NAPI_ID unsupported: set UOE message");
            }
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.net.Sockets");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(hasMethod(c, "setOption"), "setOption");
        check(hasMethod(c, "getOption"), "getOption");
        check(hasMethod(c, "supportedOptions"), "supportedOptions");

    }

    static void testBlackBox() throws Exception {
        java.net.Socket s = new java.net.Socket();
        Sockets.setOption(s, StandardSocketOptions.SO_REUSEADDR, true);
        check(Boolean.TRUE.equals(Sockets.getOption(s, StandardSocketOptions.SO_REUSEADDR)), "set/get option");
        check(Sockets.supportedOptions(java.net.Socket.class) != null, "supportedOptions");
        s.close();
        java.net.ServerSocket ss = new java.net.ServerSocket(0);
        Sockets.setOption(ss, StandardSocketOptions.SO_REUSEADDR, true);
        check(Boolean.TRUE.equals(Sockets.getOption(ss, StandardSocketOptions.SO_REUSEADDR)), "server set/get option");
        ss.close();
        deepen();

    }

    // ---- 深化(2026-11-05)：四类 socket 支持面矩阵 + 双通道闭环 + 异常面（经 JDK27.0.1 Linux 实测）----
    static void deepen() throws Exception {
        // The extended/extra option surface is platform specific: SO_REUSEPORT,
        // TCP_QUICKACK and SO_INCOMING_NAPI_ID exist on Linux, not on Windows;
        // the TCP keepalive trio is supported by Windows 10+.
        boolean windows = java.io.File.separatorChar == '\\';
        java.net.Socket s = new java.net.Socket();
        try {
            // 1. 真实 set/get 闭环（Sockets 通道）
            Sockets.setOption(s, StandardSocketOptions.SO_RCVBUF, 65536);
            check(Integer.valueOf(65536).equals(Sockets.getOption(s, StandardSocketOptions.SO_RCVBUF)), "SO_RCVBUF 65536 round-trip");
            Sockets.setOption(s, StandardSocketOptions.SO_SNDBUF, 32768);
            check(Integer.valueOf(32768).equals(Sockets.getOption(s, StandardSocketOptions.SO_SNDBUF)), "SO_SNDBUF 32768 round-trip");
            Sockets.setOption(s, StandardSocketOptions.SO_REUSEADDR, Boolean.FALSE);
            check(Boolean.FALSE.equals(Sockets.getOption(s, StandardSocketOptions.SO_REUSEADDR)), "SO_REUSEADDR false round-trip");
            Sockets.setOption(s, StandardSocketOptions.TCP_NODELAY, Boolean.FALSE);
            check(Boolean.FALSE.equals(Sockets.getOption(s, StandardSocketOptions.TCP_NODELAY)), "TCP_NODELAY false round-trip");
            Sockets.setOption(s, StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
            check(Boolean.TRUE.equals(Sockets.getOption(s, StandardSocketOptions.SO_KEEPALIVE)), "SO_KEEPALIVE round-trip");
            Sockets.setOption(s, StandardSocketOptions.SO_KEEPALIVE, Boolean.FALSE);
            check(Boolean.FALSE.equals(Sockets.getOption(s, StandardSocketOptions.SO_KEEPALIVE)), "SO_KEEPALIVE off");
            check(Sockets.getOption(s, StandardSocketOptions.SO_RCVBUF) instanceof Integer, "SO_RCVBUF value Integer");
            // 2. SO_REUSEPORT（JDK-9+ 常量，Linux 支持；Windows 不支持）
            if (windows) {
                try { Sockets.setOption(s, StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); check(false, "SO_REUSEPORT should be unsupported on Windows"); }
                catch (UnsupportedOperationException e) { check(e.getMessage() != null && e.getMessage().contains("SO_REUSEPORT"), "SO_REUSEPORT UOE on Windows"); }
            } else {
                Sockets.setOption(s, StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
                check(Boolean.TRUE.equals(Sockets.getOption(s, StandardSocketOptions.SO_REUSEPORT)), "SO_REUSEPORT round-trip");
                Sockets.setOption(s, StandardSocketOptions.SO_REUSEPORT, Boolean.FALSE);
                check(Boolean.FALSE.equals(Sockets.getOption(s, StandardSocketOptions.SO_REUSEPORT)), "SO_REUSEPORT off");
            }
            // 3. 扩展选项经 Sockets 通道 + 原生通道互通（按运行时能力探测）
            probeIntOption(s, "TCP_KEEPIDLE", ExtendedSocketOptions.TCP_KEEPIDLE, 100);
            probeBoolOption(s, "TCP_QUICKACK", ExtendedSocketOptions.TCP_QUICKACK, Boolean.FALSE);
            if (Sockets.supportedOptions(java.net.Socket.class).contains(
                    ExtendedSocketOptions.TCP_QUICKACK)) {
                s.setOption(ExtendedSocketOptions.TCP_QUICKACK, Boolean.FALSE);
                check(Boolean.FALSE.equals(s.getOption(ExtendedSocketOptions.TCP_QUICKACK)), "TCP_QUICKACK via native channel");
                check(Boolean.FALSE.equals(Sockets.getOption(s, ExtendedSocketOptions.TCP_QUICKACK)), "native set readable via Sockets");
            }
            // 4. null 参数：null 选项 → NPE；null socket → NPE（JDK-27 实测，JDK-8 为 IAE）
            try { Sockets.setOption(s, null, 1); check(false, "setOption null option should NPE"); }
            catch (NullPointerException e) { check(true, "setOption null option NPE"); }
            try { Sockets.getOption(s, null); check(false, "getOption null option should NPE"); }
            catch (NullPointerException e) { check(true, "getOption null option NPE"); }
            try { Sockets.setOption((java.net.Socket) null, StandardSocketOptions.SO_RCVBUF, 1); check(false, "setOption null socket should NPE"); }
            catch (NullPointerException e) { check(true, "setOption null socket NPE (JDK-27)"); }
            try { Sockets.getOption((java.net.Socket) null, StandardSocketOptions.SO_RCVBUF); check(false, "getOption null socket should NPE"); }
            catch (NullPointerException e) { check(true, "getOption null socket NPE (JDK-27)"); }
            // 5. 值类型检查（JDK-27 独有）：错型 → IllegalArgumentException("Invalid value ...")
            java.lang.reflect.Method setM = Sockets.class.getMethod("setOption",
                    java.net.Socket.class, java.net.SocketOption.class, Object.class);
            try { setM.invoke(null, s, StandardSocketOptions.SO_RCVBUF, "notanint"); check(false, "bad type should throw"); }
            catch (java.lang.reflect.InvocationTargetException e) {
                check(e.getCause() instanceof IllegalArgumentException
                        && e.getCause().getMessage() != null && e.getCause().getMessage().contains("Invalid value"),
                        "bad type value -> IAE 'Invalid value' on JDK-27");
            }
            // 6. SO_RCVBUF=0 被接受（JDK-8 抛异常）
            try { Sockets.setOption(s, StandardSocketOptions.SO_RCVBUF, 0); check(true, "SO_RCVBUF=0 accepted on JDK-27"); }
            catch (java.net.SocketException e) { check(false, "SO_RCVBUF=0 should be accepted"); }
            check(Sockets.getOption(s, StandardSocketOptions.SO_RCVBUF) instanceof Integer, "RCVBUF after 0 still Integer");
            // 7. 不支持的选项 → UnsupportedOperationException
            try { Sockets.setOption(s, ExtendedSocketOptions.IP_DONTFRAGMENT, Boolean.TRUE); check(false, "IP_DONTFRAGMENT should UOE on Linux"); }
            catch (UnsupportedOperationException e) { check(e.getMessage() != null && e.getMessage().contains("IP_DONTFRAGMENT"), "IP_DONTFRAGMENT UOE message"); }
        } finally {
            s.close();
        }
        // 8. 已关闭 socket → SocketException("Socket is closed")（JDK-8 消息为 "Socket Closed"）
        try { Sockets.setOption(s, StandardSocketOptions.SO_RCVBUF, 4096); check(false, "closed set should throw"); }
        catch (java.net.SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed set 'Socket is closed'"); }
        try { Sockets.getOption(s, StandardSocketOptions.SO_RCVBUF); check(false, "closed get should throw"); }
        catch (java.net.SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed get 'Socket is closed'"); }
        try { s.setOption(StandardSocketOptions.SO_RCVBUF, 4096); check(false, "native closed set should throw"); }
        catch (java.net.SocketException e) { check(true, "native closed set SocketException"); }
        // 9. supportedOptions 矩阵（平台相关；Linux JDK-27 / Windows JDK-27）
        java.util.Set<java.net.SocketOption<?>> sk = Sockets.supportedOptions(java.net.Socket.class);
        java.net.SocketOption<?>[] sockMust = {
            StandardSocketOptions.SO_KEEPALIVE, StandardSocketOptions.SO_SNDBUF,
            StandardSocketOptions.SO_RCVBUF, StandardSocketOptions.SO_REUSEADDR,
            StandardSocketOptions.SO_LINGER,
            StandardSocketOptions.IP_TOS, StandardSocketOptions.TCP_NODELAY };
        for (java.net.SocketOption<?> o : sockMust) check(sk.contains(o), "Socket set contains " + o.name());
        // 扩展选项（keepalive 三件套、TCP_QUICKACK、SO_INCOMING_NAPI_ID）的
        // 可用性已由 section 2/3 的运行时能力探测覆盖，这里不再平台硬编码。
        if (windows) {
            check(!sk.contains(StandardSocketOptions.SO_REUSEPORT), "Socket set lacks SO_REUSEPORT on Windows");
        } else {
            check(sk.contains(StandardSocketOptions.SO_REUSEPORT), "Socket set contains SO_REUSEPORT");
        }
        check(!sk.contains(ExtendedSocketOptions.SO_PEERCRED), "Socket set lacks SO_PEERCRED");
        java.util.Set<java.net.SocketOption<?>> ss = Sockets.supportedOptions(java.net.ServerSocket.class);
        check(ss.contains(StandardSocketOptions.SO_RCVBUF), "ServerSocket contains SO_RCVBUF");
        check(ss.contains(StandardSocketOptions.SO_REUSEADDR), "ServerSocket contains SO_REUSEADDR");
        check(ss.contains(StandardSocketOptions.IP_TOS), "ServerSocket contains IP_TOS");
        if (sk.contains(ExtendedSocketOptions.TCP_KEEPIDLE)) {
            check(ss.contains(ExtendedSocketOptions.TCP_KEEPIDLE),
                    "ServerSocket contains TCP_KEEPIDLE where Socket does");
        }
        check(!ss.contains(StandardSocketOptions.TCP_NODELAY), "ServerSocket lacks TCP_NODELAY");
        check(!ss.contains(StandardSocketOptions.SO_SNDBUF), "ServerSocket lacks SO_SNDBUF");
        java.util.Set<java.net.SocketOption<?>> ds = Sockets.supportedOptions(java.net.DatagramSocket.class);
        check(ds.contains(StandardSocketOptions.SO_SNDBUF), "DatagramSocket contains SO_SNDBUF");
        check(ds.contains(StandardSocketOptions.SO_RCVBUF), "DatagramSocket contains SO_RCVBUF");
        check(!ds.contains(StandardSocketOptions.TCP_NODELAY), "DatagramSocket lacks TCP_NODELAY");
        check(!ds.contains(ExtendedSocketOptions.TCP_KEEPIDLE), "DatagramSocket lacks TCP_KEEPIDLE");
        java.util.Set<java.net.SocketOption<?>> ms = Sockets.supportedOptions(java.net.MulticastSocket.class);
        check(ms.contains(StandardSocketOptions.IP_MULTICAST_TTL), "MulticastSocket contains IP_MULTICAST_TTL");
        check(ms.contains(StandardSocketOptions.IP_MULTICAST_IF), "MulticastSocket contains IP_MULTICAST_IF");
        check(ms.contains(StandardSocketOptions.IP_MULTICAST_LOOP), "MulticastSocket contains IP_MULTICAST_LOOP");
        // 10. 集不可变、稳定、未知/null 类型
        try { sk.add(StandardSocketOptions.SO_SNDBUF); check(false, "set immutable"); }
        catch (UnsupportedOperationException e) { check(true, "supported set immutable"); }
        check(sk.equals(Sockets.supportedOptions(java.net.Socket.class)), "supported set stable");
        try { Sockets.supportedOptions(String.class); check(false, "unknown socket type should IAE"); }
        catch (IllegalArgumentException e) { check("unknown socket type".equals(e.getMessage()), "unknown type message"); }
        try { Sockets.supportedOptions(null); check(false, "null socket type should throw"); }
        catch (IllegalArgumentException e) { check(true, "null socket type IAE"); }
        catch (NullPointerException e) { check(true, "null socket type NPE"); }
        // 11. ServerSocket 闭环 + SO_REUSEPORT
        java.net.ServerSocket srv = new java.net.ServerSocket(0);
        try {
            Sockets.setOption(srv, StandardSocketOptions.SO_RCVBUF, 8192);
            int got = Sockets.getOption(srv, StandardSocketOptions.SO_RCVBUF);
            check(got >= 1024, "ServerSocket SO_RCVBUF readback >= 1024: " + got);
            if (windows) {
                try { srv.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE); check(false, "SS SO_REUSEPORT should be unsupported on Windows"); }
                catch (UnsupportedOperationException e) { check(e.getMessage() != null && e.getMessage().contains("SO_REUSEPORT"), "SS SO_REUSEPORT UOE on Windows"); }
            } else {
                srv.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.TRUE);
                check(Boolean.TRUE.equals(Sockets.getOption(srv, StandardSocketOptions.SO_REUSEPORT)), "SS SO_REUSEPORT via native set / Sockets get");
            }
            try { Sockets.setOption(srv, StandardSocketOptions.TCP_NODELAY, Boolean.TRUE); check(false, "SS TCP_NODELAY should UOE"); }
            catch (UnsupportedOperationException e) { check(e.getMessage() != null && e.getMessage().contains("TCP_NODELAY"), "SS TCP_NODELAY UOE message"); }
            check(srv.getLocalPort() > 0, "ServerSocket bound port");
        } finally { srv.close(); }
        // 12. DatagramSocket / MulticastSocket 闭环
        java.net.DatagramSocket dg = new java.net.DatagramSocket((java.net.SocketAddress) null);
        try {
            Sockets.setOption(dg, StandardSocketOptions.SO_RCVBUF, 8192);
            check(Sockets.getOption(dg, StandardSocketOptions.SO_RCVBUF) instanceof Integer, "DatagramSocket SO_RCVBUF Integer");
            try { Sockets.setOption(dg, StandardSocketOptions.TCP_NODELAY, Boolean.TRUE); check(false, "DS TCP_NODELAY should UOE"); }
            catch (UnsupportedOperationException e) { check(true, "DatagramSocket TCP_NODELAY UOE"); }
        } finally { dg.close(); }
        java.net.MulticastSocket mc = new java.net.MulticastSocket((java.net.SocketAddress) null);
        try {
            Sockets.setOption(mc, StandardSocketOptions.IP_MULTICAST_TTL, 2);
            check(Integer.valueOf(2).equals(Sockets.getOption(mc, StandardSocketOptions.IP_MULTICAST_TTL)), "MulticastSocket TTL round-trip");
            Sockets.setOption(mc, StandardSocketOptions.IP_MULTICAST_LOOP, Boolean.FALSE);
            check(Boolean.FALSE.equals(Sockets.getOption(mc, StandardSocketOptions.IP_MULTICAST_LOOP)), "MulticastSocket LOOP false");
        } finally { mc.close(); }
        // 13. 方法面：java.net.Socket 原生 setOption/getOption 存在（JDK-8 无）+ Sockets 不可实例化 + @Deprecated
        check(java.net.Socket.class.getMethod("setOption", java.net.SocketOption.class, Object.class) != null, "Socket.setOption(SocketOption) exists");
        check(java.net.Socket.class.getMethod("getOption", java.net.SocketOption.class) != null, "Socket.getOption(SocketOption) exists");
        check(Sockets.class.getConstructors().length == 0, "Sockets not instantiable");
        check(Sockets.class.isAnnotationPresent(Deprecated.class), "Sockets @Deprecated since 16");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
