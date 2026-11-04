/*
 * @test
 * @summary java.util.logging.SocketHandler conformance: white-box structure + reflection (network-bound; no sockets opened).
 * @run main SocketHandlerTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SocketHandler;
import java.util.logging.StreamHandler;

public class SocketHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.SocketHandler");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(StreamHandler.class.isAssignableFrom(c), "extends StreamHandler");
        check(c.getSuperclass() == StreamHandler.class, "direct superclass StreamHandler");
        check(Handler.class.isAssignableFrom(c), "is a Handler");
        check(c.getMethod("publish", LogRecord.class) != null, "publish(LogRecord)");
        check(c.getMethod("close") != null, "close()");
    }

    // ---- Part 2: black-box (reflection of network-bound constructor surface) ----
    // SocketHandler connects over TCP in its constructor, so we must not
    // instantiate it. We reflect on the constructor signatures only.
    static void testBlackBox() throws Exception {
        Class<?> c = SocketHandler.class;
        Constructor<?> noArg = c.getConstructor();
        check(noArg.getParameterTypes().length == 0, "no-arg constructor exists");
        check(noArg.getExceptionTypes().length == 1
                && noArg.getExceptionTypes()[0] == IOException.class,
                "no-arg constructor declares IOException");

        Constructor<?> hostPort = c.getConstructor(String.class, int.class);
        check(hostPort.getParameterTypes()[0] == String.class
                && hostPort.getParameterTypes()[1] == int.class,
                "(String,int) constructor parameter types");
        check(hostPort.getExceptionTypes().length == 1
                && hostPort.getExceptionTypes()[0] == IOException.class,
                "(String,int) constructor declares IOException");
    }


    // ===================== 契约深化：回环线路发送（ServerSocket 接收 XML 流）/默认 XMLFormatter/临时端口清理 =====================
    static void testContracts() throws Exception {
        try (java.net.ServerSocket ss = new java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress())) {
            final java.net.Socket[] accepted = new java.net.Socket[1];
            Thread acceptor = new Thread(() -> {
                try { accepted[0] = ss.accept(); } catch (java.io.IOException e) { }
            });
            acceptor.start();
            java.util.logging.SocketHandler sh = new java.util.logging.SocketHandler("127.0.0.1", ss.getLocalPort());
            try {
                check(sh.getFormatter() instanceof java.util.logging.XMLFormatter, "default XMLFormatter");
                check(sh.getEncoding() == null, "default encoding null");
                sh.publish(new java.util.logging.LogRecord(java.util.logging.Level.SEVERE, "over-wire"));
                sh.flush();
                acceptor.join(5000);
                check(accepted[0] != null, "connection accepted on loopback");
                if (accepted[0] != null) {
                    accepted[0].setSoTimeout(2000);
                    StringBuilder wire = new StringBuilder();
                    try {
                        java.io.InputStream in = accepted[0].getInputStream();
                        byte[] buf = new byte[4096];
                        int n;
                        while ((n = in.read(buf)) > 0) wire.append(new String(buf, 0, n, "UTF-8"));
                    } catch (java.net.SocketTimeoutException expected) { }
                    check(wire.toString().contains("over-wire"), "record traversed the wire: " + wire.length() + " bytes");
                    accepted[0].close();
                }
            } finally {
                sh.close();
            }
        }
        // 主机名 null -> IllegalArgumentException（"Null host name"）
        try {
            new java.util.logging.SocketHandler(null, 1);
            check(false, "SocketHandler(null host) IAE");
        } catch (IllegalArgumentException e) { check(true, "SocketHandler(null host) IAE"); }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
