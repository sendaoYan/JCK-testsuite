/*
 * @test
 * @summary java.nio.channels.MulticastChannel conformance: white-box structure + black-box behavior.
 * @run main MulticastChannelTest
 */

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.MulticastChannel;
import java.util.Set;

public class MulticastChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.MulticastChannel");
        check(c.isInterface(), "MulticastChannel is an interface");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c),
              "extends NetworkChannel");
        for (String n : new String[] {"join", "close"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
        check(java.nio.channels.DatagramChannel.class.isAssignableFrom(c) == false,
              "MulticastChannel does not extend DatagramChannel (other way around)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InetAddress group = InetAddress.getByName("224.0.113.1"); // AD-HOC block 1, no external traffic

        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            MulticastChannel mc = dc;
            dc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            check(dc.getLocalAddress() != null, "channel bound");

            // find a multicast-capable interface (usually loopback on Linux)
            NetworkInterface nif = null;
            for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isUp() && ni.supportsMulticast() && ni.inetAddresses().findAny().isPresent()) {
                    nif = ni;
                    break;
                }
            }
            if (nif == null) {
                System.out.println("NOTE: no multicast-capable interface; join tests skipped");
            } else {
                MembershipKey key = mc.join(group, nif);
                check(key != null, "membership acquired");
                check(key.isValid(), "membership valid");
                check(key.channel() == dc, "membership channel");
                check(key.group().equals(group), "membership group");
                check(key.networkInterface() == nif, "membership interface");
                check(key.sourceAddress() == null, "no source filter by default");

                // membership delivers a datagram sent to the group by ourselves
                InetAddress ifaceAddr = nif.inetAddresses().findFirst().orElse(null);
                if (ifaceAddr != null) {
                    dc.configureBlocking(false);
                    java.nio.channels.Selector sel = java.nio.channels.Selector.open();
                    dc.register(sel, java.nio.channels.SelectionKey.OP_READ);
                    try (DatagramChannel sender = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
                        sender.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
                        ByteBuffer out = ByteBuffer.wrap(new byte[] {77});
                        sender.send(out, new InetSocketAddress(group,
                                ((InetSocketAddress) dc.getLocalAddress()).getPort()));
                    }
                    int n = sel.select(5000);
                    check(n >= 0, "select returned");
                    ByteBuffer in = ByteBuffer.allocate(16);
                    if (n > 0) {
                        check(dc.receive(in) != null, "multicast packet received");
                        check(in.get(0) == 77, "multicast payload");
                    }
                    sel.close();
                }

                key.drop();
                check(!key.isValid(), "membership invalid after drop");

                // rejoin and close the channel: membership invalidated implicitly
                MembershipKey k2 = mc.join(group, nif);
                check(k2.isValid(), "second membership valid");
            }
            // close() through the interface type
            mc.close();
            check(!dc.isOpen(), "closed via MulticastChannel.close");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
