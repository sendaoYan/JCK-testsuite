/*
 * @test
 * @summary java.nio.channels.InterruptibleChannel conformance: interface hierarchy and methods.
 * @run main InterruptibleChannelTest
 */

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.Channel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.lang.reflect.Method;
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

    static void testInterruptibleChannel() {
        check(InterruptibleChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(InterruptibleChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : InterruptibleChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "close" };
        for (String n : required) check(names.contains(n), "has method " + n);
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
        check(InterruptibleChannel.class.isAssignableFrom(java.nio.channels.FileChannel.class),
              "FileChannel is an InterruptibleChannel (via AbstractInterruptibleChannel)");
        // close() works through the interface type
        SocketChannel sc = SocketChannel.open();
        InterruptibleChannel ic = sc;
        check(ic instanceof Channel, "is a Channel");
        ic.close();
        check(!sc.isOpen(), "closed via InterruptibleChannel reference");
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

    static void testInterruptPipeSourceRead() throws Exception {
        final Pipe pipe = Pipe.open();
        try {
            final Exception[] caught = new Exception[1];
            final CountDownLatch started = new CountDownLatch(1);
            Thread t = new Thread(() -> {
                try {
                    started.countDown();
                    pipe.source().read(ByteBuffer.allocate(1)); // blocks: sink never writes
                } catch (Exception e) {
                    caught[0] = e;
                }
            }, "interrupt-pipe");
            t.setDaemon(true);
            t.start();
            started.await();
            settle();
            t.interrupt();
            t.join(10_000);
            check(!t.isAlive(), "pipe reader finished after interrupt (bounded)");
            check(caught[0] instanceof java.nio.channels.ClosedByInterruptException,
                  "interrupt while pipe-reading -> ClosedByInterruptException, got " + caught[0]);
            check(!pipe.source().isOpen(), "pipe source closed by the interrupt");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    public static void main(String[] args) throws Exception {
        testInterruptibleChannel();
        testImplementors();
        testInterruptBlockingRead();
        testCloseWhileBlockingRead();
        testInterruptBlockingAccept();
        testCloseWhileBlockingAccept();
        testInterruptBlockingReceive();
        testInterruptPipeSourceRead();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
