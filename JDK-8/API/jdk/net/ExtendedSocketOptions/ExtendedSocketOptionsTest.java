/*
 * @test
 * @summary jdk.net.ExtendedSocketOptions conformance: extended socket options.
 * @run main ExtendedSocketOptionsTest
 */

public class ExtendedSocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // Runtime capability probe: the supported-options set decides whether
    // set/get must round-trip or must be rejected with
    // UnsupportedOperationException. Never hard-code a platform's option set.
    static void probeIntOption(java.net.Socket s, String label,
            java.net.SocketOption<Integer> opt, int value) throws Exception {
        if (jdk.net.Sockets.supportedOptions(java.net.Socket.class).contains(opt)) {
            jdk.net.Sockets.setOption(s, opt, value);
            check(Integer.valueOf(value).equals(jdk.net.Sockets.getOption(s, opt)),
                    label + " supported: set/get round-trip " + value);
        } else {
            try {
                jdk.net.Sockets.setOption(s, opt, value);
                check(false, label + " unsupported: set must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check(label.equals(e.getMessage()), label + " unsupported: set UOE message");
            }
            try {
                jdk.net.Sockets.getOption(s, opt);
                check(false, label + " unsupported: get must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check(label.equals(e.getMessage()), label + " unsupported: get UOE message");
            }
        }
    }

    static void probeFlowSla(java.net.Socket s) throws Exception {
        if (jdk.net.Sockets.supportedOptions(java.net.Socket.class).contains(
                jdk.net.ExtendedSocketOptions.SO_FLOW_SLA)) {
            jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.SO_FLOW_SLA,
                    jdk.net.SocketFlow.create().priority(1).bandwidth(1024));
            Object flow = jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.SO_FLOW_SLA);
            check(flow instanceof jdk.net.SocketFlow,
                    "SO_FLOW_SLA supported: set/get returns a SocketFlow");
        } else {
            try {
                jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.SO_FLOW_SLA,
                        jdk.net.SocketFlow.create().priority(1).bandwidth(1024));
                check(false, "SO_FLOW_SLA unsupported: set must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check("SO_FLOW_SLA".equals(e.getMessage()), "SO_FLOW_SLA unsupported: set UOE message");
            }
            try {
                jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.SO_FLOW_SLA);
                check(false, "SO_FLOW_SLA unsupported: get must throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                check("SO_FLOW_SLA".equals(e.getMessage()), "SO_FLOW_SLA unsupported: get UOE message");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        check(jdk.net.ExtendedSocketOptions.SO_FLOW_SLA != null, "SO_FLOW_SLA");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE != null, "TCP_KEEPIDLE");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL != null, "TCP_KEEPINTERVAL");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT != null, "TCP_KEEPCOUNT");
        check(!jdk.net.ExtendedSocketOptions.SO_FLOW_SLA.equals(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE), "distinct options");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 深化(2026-11-05)：常量面反射全表 + 真实 set/get 闭环 + 异常面（均经 JDK8u372 Linux 实测）----
    static void deepen() throws Exception {
        // 1. 反射枚举 public static SocketOption 常量全集（JDK-8 恰好 4 个）
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Field f : jdk.net.ExtendedSocketOptions.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(m) && java.lang.reflect.Modifier.isStatic(m)
                    && java.net.SocketOption.class.isAssignableFrom(f.getType())) {
                names.add(f.getName());
            }
        }
        check(names.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                "SO_FLOW_SLA", "TCP_KEEPIDLE", "TCP_KEEPINTERVAL", "TCP_KEEPCOUNT"))),
                "exact public SocketOption constant set: " + names);
        // 2. 每个常量 name()/type() 精确断言（name 等于字段名是 SocketOption 契约）
        java.net.SocketOption<?>[] all = {
            jdk.net.ExtendedSocketOptions.SO_FLOW_SLA,
            jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE,
            jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL,
            jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT };
        String[] n = {"SO_FLOW_SLA", "TCP_KEEPIDLE", "TCP_KEEPINTERVAL", "TCP_KEEPCOUNT"};
        Class<?>[] t = { jdk.net.SocketFlow.class, Integer.class, Integer.class, Integer.class };
        for (int i = 0; i < all.length; i++) {
            check(n[i].equals(all[i].name()), n[i] + " name()");
            check(t[i] == all[i].type(), n[i] + " type()");
        }
        // 3. 两两互异（4 选 2 全对）+ hashCode 两次调用稳定
        for (int i = 0; i < all.length; i++) {
            check(all[i].hashCode() == all[i].hashCode(), n[i] + " hashCode stable");
            for (int j = i + 1; j < all.length; j++)
                check(!all[i].equals(all[j]), n[i] + " != " + n[j]);
        }
        check(!all[0].equals(null), "option not equals null");
        check(!all[0].equals("SO_FLOW_SLA"), "option not equals String");
        // 4. 类结构：final、包私有构造（不可实例化）
        check(java.lang.reflect.Modifier.isFinal(jdk.net.ExtendedSocketOptions.class.getModifiers()), "class final");
        check(jdk.net.ExtendedSocketOptions.class.getConstructors().length == 0, "no public constructors");
        // 5. supportedOptions 是运行时能力的权威来源（平台相关：Linux 有
        //    TCP_KEEP*、不支持 SO_FLOW_SLA；Windows 上则相反）。集合内选项
        //    必须 set/get 闭环，集合外选项必须抛 UnsupportedOperationException。
        java.util.Set<java.net.SocketOption<?>> sk = jdk.net.Sockets.supportedOptions(java.net.Socket.class);
        java.net.Socket s = new java.net.Socket();
        try {
            probeIntOption(s, "TCP_KEEPIDLE", jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, 100);
            probeIntOption(s, "TCP_KEEPINTERVAL", jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL, 50);
            probeIntOption(s, "TCP_KEEPCOUNT", jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT, 8);
            probeFlowSla(s);
            if (sk.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE)) {
                check(jdk.net.Sockets.supportedOptions(java.net.ServerSocket.class).contains(
                        jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE),
                        "ServerSocket supports TCP_KEEPIDLE where Socket does");
            }
            check(!jdk.net.Sockets.supportedOptions(java.net.DatagramSocket.class).contains(
                    jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE), "DatagramSocket lacks TCP_KEEPIDLE");
            // 6. 内核级非法值（仅在 TCP_KEEPIDLE 受支持时才有意义）
            if (sk.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE)) {
                try { jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, 0); check(false, "KEEPIDLE=0 should throw"); }
                catch (java.net.SocketException e) { check(true, "KEEPIDLE=0 SocketException"); }
                if (java.io.File.separatorChar == '\\') {
                    // Windows 内核把负值夹到 uint32 上限（探针：-5 → 4294967 秒），不返回 EINVAL；
                    // 只有 POSIX 内核才拒绝负值，因此这里断言钳制后的值仍为正。
                    jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, -5);
                    Object clamped = jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE);
                    check(clamped instanceof Integer && (Integer) clamped > 0,
                          "negative KEEPIDLE clamped to a positive value on Windows");
                } else {
                    try { jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, -5); check(false, "KEEPIDLE=-5 should throw"); }
                    catch (java.net.SocketException e) { check(true, "KEEPIDLE=-5 SocketException"); }
                }
            }
            // 9. 未知选项（自定义 SocketOption）→ UnsupportedOperationException(选项name)
            final java.net.SocketOption<Object> fake = new java.net.SocketOption<Object>() {
                public String name() { return "FAKE_OPT"; }
                public Class<Object> type() { return Object.class; }
            };
            try { jdk.net.Sockets.setOption(s, fake, "x"); check(false, "fake option should throw"); }
            catch (UnsupportedOperationException e) { check("FAKE_OPT".equals(e.getMessage()), "fake UOE message"); }
            // 10. SO_RCVBUF 值域：0/坏类型 → SocketException（JDK-8 无泛型值类型检查，直接落到内核）
            jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_RCVBUF, 65536);
            check(Integer.valueOf(65536).equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_RCVBUF)), "SO_RCVBUF round-trip 65536");
            try { jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_RCVBUF, 0); check(false, "SO_RCVBUF=0 should throw"); }
            catch (java.net.SocketException e) { check(e.getMessage() != null && e.getMessage().contains("bad parameter"), "SO_RCVBUF=0 SocketException bad parameter"); }
            java.lang.reflect.Method setM = jdk.net.Sockets.class.getMethod("setOption",
                    java.net.Socket.class, java.net.SocketOption.class, Object.class);
            try { setM.invoke(null, s, java.net.StandardSocketOptions.SO_RCVBUF, "notanint"); check(false, "bad type should throw"); }
            catch (java.lang.reflect.InvocationTargetException e) {
                check(e.getCause() instanceof java.net.SocketException, "bad type value -> SocketException on JDK-8");
            }
            // 11. 已关闭 socket → SocketException
        } finally {
            s.close();
        }
        try { jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, 100); check(false, "closed set should throw"); }
        catch (java.net.SocketException e) { check("Socket closed".equals(e.getMessage()), "closed set SocketException 'Socket closed' (extended path, lowercase)"); }
        try { jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE); check(false, "closed get should throw"); }
        catch (java.net.SocketException e) { check(true, "closed get SocketException"); }
        // 12. supportedOptions 返回集不可变、稳定
        java.util.Set<java.net.SocketOption<?>> set1 = jdk.net.Sockets.supportedOptions(java.net.Socket.class);
        try { set1.add(java.net.StandardSocketOptions.SO_SNDBUF); check(false, "set should be immutable"); }
        catch (UnsupportedOperationException e) { check(true, "supported set immutable"); }
        check(set1.equals(jdk.net.Sockets.supportedOptions(java.net.Socket.class)), "supported set stable across calls");
        try { jdk.net.Sockets.supportedOptions(String.class); check(false, "unknown type should throw"); }
        catch (IllegalArgumentException e) { check("unknown socket type".equals(e.getMessage()), "unknown type IAE message"); }
    }
}
