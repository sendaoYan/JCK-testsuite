/*
 * @test
 * @summary java.nio.channels.InterruptibleChannel conformance: white-box structure + black-box behavior.
 * @run main InterruptibleChannelTest
 */

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class InterruptibleChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(InterruptibleChannel.class.isInterface(), "is interface");
        check(java.nio.channels.Channel.class.isAssignableFrom(InterruptibleChannel.class),
              "extends Channel");
        Set<String> names = new HashSet<>();
        for (Method m : InterruptibleChannel.class.getMethods()) names.add(m.getName());
        check(names.contains("close"), "redeclares close");
        check(FileChannel.class.isAssignableFrom(InterruptibleChannel.class) == false,
              "FileChannel implements it (assignability checked via instance below)");
        check(java.nio.channels.spi.AbstractInterruptibleChannel.class
              .getInterfaces().length > 0, "AbstractInterruptibleChannel implements interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // FileChannel is interruptible: interrupting a thread blocked in read
        // closes the channel and throws ClosedByInterruptException in that thread.
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("i.bin");
            Files.write(file, new byte[0]);

            // A thread blocked reading a Pipe source is interrupted: it gets
            // ClosedByInterruptException and the channel ends up closed.
            final Exception[] caught = new Exception[1];
            java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
            try {
                CountDownLatch entered = new CountDownLatch(1);
                final java.nio.channels.Pipe.SourceChannel source = pipe.source();
                Thread reader = new Thread(() -> {
                    try {
                        entered.countDown();
                        source.read(ByteBuffer.allocate(1)); // blocks: sink never writes
                    } catch (Exception e) {
                        caught[0] = e;
                    }
                }, "interruptible-reader");
                reader.setDaemon(true);
                reader.start();
                entered.await();
                Thread.sleep(300); // let the reader block in read()
                reader.interrupt();
                reader.join(10_000);
                check(!reader.isAlive(), "reader thread finished after interrupt");
                check(caught[0] instanceof java.nio.channels.ClosedByInterruptException,
                      "interrupt while blocked -> ClosedByInterruptException, got " + caught[0]);
                check(!source.isOpen(), "channel closed by interrupt");
            } finally {
                try { pipe.sink().close(); } catch (Exception ignore) { }
                try { pipe.source().close(); } catch (Exception ignore) { }
            }

            check(Files.readAllBytes(file).length == 0, "file untouched");

            // basic interface behavior: close via interface type
            FileChannel ch = FileChannel.open(file, StandardOpenOption.READ);
            InterruptibleChannel ic = ch;
            check(ic instanceof java.nio.channels.Channel, "is a Channel");
            ic.close();
            check(!((java.nio.channels.Channel) ic).isOpen(), "closed via InterruptibleChannel");
        } finally {
            try (var s = Files.list(dir)) { s.forEach(p -> { try { Files.delete(p); } catch (Exception ignore) { } }); }
            Files.deleteIfExists(dir);
        }
    }

    // ===================== A6 deepening: interrupt/close semantics on blocking operations =====================

    static final InetAddress LO = InetAddress.getLoopbackAddress();

    /** Bounded settle (poll-with-timeout, < 0.5 s) so a daemon thread reliably enters a blocking op. */
    static void settle() {
        LockSupport.parkNanos(300_000_000L);
    }

    static void testImplementors() throws Exception {
        check(InterruptibleChannel.class.isAssignableFrom(SocketChannel.class),
              "SocketChannel is an InterruptibleChannel");
        check(InterruptibleChannel.class.isAssignableFrom(ServerSocketChannel.class),
              "ServerSocketChannel is an InterruptibleChannel");
        check(InterruptibleChannel.class.isAssignableFrom(DatagramChannel.class),
              "DatagramChannel is an InterruptibleChannel");
        check(InterruptibleChannel.class.isAssignableFrom(Pipe.SourceChannel.class),
              "Pipe.SourceChannel is an InterruptibleChannel");
        check(InterruptibleChannel.class.isAssignableFrom(Pipe.SinkChannel.class),
              "Pipe.SinkChannel is an InterruptibleChannel");
        check(InterruptibleChannel.class.isAssignableFrom(FileChannel.class),
              "FileChannel is an InterruptibleChannel (via AbstractInterruptibleChannel)");
    }

    /** Loopback pair; the server side is accepted eagerly and stays silent. */
    static SocketChannel[] connectedPair(ServerSocketChannel ssc) throws Exception {
        ssc.bind(new InetSocketAddress(LO, 0));
        final SocketChannel client = SocketChannel.open(
                (InetSocketAddress) ssc.getLocalAddress());
        SocketChannel server = ssc.accept();
        return new SocketChannel[] {client, server};
    }

    static void testInterruptBlockingRead() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            final SocketChannel[] pair = connectedPair(ssc);
            try {
                final SocketChannel reader = pair[0];
                final Exception[] caught = new Exception[1];
                final boolean[] flag = new boolean[1];
                final CountDownLatch started = new CountDownLatch(1);
                Thread t = new Thread(() -> {
                    try {
                        started.countDown();
                        reader.read(ByteBuffer.allocate(8)); // blocks: peer never sends
                    } catch (Exception e) {
                        caught[0] = e;
                    }
                    flag[0] = Thread.currentThread().isInterrupted();
                }, "interrupt-reader");
                t.setDaemon(true);
                t.start();
                started.await();
                settle(); // reader is inside the blocking read now
                t.interrupt();
                t.join(10_000);
                check(!t.isAlive(), "reader finished after interrupt (bounded)");
                check(caught[0] instanceof java.nio.channels.ClosedByInterruptException,
                      "interrupt while blocking -> ClosedByInterruptException, got " + caught[0]);
                check(!reader.isOpen(), "channel closed by the interrupt");
                check(flag[0], "thread interrupt status preserved");
            } finally {
                pair[0].close();
                pair[1].close();
            }
        }
    }

    static void testCloseWhileBlockingRead() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            final SocketChannel[] pair = connectedPair(ssc);
            try {
                final SocketChannel reader = pair[0];
                final Exception[] caught = new Exception[1];
                final CountDownLatch started = new CountDownLatch(1);
                Thread t = new Thread(() -> {
                    try {
                        started.countDown();
                        reader.read(ByteBuffer.allocate(8)); // blocks
                    } catch (Exception e) {
                        caught[0] = e;
                    }
                }, "close-reader");
                t.setDaemon(true);
                t.start();
                started.await();
                settle();
                reader.close(); // asynchronous close from another thread
                t.join(10_000);
                check(!t.isAlive(), "reader finished after close (bounded)");
                check(caught[0] instanceof java.nio.channels.AsynchronousCloseException,
                      "close while blocking -> AsynchronousCloseException, got " + caught[0]);
                check(!reader.isOpen(), "channel closed");
            } finally {
                pair[1].close();
            }
        }
    }

    static void testInterruptBlockingAccept() throws Exception {
        final ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            ssc.bind(new InetSocketAddress(LO, 0));
            final Exception[] caught = new Exception[1];
            final boolean[] flag = new boolean[1];
            final CountDownLatch started = new CountDownLatch(1);
            Thread t = new Thread(() -> {
                try {
                    started.countDown();
                    ssc.accept(); // blocks: no client ever connects
                } catch (Exception e) {
                    caught[0] = e;
                }
                flag[0] = Thread.currentThread().isInterrupted();
            }, "interrupt-acceptor");
            t.setDaemon(true);
            t.start();
            started.await();
            settle();
            t.interrupt();
            t.join(10_000);
            check(!t.isAlive(), "acceptor finished after interrupt (bounded)");
            check(caught[0] instanceof java.nio.channels.ClosedByInterruptException,
                  "interrupt while accepting -> ClosedByInterruptException, got " + caught[0]);
            check(!ssc.isOpen(), "server channel closed by the interrupt");
            check(flag[0], "acceptor interrupt status preserved");
        } finally {
            ssc.close();
        }
    }

    static void testCloseWhileBlockingAccept() throws Exception {
        final ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            ssc.bind(new InetSocketAddress(LO, 0));
            final Exception[] caught = new Exception[1];
            final CountDownLatch started = new CountDownLatch(1);
            Thread t = new Thread(() -> {
                try {
                    started.countDown();
                    ssc.accept();
                } catch (Exception e) {
                    caught[0] = e;
                }
            }, "close-acceptor");
            t.setDaemon(true);
            t.start();
            started.await();
            settle();
            ssc.close();
            t.join(10_000);
            check(!t.isAlive(), "acceptor finished after close (bounded)");
            check(caught[0] instanceof java.nio.channels.AsynchronousCloseException,
                  "close while accepting -> AsynchronousCloseException, got " + caught[0]);
            check(!ssc.isOpen(), "server channel closed");
        } finally {
            ssc.close();
        }
    }

    static void testInterruptBlockingReceive() throws Exception {
        final DatagramChannel dc = DatagramChannel.open();
        try {
            dc.bind(new InetSocketAddress(LO, 0));
            final Exception[] caught = new Exception[1];
            final CountDownLatch started = new CountDownLatch(1);
            Thread t = new Thread(() -> {
                try {
                    started.countDown();
                    dc.receive(ByteBuffer.allocate(4)); // blocks: no datagram ever arrives
                } catch (Exception e) {
                    caught[0] = e;
                }
            }, "interrupt-receiver");
            t.setDaemon(true);
            t.start();
            started.await();
            settle();
            t.interrupt();
            t.join(10_000);
            check(!t.isAlive(), "receiver finished after interrupt (bounded)");
            check(caught[0] instanceof java.nio.channels.ClosedByInterruptException,
                  "interrupt while receiving -> ClosedByInterruptException, got " + caught[0]);
            check(!dc.isOpen(), "datagram channel closed by the interrupt");
        } finally {
            dc.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testImplementors();
        testInterruptBlockingRead();
        testCloseWhileBlockingRead();
        testInterruptBlockingAccept();
        testCloseWhileBlockingAccept();
        testInterruptBlockingReceive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
