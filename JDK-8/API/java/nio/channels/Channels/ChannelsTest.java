/*
 * @test
 * @summary java.nio.channels.Channels conformance: stream/channel bridges.
 * @run main ChannelsTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ChannelsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChannels() throws Exception {
        // InputStream -> ReadableByteChannel
        ReadableByteChannel rc = Channels.newChannel(
                new ByteArrayInputStream(new byte[] {65, 66, 67}));
        check(rc.isOpen(), "newChannel(InputStream) isOpen");
        ByteBuffer bb = ByteBuffer.allocate(3);
        rc.read(bb);
        bb.flip();
        check(bb.get() == 65, "read A");
        rc.close();

        // OutputStream -> WritableByteChannel
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        WritableByteChannel wc = Channels.newChannel(bout);
        wc.write(ByteBuffer.wrap(new byte[] {88, 89}));
        check("XY".equals(new String(bout.toByteArray(), "UTF-8")),
              "newChannel(OutputStream) write");
        wc.close();

        // ReadableByteChannel -> InputStream
        ReadableByteChannel src = Channels.newChannel(new ByteArrayInputStream(new byte[] {7}));
        InputStream in = Channels.newInputStream(src);
        check(in.read() == 7, "newInputStream");
        in.close();

        // WritableByteChannel -> OutputStream
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        WritableByteChannel dst = Channels.newChannel(bout2);
        OutputStream out = Channels.newOutputStream(dst);
        out.write(9);
        out.flush();
        check(bout2.size() == 1, "newOutputStream write");
        out.close();

        // ReadableByteChannel -> Reader
        ReadableByteChannel src2 = Channels.newChannel(
                new ByteArrayInputStream("ok".getBytes("UTF-8")));
        Reader r = Channels.newReader(src2, "UTF-8");
        char[] cbuf = new char[2];
        r.read(cbuf);
        check("ok".equals(new String(cbuf)), "newReader");
        r.close();

        // WritableByteChannel -> Writer
        ByteArrayOutputStream bout3 = new ByteArrayOutputStream();
        WritableByteChannel dst2 = Channels.newChannel(bout3);
        Writer w = Channels.newWriter(dst2, "UTF-8");
        w.write("ab");
        w.flush();
        check("ab".equals(new String(bout3.toByteArray(), "UTF-8")), "newWriter");
        w.close();
    }

    public static void main(String[] args) throws Exception {
        testChannels();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
