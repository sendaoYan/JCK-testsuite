/*
 * @test
 * @summary com.sun.nio.sctp.MessageInfo conformance: white-box structure + black-box behavior.
 * @run main MessageInfoTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class MessageInfoTest {
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
        Class<?> c = load("com.sun.nio.sctp.MessageInfo");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");

        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected no-arg constructor");

        Set<String> m = methodNames(c);
        String[] names = {"createOutgoing","address","association","bytes","isComplete","complete",
                          "isUnordered","unordered","payloadProtocolID","streamNumber","timeToLive"};
        for (String n : names) check(m.contains(n), n);

        // both static factories exist
        c.getDeclaredMethod("createOutgoing", SocketAddress.class, int.class);
        c.getDeclaredMethod("createOutgoing", com.sun.nio.sctp.Association.class, SocketAddress.class, int.class);
        check(true, "both createOutgoing overloads present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SocketAddress addr = new InetSocketAddress("127.0.0.1", 12345);

        // createOutgoing(SocketAddress, int): defaults per spec
        com.sun.nio.sctp.MessageInfo mi = com.sun.nio.sctp.MessageInfo.createOutgoing(addr, 5);
        check(mi.address().equals(addr), "address equals");
        check(mi.association() == null, "association null for 2-arg factory");
        check(mi.streamNumber() == 5, "streamNumber 5");
        check(mi.bytes() == 0, "bytes default 0");
        check(mi.isComplete(), "isComplete default true");
        check(!mi.isUnordered(), "isUnordered default false");
        check(mi.payloadProtocolID() == 0, "payloadProtocolID default 0");
        check(mi.timeToLive() == 0L, "timeToLive default 0L");

        // fluent setters return this and update state
        check(mi.complete(false) == mi, "complete returns this");
        check(!mi.isComplete(), "complete(false) reflected");
        check(mi.unordered(true) == mi, "unordered returns this");
        check(mi.isUnordered(), "unordered(true) reflected");
        check(mi.payloadProtocolID(7) == mi, "payloadProtocolID returns this");
        check(mi.payloadProtocolID() == 7, "payloadProtocolID 7");
        check(mi.streamNumber(9) == mi, "streamNumber returns this");
        check(mi.streamNumber() == 9, "streamNumber 9");
        check(mi.timeToLive(1000L) == mi, "timeToLive returns this");
        check(mi.timeToLive() == 1000L, "timeToLive 1000");
        check(mi.complete(true).isComplete(), "complete(true) re-enables");

        // 3-arg factory: carries the association
        com.sun.nio.sctp.Association assoc = new com.sun.nio.sctp.Association(1, 2, 3) {};
        mi = com.sun.nio.sctp.MessageInfo.createOutgoing(assoc, addr, 5);
        check(mi.association() == assoc, "association from 3-arg factory");
        check(mi.address().equals(addr), "address from 3-arg factory");
        check(mi.streamNumber() == 5, "streamNumber from 3-arg factory");

        // null address is allowed for both factories
        mi = com.sun.nio.sctp.MessageInfo.createOutgoing((SocketAddress) null, 0);
        check(mi.address() == null, "null address 2-arg factory");
        check(mi.association() == null, "null address 2-arg factory -> null association");
        mi = com.sun.nio.sctp.MessageInfo.createOutgoing(assoc, null, 0);
        check(mi.address() == null, "null address 3-arg factory");

        // stream number validation: negative and > 65536 are rejected; boundaries accepted
        checkIAE("createOutgoing(addr,-1)", () -> com.sun.nio.sctp.MessageInfo.createOutgoing(addr, -1));
        checkIAE("createOutgoing(addr,65537)", () -> com.sun.nio.sctp.MessageInfo.createOutgoing(addr, 65537));
        checkIAE("createOutgoing(assoc,addr,-1)", () -> com.sun.nio.sctp.MessageInfo.createOutgoing(assoc, addr, -1));
        checkIAE("createOutgoing(assoc,addr,65537)", () -> com.sun.nio.sctp.MessageInfo.createOutgoing(assoc, addr, 65537));
        checkIAE("createOutgoing(null association)", () -> com.sun.nio.sctp.MessageInfo.createOutgoing(null, addr, 0));

        check(com.sun.nio.sctp.MessageInfo.createOutgoing(addr, 0).streamNumber() == 0, "streamNumber 0 accepted");
        check(com.sun.nio.sctp.MessageInfo.createOutgoing(addr, 65536).streamNumber() == 65536, "streamNumber 65536 accepted");

        // setter validation
        com.sun.nio.sctp.MessageInfo s = com.sun.nio.sctp.MessageInfo.createOutgoing(addr, 0);
        checkIAE("streamNumber(-1)", () -> s.streamNumber(-1));
        checkIAE("streamNumber(65537)", () -> s.streamNumber(65537));
        check(s.streamNumber(65536).streamNumber() == 65536, "setter streamNumber 65536 accepted");
    }

    static void checkIAE(String what, Runnable r) {
        try {
            r.run();
            check(false, what + " should throw IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            check(true, what + " throws IllegalArgumentException");
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
