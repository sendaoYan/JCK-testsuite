/*
 * @test
 * @summary java.nio.channels.spi.AbstractInterruptibleChannel conformance: white-box structure + black-box behavior.
 * @run main AbstractInterruptibleChannelTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.util.Set;

public class AbstractInterruptibleChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal subclass exercising the begin/end protocol. */
    static final class StubChannel extends AbstractInterruptibleChannel {
        boolean implClosed;
        int closeCalls;
        protected void implCloseChannel() {
            closeCalls++;
            implClosed = true;
        }
        int operate(boolean completeNormally) throws Exception {
            begin();
            try {
                return completeNormally ? 42 : 0;
            } finally {
                end(completeNormally);
            }
        }
        /** begin/end with an already-closed channel: end(false) must report the close. */
        void endAfterClose() throws Exception {
            begin();
            end(false);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.spi.AbstractInterruptibleChannel");
        check(Modifier.isAbstract(c.getModifiers()), "AbstractInterruptibleChannel is abstract");
        for (Class<?> i : new Class<?>[] {Channel.class, InterruptibleChannel.class})
            check(i.isAssignableFrom(c), "implements " + i.getSimpleName());
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        check(Modifier.isFinal(c.getMethod("close").getModifiers()), "close is final");
        check(Modifier.isFinal(c.getMethod("isOpen").getModifiers()), "isOpen is final");
        check(Modifier.isProtected(c.getDeclaredMethod("begin").getModifiers()), "begin protected");
        check(Modifier.isProtected(c.getDeclaredMethod("end", boolean.class).getModifiers()),
              "end protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StubChannel ch = new StubChannel();
        check(ch.isOpen(), "open after construction");
        check(ch instanceof Channel && ch instanceof InterruptibleChannel,
              "interfaces satisfied");

        // begin/end around a successful operation
        check(ch.operate(true) == 42, "operation result delivered");

        // close() delegates to implCloseChannel exactly once and flips isOpen
        ch.close();
        check(!ch.isOpen(), "closed after close");
        check(ch.implClosed, "implCloseChannel invoked");
        check(ch.closeCalls == 1, "implCloseChannel called once");
        ch.close(); // idempotent
        check(ch.closeCalls == 1, "second close does not re-invoke implCloseChannel");

        // end(false) after asynchronous close throws AsynchronousCloseException
        StubChannel ch2 = new StubChannel();
        ch2.close();
        boolean threw = false;
        try {
            ch2.endAfterClose(); // begin/end(false) from inside the subclass
        } catch (java.nio.channels.AsynchronousCloseException e) {
            threw = true;
        }
        check(threw, "end(false) on closed channel -> AsynchronousCloseException");

        // a concrete JDK subclass behaves per the same contract: FileChannel
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck");
        try {
            java.nio.file.Path f = dir.resolve("aic.bin");
            java.nio.file.Files.write(f, new byte[] {7});
            AbstractInterruptibleChannel fc = java.nio.channels.FileChannel.open(
                    f, java.nio.file.StandardOpenOption.READ);
            check(fc.isOpen(), "FileChannel open via abstract type");
            fc.close();
            check(!fc.isOpen(), "FileChannel closed via abstract type");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("aic.bin"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
