/*
 * @test
 * @summary java.nio.channels.FileChannel conformance: white-box structure + black-box behavior.
 * @run main FileChannelTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.InterruptibleChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void deleteRecursively(Path p) throws IOException {
        if (Files.isDirectory(p)) {
            try (Stream<Path> s = Files.list(p)) {
                for (Path c : s.collect(Collectors.toList())) deleteRecursively(c);
            }
        }
        Files.deleteIfExists(p);
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.FileChannel");
        check(Modifier.isAbstract(c.getModifiers()), "FileChannel is abstract");
        check(SeekableByteChannel.class.isAssignableFrom(c), "implements SeekableByteChannel");
        check(GatheringByteChannel.class.isAssignableFrom(c), "implements GatheringByteChannel");
        check(ScatteringByteChannel.class.isAssignableFrom(c), "implements ScatteringByteChannel");
        check(InterruptibleChannel.class.isAssignableFrom(c), "implements InterruptibleChannel");
        check(java.nio.channels.spi.AbstractInterruptibleChannel.class.isAssignableFrom(c),
              "extends AbstractInterruptibleChannel");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");
        Set<String> names = new HashSet<>();
        for (Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"open", "read", "write", "position", "size", "truncate",
                "force", "transferTo", "transferFrom", "map", "lock", "tryLock", "isOpen", "close"})
            check(names.contains(n), "has method " + n);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("data.bin");

            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.CREATE,
                    StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                check(ch.isOpen(), "channel open");
                check(ch.size() == 0, "initial size 0");
                ByteBuffer out = ByteBuffer.wrap(new byte[] {10, 20, 30, 40, 50});
                int n = ch.write(out);
                check(n == 5, "wrote 5 bytes");
                check(ch.position() == 5, "position advanced to 5");
                check(ch.size() == 5, "size 5 after write");

                ch.position(0);
                ByteBuffer in = ByteBuffer.allocate(5);
                int r = ch.read(in);
                check(r == 5, "read 5 bytes");
                check(Arrays.equals(in.array(), new byte[] {10, 20, 30, 40, 50}), "content round trip");

                ch.position(5);
                ByteBuffer in2 = ByteBuffer.allocate(3);
                int r2 = ch.read(in2, 1);
                check(r2 == 3, "positional read 3");
                check(in2.get(0) == 20 && in2.get(2) == 40, "positional read content");
                check(ch.position() == 5, "channel position unchanged by positional read");

                ByteBuffer patch = ByteBuffer.wrap(new byte[] {99});
                ch.write(patch, 0);
                check(ch.size() == 5, "size unchanged by positional write");
                ByteBuffer v = ByteBuffer.allocate(1);
                ch.read(v, 0);
                check(v.get(0) == 99, "patch visible");

                ch.truncate(0);
                ch.position(0);
                ByteBuffer p1 = ByteBuffer.wrap(new byte[] {1, 2});
                ByteBuffer p2 = ByteBuffer.wrap(new byte[] {3, 4, 5});
                long sw = ch.write(new ByteBuffer[] {p1, p2});
                check(sw == 5, "gathering write 5");
                check(ch.size() == 5, "size 5 after gathering write");
                ch.position(0);
                ByteBuffer g1 = ByteBuffer.allocate(2);
                ByteBuffer g2 = ByteBuffer.allocate(3);
                long sr = ch.read(new ByteBuffer[] {g1, g2}, 0, 2);
                check(sr == 5, "scattering read 5");
                check(g1.get(0) == 1 && g1.get(1) == 2, "g1 content");
                check(g2.get(0) == 3 && g2.get(2) == 5, "g2 content");

                ch.truncate(3);
                check(ch.size() == 3, "size 3 after truncate");
                ch.position(0);
                ByteBuffer t = ByteBuffer.allocate(8);
                int tr = ch.read(t);
                check(tr == 3, "only 3 bytes after truncate");
                check(t.get(0) == 1 && t.get(1) == 2 && t.get(2) == 3, "truncated head kept");

                Path other = dir.resolve("other.bin");
                try (FileChannel dst = FileChannel.open(other, StandardOpenOption.CREATE,
                        StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                    long moved = ch.transferTo(0, ch.size(), dst);
                    check(moved == 3, "transferTo moved 3");
                    check(dst.size() == 3, "dst size 3");
                    ByteBuffer back = ByteBuffer.allocate(3);
                    dst.read(back, 0);
                    check(back.get(0) == 1 && back.get(2) == 3, "dst content");
                    Path extraFile = dir.resolve("extra.bin");
                    Files.write(extraFile, new byte[] {7, 8});
                    long got;
                    try (FileChannel src = FileChannel.open(extraFile, StandardOpenOption.READ)) {
                        got = dst.transferFrom(src, 3, 2);
                    }
                    check(got == 2, "transferFrom 2");
                    check(dst.size() == 5, "dst size 5 after transferFrom");
                }

                ch.truncate(4);
                MappedByteBuffer mbb = ch.map(FileChannel.MapMode.READ_WRITE, 0, 4);
                check(mbb != null && mbb.capacity() == 4, "mapped buffer capacity 4");
                mbb.put(0, (byte) 42);
                mbb.force();
                ByteBuffer verify = ByteBuffer.allocate(4);
                ch.read(verify, 0);
                check(verify.get(0) == 42, "mapped write visible in file");

                FileLock lock = ch.lock(0, 4, false);
                check(lock != null, "lock acquired");
                check(lock.acquiredBy() == ch, "acquiredBy is the channel");
                check(lock.channel() == ch, "channel() is the channel");
                check(!lock.isShared(), "exclusive lock");
                check(lock.position() == 0 && lock.size() == 4, "lock range");
                check(lock.isValid(), "lock valid");
                lock.release();
                check(!lock.isValid(), "lock invalid after release");
                FileLock tl = ch.tryLock(0, 4, true);
                check(tl != null && tl.isShared(), "tryLock shared");
                check(tl.overlaps(0, 4), "overlaps own range");
                check(!tl.overlaps(10, 4), "no overlap elsewhere");
                tl.close();
                check(!tl.isValid(), "tryLock invalid after close");

                ch.force(true);

                check(ch.position(2) == ch, "position(long) returns this");
                check(ch.truncate(2) instanceof FileChannel, "truncate returns FileChannel");
            }
            check(Files.exists(file), "file persisted after channel close");

            try (FileChannel ro = FileChannel.open(file, StandardOpenOption.READ)) {
                check(ro.size() > 0, "read-only size");
                boolean threw = false;
                try { ro.write(ByteBuffer.wrap(new byte[] {1})); }
                catch (java.nio.channels.NonWritableChannelException e) { threw = true; }
                check(threw, "write on READ channel -> NonWritableChannelException");
                ByteBuffer ok = ByteBuffer.allocate(2);
                check(ro.read(ok) == 2, "read works on READ channel");
            }

            try (FileChannel wo = FileChannel.open(file, StandardOpenOption.WRITE)) {
                boolean threw = false;
                try { wo.read(ByteBuffer.allocate(1)); }
                catch (java.nio.channels.NonReadableChannelException e) { threw = true; }
                check(threw, "read on WRITE channel -> NonReadableChannelException");
            }

            FileChannel cl = FileChannel.open(file, StandardOpenOption.READ);
            cl.close();
            check(!cl.isOpen(), "closed");
            boolean threw = false;
            try { cl.read(ByteBuffer.allocate(1)); }
            catch (java.nio.channels.ClosedChannelException e) { threw = true; }
            check(threw, "read after close -> ClosedChannelException");
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
