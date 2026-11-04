/*
 * @test
 * @summary java.net.DatagramSocketImpl conformance: white-box structure + black-box behavior.
 * @run main DatagramSocketImplTest
 */

import java.lang.reflect.*;
import java.net.*;

public class DatagramSocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocketImpl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(SocketOptions.class.isAssignableFrom(c), "implements SocketOptions");
        check(c.getConstructor() != null, "no-arg ctor");
        for (String m : new String[]{"create","bind","send","receive","peek","peekData","setTimeToLive","getTimeToLive","join","leave","joinGroup","leaveGroup","close","getLocalPort","getFileDescriptor"}) {
            check(hasMethod(c, m), "method " + m);
        }
        check(c.getDeclaredField("localPort") != null, "localPort field");
        check(c.getDeclaredField("fd") != null, "fd field");
    }

    static void testBlackBox() throws Exception {
        check(Modifier.isAbstract(DatagramSocketImpl.class.getModifiers()), "still abstract");
        check(SocketOptions.class.isAssignableFrom(DatagramSocketImpl.class), "is SocketOptions");
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(0);
            check(ds.getLocalPort() > 0, "ephemeral port assigned via impl");
        } finally {
            if (ds != null) ds.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
