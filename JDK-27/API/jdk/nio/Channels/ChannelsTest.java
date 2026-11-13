/*
 * @test
 * @summary jdk.nio.Channels conformance: white-box structure + black-box behavior.
 * @library /lib
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


    static void testDeepen() throws Exception {
        java.io.File f = java.io.File.createTempFile("chandeepen", ".tmp");
        f.deleteOnExit();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        fos.write("payload".getBytes("UTF-8"));
        fos.flush();
        java.io.FileInputStream fis = new java.io.FileInputStream(f);
        final boolean[] closed = new boolean[1];
        final boolean[] released = new boolean[1];
        SelectableChannel sc = jdk.nio.Channels.readWriteSelectableChannel(fis.getFD(), new jdk.nio.Channels.SelectableChannelCloser() {
            public void implCloseChannel(SelectableChannel c) { closed[0] = true; }
            public void implReleaseChannel(SelectableChannel c) { released[0] = true; }
        });
        check(sc.isBlocking(), "new channel blocking by default");
        check(sc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE), "validOps READ|WRITE == 5");
        check(sc.provider() != null, "provider attached");
        check(sc.configureBlocking(false) == sc && !sc.isBlocking(), "configureBlocking(false)");
        Selector sel = Selector.open();
        SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
        check(key.interestOps() == SelectionKey.OP_READ, "key interestOps");
        check(key.readyOps() == 0, "fresh key not ready");
        check(key.channel() == sc, "key channel identity");
        check(sel.selectNow() == 0, "selectNow on regular-file fd yields no ready keys");
        sel.close();
        sc.configureBlocking(true);
        check(sc.isBlocking(), "configureBlocking back to true");
        sc.close();
        check(closed[0], "implCloseChannel invoked exactly by close");
        check(!released[0], "implReleaseChannel not invoked by close alone");
        sc.close();
        check(!sc.isOpen(), "double close idempotent");
        boolean npe = false;
        try { jdk.nio.Channels.readWriteSelectableChannel(fis.getFD(), null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "null closer NPE");
        fis.close(); fos.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // The documented contract for readWriteSelectableChannel is a "socket
        // or resource that can be multiplexed by a Selector". The Windows
        // Selector (WEPoll) multiplexes sockets only, so a regular-file
        // descriptor is outside that contract there and the blocking-mode
        // switch fails with "Socket operation on nonsocket: ioctlsocket".
        if (java.io.File.separatorChar == '\\') {
            throw new jtreg.SkippedException(
                    "Windows Selector only multiplexes sockets; a regular-file"
                    + " descriptor is not a selectable resource on Windows");
        }
        testDeepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
