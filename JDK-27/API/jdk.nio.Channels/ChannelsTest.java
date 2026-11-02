/*
 * @test
 * @summary jdk.nio.Channels conformance: read-write selectable channel.
 * @run main ChannelsTest
 */

public class ChannelsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        java.io.File f = java.io.File.createTempFile("fdx", ".tmp");
        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        java.io.FileDescriptor fd = fos.getFD();
        final boolean[] closed = new boolean[1];
        java.nio.channels.SelectableChannel sc = jdk.nio.Channels.readWriteSelectableChannel(fd, new jdk.nio.Channels.SelectableChannelCloser() {
            public void implCloseChannel(java.nio.channels.SelectableChannel c) throws java.io.IOException { closed[0] = true; }
            public void implReleaseChannel(java.nio.channels.SelectableChannel c) throws java.io.IOException {}
        });
        check(sc != null, "channel non-null");
        check(sc.isOpen(), "channel open");
        check(!sc.isRegistered(), "channel not registered");
        check(sc.validOps() == (java.nio.channels.SelectionKey.OP_READ | java.nio.channels.SelectionKey.OP_WRITE), "validOps");
        sc.close();
        check(closed[0], "implCloseChannel called");
        check(!sc.isOpen(), "channel closed");
        fos.close();
        f.delete();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
