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

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
