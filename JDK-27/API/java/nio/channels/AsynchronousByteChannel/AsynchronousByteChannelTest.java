/*
 * @test
 * @summary java.nio.channels.AsynchronousByteChannel conformance: white-box structure + black-box behavior.
 * @run main AsynchronousByteChannelTest
 */

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AsynchronousByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.AsynchronousByteChannel");
        check(c.isInterface(), "AsynchronousByteChannel is an interface");
        check(java.nio.channels.AsynchronousChannel.class.isAssignableFrom(c),
              "extends AsynchronousChannel");
        for (String n : new String[] {"read", "write"})
            check(java.util.Arrays.stream(c.getMethods()).filter(m -> m.getName().equals(n)).count() == 2,
                  "read and write each have Future and CompletionHandler variants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();

            // Future-based write/read through the interface type
            try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                check(client.connect(addr).get(10, TimeUnit.SECONDS) == null,
                      "connect completed (Void)");
                AsynchronousSocketChannel peer = server.accept().get(10, TimeUnit.SECONDS);
                AsynchronousByteChannel abc = client;

                Future<Integer> wf = abc.write(ByteBuffer.wrap(new byte[] {3, 1, 4}));
                check(wf.get(10, TimeUnit.SECONDS) == 3, "future write 3 bytes");
                // peer echoes the bytes back
                ByteBuffer echo = ByteBuffer.allocate(3);
                Future<Integer> pr = peer.read(echo);
                check(pr.get(10, TimeUnit.SECONDS) == 3, "peer received 3");
                peer.write((ByteBuffer) echo.flip()).get(10, TimeUnit.SECONDS);
                ByteBuffer rbuf = ByteBuffer.allocate(3);
                Future<Integer> rf = abc.read(rbuf);
                check(rf.get(10, TimeUnit.SECONDS) == 3, "future read 3 bytes");
                check(rbuf.get(0) == 3 && rbuf.get(1) == 1 && rbuf.get(2) == 4, "read content");
                check(rf.isDone(), "future done");

                // CompletionHandler variant through the interface
                java.util.concurrent.CompletableFuture<Integer> hw = new java.util.concurrent.CompletableFuture<>();
                abc.write(ByteBuffer.wrap(new byte[] {7}), "w",
                        new java.nio.channels.CompletionHandler<Integer, String>() {
                            public void completed(Integer v, String a) { hw.complete(v); }
                            public void failed(Throwable t, String a) { hw.completeExceptionally(t); }
                        });
                check(hw.get(10, TimeUnit.SECONDS) == 1, "handler write 1 byte");
                ByteBuffer pb = ByteBuffer.allocate(1);
                check(peer.read(pb).get(10, TimeUnit.SECONDS) == 1, "peer got handler-written byte");
                peer.write((ByteBuffer) pb.flip()).get(10, TimeUnit.SECONDS);

                java.util.concurrent.CompletableFuture<Integer> hr = new java.util.concurrent.CompletableFuture<>();
                ByteBuffer rb2 = ByteBuffer.allocate(1);
                abc.read(rb2, "r",
                        new java.nio.channels.CompletionHandler<Integer, String>() {
                            public void completed(Integer v, String a) { hr.complete(v); }
                            public void failed(Throwable t, String a) { hr.completeExceptionally(t); }
                        });
                check(hr.get(10, TimeUnit.SECONDS) == 1 && rb2.get(0) == 7, "handler read 1 byte");

                // closed channel read fails
                abc.close();
                boolean threw = false;
                try { abc.read(ByteBuffer.allocate(1)).get(10, TimeUnit.SECONDS); }
                catch (Exception e) { threw = true; }
                check(threw, "read after close fails");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
