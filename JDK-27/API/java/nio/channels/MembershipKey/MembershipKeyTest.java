/*
 * @test
 * @summary java.nio.channels.MembershipKey conformance: white-box structure + black-box behavior.
 * @run main MembershipKeyTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.Set;

public class MembershipKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.MembershipKey");
        check(Modifier.isAbstract(c.getModifiers()), "MembershipKey is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"isValid", "drop", "block", "unblock", "channel",
                "group", "networkInterface", "sourceAddress"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InetAddress group = InetAddress.getByName("224.0.113.2");

        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            dc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));

            NetworkInterface nif = null;
            for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isUp() && ni.supportsMulticast() && ni.inetAddresses().findAny().isPresent()) {
                    nif = ni;
                    break;
                }
            }
            if (nif == null) {
                System.out.println("NOTE: no multicast-capable interface; behavioral join skipped");
            } else {
                MembershipKey key = dc.join(group, nif);
                check(key.isValid(), "valid on join");
                check(key.channel() == dc, "channel()");
                check(key.group().equals(group), "group()");
                check(key.networkInterface().equals(nif), "networkInterface()");
                check(key.sourceAddress() == null, "sourceAddress null for any-source join");

                // drop invalidates
                key.drop();
                check(!key.isValid(), "invalid after drop");
                key.drop(); // second drop is a documented no-op
                check(!key.isValid(), "still invalid after no-op drop");

                // block/unblock round trip on an any-source membership
                MembershipKey any = dc.join(group, nif);
                InetAddress src = InetAddress.getLoopbackAddress();
                MembershipKey blocked = any.block(src);
                check(blocked == any, "block returns this key");
                MembershipKey unblocked = any.unblock(src);
                check(unblocked == any, "unblock returns this key");
                boolean threw = false;
                try { any.unblock(src); }
                catch (IllegalStateException e) { threw = true; }
                check(threw, "unblock non-blocked source -> IllegalStateException");

                // channel close invalidates membership
                dc.close();
                check(!any.isValid(), "invalid after channel close");
                threw = false;
                try { DatagramChannel.open(java.net.StandardProtocolFamily.INET).close();
                      DatagramChannel closed = DatagramChannel.open(java.net.StandardProtocolFamily.INET);
                      closed.close();
                      closed.join(group, nif); }
                catch (Exception e) { threw = true; }
                check(threw, "join on closed channel rejected");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
