/*
 * @test
 * @summary com.sun.nio.sctp.SctpChannel conformance: white-box structure + black-box behavior.
 * @run main SctpChannelTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;

public class SctpChannelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.sctp.SctpChannel");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.nio.channels.spi.AbstractSelectableChannel"),
              "extends AbstractSelectableChannel");
        Set<String> m = methodNames(c);
        String[] names = {"open","association","bind","bindAddress","unbindAddress","connect",
                          "isConnectionPending","finishConnect","getAllLocalAddresses","getRemoteAddresses",
                          "shutdown","getOption","setOption","supportedOptions","validOps","receive","send"};
        for (String n : names) check(m.contains(n), n);

        Method validOps = c.getDeclaredMethod("validOps");
        check(Modifier.isFinal(validOps.getModifiers()), "validOps is final");
        Method open = c.getDeclaredMethod("open");
        check(Modifier.isStatic(open.getModifiers()), "open() is static");
        Method open3 = c.getDeclaredMethod("open", java.net.SocketAddress.class, int.class, int.class);
        check(Modifier.isStatic(open3.getModifiers()), "open(SocketAddress,int,int) is static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // SCTP is a native protocol. On kernels/installations without SCTP
        // support, open() legally throws UnsupportedOperationException.
        // Both a successfully opened channel and that documented degrade path
        // are accepted; we never bind ports or exchange real SCTP traffic.
        boolean opened = false;
        try {
            com.sun.nio.sctp.SctpChannel ch = com.sun.nio.sctp.SctpChannel.open();
            check(ch != null, "open() returns non-null");
            check(ch.isOpen(), "open channel isOpen");
            check(ch.isBlocking(), "default blocking mode");
            check(ch.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT),
                  "validOps OP_READ|OP_WRITE|OP_CONNECT");
            check(ch.supportedOptions() != null, "supportedOptions non-null");
            ch.close();
            check(!ch.isOpen(), "channel closed");
            opened = true;
        } catch (UnsupportedOperationException degrade) {
            check(true, "SCTP not supported: open() throws UnsupportedOperationException");
        } catch (IOException ioe) {
            throw new AssertionError("unexpected IOException from open()", ioe);
        }

        // A second attempt behaves deterministically like the first.
        try {
            com.sun.nio.sctp.SctpChannel ch = com.sun.nio.sctp.SctpChannel.open();
            ch.close();
            check(opened, "repeat open consistent (success)");
        } catch (UnsupportedOperationException degrade) {
            check(!opened, "repeat open consistent (unsupported)");
        } catch (IOException ioe) {
            throw new AssertionError("unexpected IOException from repeat open()", ioe);
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
