/*
 * @test
 * @summary org.ietf.jgss.ChannelBinding conformance: white-box structure + black-box behavior.
 * @run main ChannelBindingTest
 */

import org.ietf.jgss.*;
import java.lang.reflect.*;
import java.net.InetAddress;
import java.util.Arrays;

public class ChannelBindingTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasCtor(Class<?> c, Class<?>... p) { try { c.getDeclaredConstructor(p); return true; } catch (Exception e) { return false; } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.ChannelBinding");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class not interface");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasCtor(c, InetAddress.class, InetAddress.class, byte[].class), "ctor(InetAddress,InetAddress,byte[])");
        check(hasCtor(c, byte[].class), "ctor(byte[])");
        String[] names = { "getInitiatorAddress", "getAcceptorAddress", "getApplicationData",
                "equals", "hashCode" };
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // null-appData ctor
        ChannelBinding cbNull = new ChannelBinding((byte[]) null);
        check(cbNull.getInitiatorAddress() == null, "null initiator");
        check(cbNull.getAcceptorAddress() == null, "null acceptor");
        check(cbNull.getApplicationData() == null, "null application data");

        // data-only ctor copies input defensively
        byte[] data = { 1, 2, 3 };
        ChannelBinding cb = new ChannelBinding(data);
        check(Arrays.equals(new byte[]{1, 2, 3}, cb.getApplicationData()), "getApplicationData equals input");
        check(cb.getInitiatorAddress() == null && cb.getAcceptorAddress() == null, "data-only ctor null addresses");
        data[0] = 99;
        check(Arrays.equals(new byte[]{1, 2, 3}, cb.getApplicationData()),
                "constructor copies appData (mutating source does not affect)");
        byte[] got = cb.getApplicationData();
        got[1] = 77;
        check(Arrays.equals(new byte[]{1, 2, 3}, cb.getApplicationData()),
                "getApplicationData returns a copy (mutating result does not affect)");

        // full ctor with addresses
        InetAddress init = InetAddress.getByName("127.0.0.1");
        InetAddress accept = InetAddress.getByName("127.0.0.2");
        byte[] d2 = { 4, 5, 6 };
        ChannelBinding full = new ChannelBinding(init, accept, d2);
        check(full.getInitiatorAddress() == init, "initiator address identity");
        check(full.getAcceptorAddress() == accept, "acceptor address identity");
        check(Arrays.equals(new byte[]{4, 5, 6}, full.getApplicationData()), "full ctor appData");
        // the full ctor also takes a defensive copy of the app data
        d2[0] = 99;
        check(Arrays.equals(new byte[]{4, 5, 6}, full.getApplicationData()),
                "full ctor copies appData defensively");
        // loopback address equality (value semantics of InetAddress)
        check(full.getInitiatorAddress().equals(InetAddress.getLoopbackAddress()),
                "initiator 127.0.0.1 equals loopback address");
        // null appData accepted in the full ctor as well
        check(new ChannelBinding(init, accept, null).getApplicationData() == null,
                "full ctor null appData");

        // equals: reflexive, symmetric-by-data, null, non-ChannelBinding
        check(cb.equals(cb), "equals self");
        check(new ChannelBinding(new byte[]{1, 2, 3}).equals(cb), "equals by data");
        check(!cb.equals(new ChannelBinding(new byte[]{9, 9, 9})), "not equals different data");
        check(!cb.equals(null), "not equals null");
        check(!cb.equals("1,2,3"), "not equals non-ChannelBinding");
        check(full.equals(new ChannelBinding(init, accept, new byte[]{4, 5, 6})), "equals by addresses + data");
        check(!full.equals(new ChannelBinding(InetAddress.getByName("127.0.0.9"), accept, new byte[]{4, 5, 6})),
                "not equals different initiator address");
        check(!full.equals(new ChannelBinding(init, InetAddress.getByName("127.0.0.9"), new byte[]{4, 5, 6})),
                "not equals different acceptor address");
        check(!full.equals(cb), "not equals addresses vs no addresses");
        check(!new ChannelBinding((byte[]) null).equals(cb), "null appData not equals non-null appData");

        // hashCode consistency + null-field fallback
        check(cb.hashCode() == new ChannelBinding(new byte[]{1, 2, 3}).hashCode(), "equal objects equal hashCode");
        check(new ChannelBinding((byte[]) null).hashCode() == 1, "all-null hashCode == 1");
        check(new ChannelBinding(init, null, null).hashCode() == init.hashCode(), "initiator-only hashCode");
        check(new ChannelBinding(null, accept, null).hashCode() == accept.hashCode(), "acceptor-only hashCode");
        check(new ChannelBinding(null, null, new byte[]{1, 2, 3}).hashCode() == Arrays.hashCode(new byte[]{1, 2, 3}),
                "appData-only hashCode");
        // the first non-null address wins even when appData is present
        check(new ChannelBinding(init, null, new byte[]{1, 2, 3}).hashCode() == init.hashCode(),
                "initiator takes hashCode precedence over appData");
        check(new ChannelBinding(null, accept, new byte[]{1, 2, 3}).hashCode() == accept.hashCode(),
                "acceptor takes hashCode precedence over appData");
        // toString is non-null on every path (null-safe)
        check(new ChannelBinding((byte[]) null).toString() != null, "toString non-null all-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
