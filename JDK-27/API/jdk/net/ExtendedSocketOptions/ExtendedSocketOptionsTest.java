/*
 * @test
 * @summary jdk.net.ExtendedSocketOptions conformance: extended socket options.
 * @run main ExtendedSocketOptionsTest
 */

public class ExtendedSocketOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(jdk.net.ExtendedSocketOptions.TCP_QUICKACK != null, "TCP_QUICKACK");
        check("TCP_QUICKACK".equals(jdk.net.ExtendedSocketOptions.TCP_QUICKACK.name()), "TCP_QUICKACK name");
        check(jdk.net.ExtendedSocketOptions.TCP_QUICKACK.type() == java.lang.Boolean.class, "TCP_QUICKACK type");
        check(jdk.net.ExtendedSocketOptions.SO_PEERCRED != null, "SO_PEERCRED");
        check(jdk.net.ExtendedSocketOptions.SO_PEERCRED.type() == jdk.net.UnixDomainPrincipal.class, "SO_PEERCRED type");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE != null, "TCP_KEEPIDLE");
        check(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE.type() == java.lang.Integer.class, "TCP_KEEPIDLE type");
        check(jdk.net.ExtendedSocketOptions.IP_DONTFRAGMENT != null, "IP_DONTFRAGMENT");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 深化(2026-11-05)：7 常量反射全表 + 支持面 + 真实闭环 + 只读选项异常面（经 JDK27.0.1 Linux 实测）----
    static void deepen() throws Exception {
        // 1. 反射枚举 public static SocketOption 常量全集（JDK-27 恰好 7 个，无 SO_FLOW_SLA）
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Field f : jdk.net.ExtendedSocketOptions.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(m) && java.lang.reflect.Modifier.isStatic(m)
                    && java.net.SocketOption.class.isAssignableFrom(f.getType())) {
                names.add(f.getName());
            }
        }
        check(names.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                "TCP_QUICKACK", "TCP_KEEPIDLE", "TCP_KEEPINTERVAL", "TCP_KEEPCOUNT",
                "SO_INCOMING_NAPI_ID", "SO_PEERCRED", "IP_DONTFRAGMENT"))),
                "exact public SocketOption constant set: " + names);
        // 2. 逐常量 name()/type() 精确断言（JDK-27 新增常量逐个点名）
        java.net.SocketOption<?>[] all = {
            jdk.net.ExtendedSocketOptions.TCP_QUICKACK,
            jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE,
            jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL,
            jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT,
            jdk.net.ExtendedSocketOptions.SO_INCOMING_NAPI_ID,
            jdk.net.ExtendedSocketOptions.SO_PEERCRED,
            jdk.net.ExtendedSocketOptions.IP_DONTFRAGMENT };
        String[] n = {"TCP_QUICKACK", "TCP_KEEPIDLE", "TCP_KEEPINTERVAL", "TCP_KEEPCOUNT",
                "SO_INCOMING_NAPI_ID", "SO_PEERCRED", "IP_DONTFRAGMENT"};
        Class<?>[] t = { Boolean.class, Integer.class, Integer.class, Integer.class,
                Integer.class, jdk.net.UnixDomainPrincipal.class, Boolean.class };
        for (int i = 0; i < all.length; i++) {
            check(n[i].equals(all[i].name()), n[i] + " name()");
            check(t[i] == all[i].type(), n[i] + " type()");
        }
        // 3. 两两互异 + hashCode 稳定
        for (int i = 0; i < all.length; i++) {
            check(all[i].hashCode() == all[i].hashCode(), n[i] + " hashCode stable");
            for (int j = i + 1; j < all.length; j++)
                check(!all[i].equals(all[j]), n[i] + " != " + n[j]);
        }
        check(!all[0].equals(null), "not equals null");
        check(!all[0].equals("TCP_QUICKACK"), "not equals String");
        // 4. JDK-8 差集：SO_FLOW_SLA 字段已删除、SocketFlow 类已删除
        try { jdk.net.ExtendedSocketOptions.class.getDeclaredField("SO_FLOW_SLA"); check(false, "SO_FLOW_SLA field should be gone"); }
        catch (NoSuchFieldException e) { check(true, "no SO_FLOW_SLA field"); }
        try { Class.forName("jdk.net.SocketFlow"); check(false, "SocketFlow should be gone"); }
        catch (ClassNotFoundException e) { check(true, "no SocketFlow class"); }
        // 5. 类结构：final、不可实例化
        check(java.lang.reflect.Modifier.isFinal(jdk.net.ExtendedSocketOptions.class.getModifiers()), "class final");
        check(jdk.net.ExtendedSocketOptions.class.getConstructors().length == 0, "no public constructors");
        // 6. supportedOptions 支持面（Linux：SO_PEERCRED/IP_DONTFRAGMENT 不在 Socket 集）
        java.util.Set<java.net.SocketOption<?>> sk = jdk.net.Sockets.supportedOptions(java.net.Socket.class);
        check(sk.contains(jdk.net.ExtendedSocketOptions.TCP_QUICKACK), "Socket supports TCP_QUICKACK");
        check(sk.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE), "Socket supports TCP_KEEPIDLE");
        check(sk.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL), "Socket supports TCP_KEEPINTERVAL");
        check(sk.contains(jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT), "Socket supports TCP_KEEPCOUNT");
        check(sk.contains(jdk.net.ExtendedSocketOptions.SO_INCOMING_NAPI_ID), "Socket supports SO_INCOMING_NAPI_ID");
        check(sk.contains(java.net.StandardSocketOptions.SO_REUSEPORT), "Socket supports SO_REUSEPORT");
        check(!sk.contains(jdk.net.ExtendedSocketOptions.SO_PEERCRED), "Socket set lacks SO_PEERCRED (unix-domain only)");
        check(!sk.contains(jdk.net.ExtendedSocketOptions.IP_DONTFRAGMENT), "Socket set lacks IP_DONTFRAGMENT on Linux");
        check(jdk.net.Sockets.supportedOptions(java.net.ServerSocket.class).contains(
                jdk.net.ExtendedSocketOptions.TCP_QUICKACK), "ServerSocket supports TCP_QUICKACK");
        check(!jdk.net.Sockets.supportedOptions(java.net.DatagramSocket.class).contains(
                jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE), "DatagramSocket lacks TCP_KEEPIDLE");
        // 7. 真实闭环：未连接 Socket 上 set/get
        java.net.Socket s = new java.net.Socket();
        try {
            jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, 100);
            check(Integer.valueOf(100).equals(jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE)), "TCP_KEEPIDLE round-trip 100");
            jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL, 50);
            check(Integer.valueOf(50).equals(jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPINTERVAL)), "TCP_KEEPINTERVAL round-trip 50");
            jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT, 8);
            check(Integer.valueOf(8).equals(jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPCOUNT)), "TCP_KEEPCOUNT round-trip 8");
            jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_QUICKACK, Boolean.FALSE);
            check(Boolean.FALSE.equals(jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_QUICKACK)), "TCP_QUICKACK false round-trip");
            jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_QUICKACK, Boolean.TRUE);
            check(Boolean.TRUE.equals(jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_QUICKACK)), "TCP_QUICKACK true round-trip");
            check(jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE) instanceof Integer, "KEEPIDLE value Integer");
            check(jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_QUICKACK) instanceof Boolean, "QUICKACK value Boolean");
            // 8. SO_INCOMING_NAPI_ID：只读——get 可用(未连接回 0)，set → SocketException("Attempt to set read only option")
            Integer napi = jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.SO_INCOMING_NAPI_ID);
            check(napi != null && napi.intValue() >= 0, "SO_INCOMING_NAPI_ID get >= 0: " + napi);
            try { jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.SO_INCOMING_NAPI_ID, 5); check(false, "NAPI set should throw"); }
            catch (java.net.SocketException e) { check(e.getMessage() != null && e.getMessage().contains("read only"), "NAPI set read-only SocketException"); }
            // 9. SO_PEERCRED / IP_DONTFRAGMENT 在 AF_INET socket 上 → UnsupportedOperationException
            try { jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.SO_PEERCRED); check(false, "SO_PEERCRED get should UOE on inet socket"); }
            catch (UnsupportedOperationException e) { check(e.getMessage() != null && e.getMessage().contains("SO_PEERCRED"), "SO_PEERCRED UOE message"); }
            try { jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.IP_DONTFRAGMENT, Boolean.TRUE); check(false, "IP_DONTFRAGMENT set should UOE"); }
            catch (UnsupportedOperationException e) { check(e.getMessage() != null && e.getMessage().contains("IP_DONTFRAGMENT"), "IP_DONTFRAGMENT UOE message"); }
            // 10. 内核级非法值：TCP_KEEPIDLE 0/-5 → SocketException("Invalid argument")
            try { jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, 0); check(false, "KEEPIDLE=0 should throw"); }
            catch (java.net.SocketException e) { check(true, "KEEPIDLE=0 SocketException"); }
            try { jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, -5); check(false, "KEEPIDLE=-5 should throw"); }
            catch (java.net.SocketException e) { check(true, "KEEPIDLE=-5 SocketException"); }
            // 11. 值类型检查（JDK-27 独有）：错型 → IllegalArgumentException("Invalid value ...")
            java.lang.reflect.Method setM = jdk.net.Sockets.class.getMethod("setOption",
                    java.net.Socket.class, java.net.SocketOption.class, Object.class);
            try { setM.invoke(null, s, java.net.StandardSocketOptions.SO_RCVBUF, "notanint"); check(false, "bad type should throw"); }
            catch (java.lang.reflect.InvocationTargetException e) {
                check(e.getCause() instanceof IllegalArgumentException, "bad type value -> IAE on JDK-27");
            }
            // 12. SO_RCVBUF=0 被接受（JDK-8 抛异常——差异点），随后回读为内核最小值
            try { jdk.net.Sockets.setOption(s, java.net.StandardSocketOptions.SO_RCVBUF, 0); check(true, "SO_RCVBUF=0 accepted on JDK-27"); }
            catch (java.net.SocketException e) { check(false, "SO_RCVBUF=0 should be accepted"); }
            check(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_RCVBUF) instanceof Integer, "RCVBUF after 0 still Integer");
            // 13. 未知选项 → UnsupportedOperationException
            final java.net.SocketOption<Object> fake = new java.net.SocketOption<Object>() {
                public String name() { return "FAKE_OPT"; }
                public Class<Object> type() { return Object.class; }
            };
            try { jdk.net.Sockets.setOption(s, fake, "x"); check(false, "fake option should throw"); }
            catch (UnsupportedOperationException e) { check(true, "fake UOE"); }
            // 14. java.net.Socket 原生 setOption/getOption（JDK-9+ 方法面，与 Sockets 通道互通）
            s.setOption(java.net.StandardSocketOptions.SO_RCVBUF, 16384);
            check(Integer.valueOf(16384).equals(s.getOption(java.net.StandardSocketOptions.SO_RCVBUF)), "Socket.setOption native round-trip");
            check(Integer.valueOf(16384).equals(jdk.net.Sockets.getOption(s, java.net.StandardSocketOptions.SO_RCVBUF)), "native set readable via Sockets.getOption");
            jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, 111);
            check(Integer.valueOf(111).equals(s.getOption(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE)), "Sockets set readable via native getOption");
        } finally {
            s.close();
        }
        // 15. 已关闭 socket → SocketException("Socket is closed")
        try { jdk.net.Sockets.setOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE, 100); check(false, "closed set should throw"); }
        catch (java.net.SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed set 'Socket is closed'"); }
        try { jdk.net.Sockets.getOption(s, jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE); check(false, "closed get should throw"); }
        catch (java.net.SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed get 'Socket is closed'"); }
        try { s.getOption(jdk.net.ExtendedSocketOptions.TCP_KEEPIDLE); check(false, "native closed get should throw"); }
        catch (java.net.SocketException e) { check(true, "native closed get SocketException"); }
        // 16. supportedOptions 集不可变、稳定、未知类型 IAE
        try { sk.add(java.net.StandardSocketOptions.SO_SNDBUF); check(false, "set should be immutable"); }
        catch (UnsupportedOperationException e) { check(true, "supported set immutable"); }
        check(sk.equals(jdk.net.Sockets.supportedOptions(java.net.Socket.class)), "supported set stable across calls");
        try { jdk.net.Sockets.supportedOptions(String.class); check(false, "unknown type should throw"); }
        catch (IllegalArgumentException e) { check("unknown socket type".equals(e.getMessage()), "unknown type IAE message"); }
    }
}
