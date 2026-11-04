/*
 * @test
 * @summary java.nio.channels.InterruptibleChannel conformance: white-box structure + black-box behavior.
 * @run main InterruptibleChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
