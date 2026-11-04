/*
 * @test
 * @summary jdk.nio.Channels conformance: white-box structure + black-box behavior.
 * @run main ChannelsTest
 */

import jdk.nio.*;
import java.nio.channels.*;
import java.lang.reflect.*;
import java.util.*;
public class ChannelsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.nio.Channels");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(hasMethod(c, "readWriteSelectableChannel"), "readWriteSelectableChannel");

    }

    static void testBlackBox() throws Exception {
        java.io.File f = java.io.File.createTempFile("fdx", ".tmp");
        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        java.io.FileDescriptor fd = fos.getFD();
        final boolean[] closed = new boolean[1];
        SelectableChannel sc = jdk.nio.Channels.readWriteSelectableChannel(fd, new jdk.nio.Channels.SelectableChannelCloser() {
            public void implCloseChannel(SelectableChannel c) throws java.io.IOException { closed[0] = true; }
            public void implReleaseChannel(SelectableChannel c) throws java.io.IOException {}
        });
        check(sc != null, "channel non-null");
        check(sc.isOpen(), "channel open");
        check(!sc.isRegistered(), "channel not registered");
        check(sc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE), "validOps");
        sc.close();
        check(closed[0], "implCloseChannel called");
        check(!sc.isOpen(), "channel closed");
        fos.close();
        f.delete();

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
