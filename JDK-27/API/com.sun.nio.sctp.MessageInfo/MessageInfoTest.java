/*
 * @test
 * @summary com.sun.nio.sctp.MessageInfo conformance: outgoing message info.
 * @run main MessageInfoTest
 */

import java.lang.reflect.*;
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

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.nio.sctp.MessageInfo");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        java.net.SocketAddress addr = new java.net.InetSocketAddress("127.0.0.1", 12345);
        com.sun.nio.sctp.MessageInfo mi = com.sun.nio.sctp.MessageInfo.createOutgoing(addr, 5);
        check(mi.address().equals(addr), "address");
        check(mi.streamNumber() == 5, "streamNumber");
        check(mi.isComplete(), "isComplete default");
        check(!mi.isUnordered(), "isUnordered default false");
        check(mi.complete(false) == mi, "complete returns this");
        check(mi.unordered(true) == mi, "unordered returns this");
        check(mi.payloadProtocolID(7) == mi, "payloadProtocolID returns this");
        check(mi.payloadProtocolID() == 7, "payloadProtocolID 7");
        check(mi.streamNumber(9) == mi, "streamNumber returns this");
        check(mi.streamNumber() == 9, "streamNumber 9");
        check(mi.timeToLive(1000) == mi, "timeToLive returns this");
        check(mi.timeToLive() == 1000, "timeToLive 1000");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
