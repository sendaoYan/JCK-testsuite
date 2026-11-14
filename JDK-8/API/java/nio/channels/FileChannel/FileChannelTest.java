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
    // 保持映射缓冲区强可达，使“映射存活期间的截断”行为可确定（否则依赖 GC 时机）。
    static Object mappedKeepAlive;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // 清理不是被测契约。Windows 上刚 map 过的文件在句柄真正关闭前删除会抛
    // ERROR_SHARING_VIOLATION；这里重试几次，仍失败就登记 deleteOnExit。
    static void deleteBestEffort(Path p) {
        for (int i = 0; i < 10; i++) {
            try {
                Files.deleteIfExists(p);
                return;
            } catch (IOException e) {
                System.gc();
                try { Thread.sleep(50); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        p.toFile().deleteOnExit();
    }

    static void deleteRecursively(Path p) throws IOException {
        if (Files.isDirectory(p)) {
            try (Stream<Path> s = Files.list(p)) {
                for (Path c : s.collect(Collectors.toList())) deleteRecursively(c);
            }
        }
        deleteBestEffort(p);
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
                // Windows 拒绝对存在用户映射的文件做截断（ERROR_USER_MAPPED_FILE），
                // POSIX 允许并返回本通道。用静态引用保证映射在截断时仍然存活。
                mappedKeepAlive = mbb;
                if (java.io.File.separatorChar == '\\') {
                    try {
                        ch.truncate(2);
                        check(false, "truncate with a live mapping must fail on Windows");
                    } catch (IOException mapped) {
                        check(mapped.getMessage() != null && !mapped.getMessage().isEmpty(),
                              "truncate-while-mapped IOException: " + mapped.getMessage());
                    }
                } else {
                    check(ch.truncate(2) instanceof FileChannel, "truncate returns FileChannel");
                }
                mappedKeepAlive = null;
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

    // ===================== A5 contract deepening =====================

    /** Runnable that may throw checked exceptions (for negative cases). */
    interface IO { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> type, IO r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    // ---- A5 white-box: exact overload surface (javap jdk8u372 实测) ----
    static void testWhiteBoxOverloads() throws Exception {
        Class<?> c = FileChannel.class;
        int openOverloads = 0;
        for (Method m : c.getMethods()) if (m.getName().equals("open")) openOverloads++;
        check(openOverloads == 2, "two open overloads");
        int readOverloads = 0, writeOverloads = 0;
        for (Method m : c.getMethods()) {
            if (m.getName().equals("read")) readOverloads++;
            if (m.getName().equals("write")) writeOverloads++;
        }
        check(readOverloads == 4, "read(ByteBuffer)/read(ByteBuffer,long)/read(ByteBuffer[],int,int)/read(ByteBuffer[]) surface");
        check(writeOverloads == 4, "write surface mirrors read");
        boolean hasArenaMap = false;
        for (Method m : c.getMethods())
            if (m.getName().equals("map") && m.getParameterCount() == 4) hasArenaMap = true;
        check(!hasArenaMap, "JDK-8: no map(MapMode,long,long,Arena) overload (JDK-19+ only)");
        check(Modifier.isFinal(c.getMethod("read", ByteBuffer[].class).getModifiers()),
              "read(ByteBuffer[]) is final (delegates to the (buf,off,len) form)");
        check(Modifier.isFinal(c.getMethod("write", ByteBuffer[].class).getModifiers()),
              "write(ByteBuffer[]) is final");
        check(Modifier.isFinal(c.getMethod("lock").getModifiers()), "lock() is final");
        check(Modifier.isFinal(c.getMethod("tryLock").getModifiers()), "tryLock() is final");
    }

    // ---- A5: open option matrix ----
    static void testOpenOptionMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-open");
        try {
            Path missing = dir.resolve("missing.bin");
            checkThrows(java.nio.file.NoSuchFileException.class,
                    () -> FileChannel.open(missing, StandardOpenOption.READ),
                    "open READ on nonexistent");
            checkThrows(java.nio.file.NoSuchFileException.class,
                    () -> FileChannel.open(missing, StandardOpenOption.WRITE),
                    "open WRITE without CREATE on nonexistent");
            checkThrows(java.nio.file.NoSuchFileException.class,
                    () -> FileChannel.open(missing, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                    "open WRITE+TRUNCATE_EXISTING on nonexistent");

            Path fresh = dir.resolve("fresh.bin");
            try (FileChannel ch = FileChannel.open(fresh, StandardOpenOption.READ,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                check(ch.size() == 0, "READ+WRITE+CREATE creates empty file");
                check(ch.position() == 0, "fresh channel starts at 0");
            }
            check(Files.exists(fresh), "CREATE created the file on disk");

            Path existing = dir.resolve("existing.bin");
            Files.write(existing, new byte[] {1, 2, 3});
            checkThrows(java.nio.file.FileAlreadyExistsException.class,
                    () -> FileChannel.open(existing, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW),
                    "CREATE_NEW on existing");
            Path brandNew = dir.resolve("brand.bin");
            try (FileChannel ch = FileChannel.open(brandNew, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE_NEW)) {
                check(ch.size() == 0, "CREATE_NEW on missing creates empty file");
            }
            check(Files.exists(brandNew), "CREATE_NEW file exists");

            try (FileChannel ch = FileChannel.open(existing, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                check(ch.size() == 0, "TRUNCATE_EXISTING clears prior content");
                check(ch.position() == 0, "position 0 after TRUNCATE_EXISTING");
            }

            // 实测：READ 与 APPEND 组合被直接拒绝（双侧一致）
            checkThrows(IllegalArgumentException.class,
                    () -> FileChannel.open(existing, StandardOpenOption.READ, StandardOpenOption.APPEND),
                    "READ + APPEND combination rejected");

            Path gone = dir.resolve("gone.bin");
            try (FileChannel ch = FileChannel.open(gone, StandardOpenOption.READ,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.DELETE_ON_CLOSE)) {
                check(ch.isOpen(), "DELETE_ON_CLOSE channel open while live");
                check(ch.write(ByteBuffer.wrap(new byte[] {1, 2})) == 2 && ch.size() == 2,
                      "DELETE_ON_CLOSE channel fully usable");
                if (java.io.File.separatorChar == '/') {
                    check(!Files.exists(gone), "on Unix the directory entry is already unlinked at open (实测)");
                } else {
                    // Windows 用 FILE_FLAG_DELETE_ON_CLOSE：最后一个句柄关闭前名字仍可见。
                    check(Files.exists(gone), "Windows keeps the name visible until the handle closes");
                }
            }
            check(!Files.exists(gone), "file stays gone after close");

            // 单参 open == 默认 READ 模式（previous 步骤已清空，重填内容）
            Files.write(existing, new byte[] {4, 5, 6});
            try (FileChannel ch = FileChannel.open(existing)) {
                ByteBuffer b = ByteBuffer.allocate(3);
                check(ch.read(b) == 3, "default-open reads whole small file");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ch.write(ByteBuffer.allocate(1)),
                        "default open is READ: write rejected");
            }
            checkThrows(java.nio.file.NoSuchFileException.class,
                    () -> FileChannel.open(missing),
                    "default open on nonexistent");

            // 关闭后再 open 同一文件：得到新的独立实例
            FileChannel first = FileChannel.open(existing, StandardOpenOption.READ);
            first.close();
            check(!first.isOpen(), "first instance closed");
            FileChannel second = FileChannel.open(existing, StandardOpenOption.READ);
            check(second != first && second.isOpen(), "reopen yields a fresh open instance");
            second.close();
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: relative/absolute read-write, position, size, truncate, force ----
    static void testPositionSemantics() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-pos");
        try {
            Path file = dir.resolve("pos.bin");
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                ch.write(ByteBuffer.wrap(new byte[] {1, 2, 3}));

                ch.position(0);
                check(ch.read(ByteBuffer.allocate(0)) == 0, "read of empty buffer returns 0");
                ByteBuffer posEqLimit = ByteBuffer.allocate(4);
                posEqLimit.position(2).limit(2);
                check(ch.read(posEqLimit) == 0, "read with position==limit returns 0");
                check(ch.write(ByteBuffer.allocate(0)) == 0, "write of empty buffer returns 0");
                ByteBuffer wEq = ByteBuffer.allocate(4);
                wEq.position(1).limit(1);
                check(ch.write(wEq) == 0, "write with position==limit returns 0");
                check(ch.position() == 0, "empty write does not move channel position");

                ByteBuffer big = ByteBuffer.allocate(8);
                check(ch.read(big) == 3, "read returns only the bytes remaining in file");
                check(big.position() == 3, "buffer position advanced by bytes read");

                // 绝对 read：不动 channel position
                ch.position(1);
                ByteBuffer abs = ByteBuffer.allocate(2);
                check(ch.read(abs, 0) == 2, "positional read count");
                check(abs.get(0) == 1 && abs.get(1) == 2, "positional read content");
                check(ch.position() == 1, "positional read leaves channel position");
                check(ch.read(ByteBuffer.allocate(1), 3) == -1, "positional read at size -> -1");
                check(ch.read(ByteBuffer.allocate(1), 100) == -1, "positional read beyond size -> -1");
                checkThrows(IllegalArgumentException.class,
                        () -> ch.read(ByteBuffer.allocate(1), -1), "positional read negative position");
                checkThrows(NullPointerException.class, () -> ch.read((ByteBuffer) null), "read(null) -> NPE");

                // 绝对 write：越过 EOF 扩容，洞为 0
                ch.position(0);
                check(ch.write(ByteBuffer.wrap(new byte[] {9}), 8) == 1, "positional write beyond EOF");
                check(ch.size() == 9, "file extended to 9");
                check(ch.position() == 0, "positional write leaves channel position");
                ByteBuffer hole = ByteBuffer.allocate(9);
                ch.read(hole, 0);
                check(hole.get(7) == 0, "hole byte stays zero");
                check(hole.get(8) == 9, "written byte lands at offset 8");
                checkThrows(IllegalArgumentException.class,
                        () -> ch.write(ByteBuffer.allocate(1), -1), "positional write negative position");
                checkThrows(NullPointerException.class, () -> ch.write((ByteBuffer) null), "write(null) -> NPE");

                ch.position(50);
                check(ch.position() == 50 && ch.size() == 9, "position beyond size allowed, size unchanged");
                ch.position(0);
                checkThrows(IllegalArgumentException.class, () -> ch.position(-1), "position(-1)");

                ch.truncate(5);
                check(ch.size() == 5, "truncate below size shrinks file");
                ch.truncate(50);
                check(ch.size() == 5, "truncate above size is a no-op");
                ch.position(4);
                ch.truncate(2);
                check(ch.size() == 2, "truncate to 2");
                check(ch.position() == 2, "position clamped down to new size");
                ch.truncate(2);
                check(ch.position() == 2, "same-size truncate keeps position");
                ch.truncate(0);
                check(ch.size() == 0, "truncate(0) empties file");

                ch.force(true);
                check(true, "force(true) completes on writable channel");
                ch.force(false);
                check(true, "force(false) completes");
            }

            Files.write(file, new byte[] {7, 7, 7});
            try (FileChannel ro = FileChannel.open(file, StandardOpenOption.READ)) {
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ro.truncate(1), "truncate on READ-only channel");
                ro.force(true);
                check(true, "force on read-only channel is harmless (实测)");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: APPEND mode semantics ----
    static void testAppendMode() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-app");
        try {
            Path file = dir.resolve("app.bin");
            Files.write(file, new byte[] {1, 2, 3, 4});
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                ch.position(0);
                check(ch.write(ByteBuffer.wrap(new byte[] {77})) == 1, "append write count");
                check(ch.position() == 5, "after append write channel position is the new EOF");
                check(ch.size() == 5, "file grew by one byte");
                check(Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 4, 77}),
                      "write went to tail despite position(0)");

                // 实测：APPEND 模式下 position(long) 不抛异常（写仍落尾部）
                ch.position(1);
                check(ch.position() == ch.size(),
                      "APPEND: position(long) accepted but logical position stays at EOF (实测)");
                ch.write(ByteBuffer.wrap(new byte[] {78}));
                check(ch.size() == 6, "write after reposition still appends");
                check(ch.position() == 6, "position reset to new EOF by the append");
                check(Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 4, 77, 78}),
                      "second write also appended");

                ch.write(ByteBuffer.wrap(new byte[] {79}), 0);
                check(ch.size() == 7, "positional write in APPEND mode extends at tail");
                check(Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 4, 77, 78, 79}),
                      "positional write's position argument ignored in APPEND mode (实测)");

                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> ch.read(ByteBuffer.allocate(1)),
                        "read on WRITE+APPEND channel");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: transferTo / transferFrom ----
    static void testTransfer() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-xfer");
        try {
            byte[] srcBytes = new byte[100];
            for (int i = 0; i < 100; i++) srcBytes[i] = (byte) i;
            Path srcP = dir.resolve("src.bin");
            Path dstP = dir.resolve("dst.bin");
            Files.write(srcP, srcBytes);
            try (FileChannel src = FileChannel.open(srcP, StandardOpenOption.READ);
                 FileChannel dst = FileChannel.open(dstP, StandardOpenOption.CREATE_NEW,
                         StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                byte[] pad = new byte[12];
                Arrays.fill(pad, (byte) 0xFF);
                dst.write(ByteBuffer.wrap(pad)); // dst position = 12

                long n = src.transferTo(10, 50, dst);
                check(n == 50, "transferTo transferred exactly 50");
                ByteBuffer snap = ByteBuffer.allocate(64);
                dst.read(snap, 0);
                byte[] arr = snap.array();
                check((arr[10] & 0xFF) == 0xFF && (arr[11] & 0xFF) == 0xFF,
                      "bytes before the target position untouched");
                check(arr[12] == srcBytes[10], "first byte transferred is src[10]");
                check(arr[61] == srcBytes[59], "last byte transferred is src[59]");
                check(dst.size() == 62, "target grew to 62");
                check(dst.position() == 62, "transferTo advanced target channel position");

                check(src.transferTo(90, 50, dst) == 10, "count beyond source EOF moves only the remainder");
                check(src.transferTo(100, 10, dst) == 0, "transferTo at exact source EOF moves 0");
                check(src.transferTo(500, 10, dst) == 0, "transferTo beyond source EOF moves 0");
                checkThrows(IllegalArgumentException.class,
                        () -> src.transferTo(-1, 10, dst), "transferTo negative position");
                checkThrows(IllegalArgumentException.class,
                        () -> src.transferTo(0, -1, dst), "transferTo negative count");

                // transferFrom：绝对位置写入，目标 channel position 不变
                dst.position(3);
                check(dst.transferFrom(src, 0, 5) == 5, "transferFrom count");
                check(dst.position() == 3, "transferFrom leaves target channel position");
                ByteBuffer head = ByteBuffer.allocate(5);
                dst.read(head, 0);
                check(Arrays.equals(head.array(), Arrays.copyOf(srcBytes, 5)),
                      "transferFrom wrote at absolute position 0");
                checkThrows(IllegalArgumentException.class,
                        () -> dst.transferFrom(src, -1, 5), "transferFrom negative position");
                checkThrows(IllegalArgumentException.class,
                        () -> dst.transferFrom(src, 0, -5), "transferFrom negative count");
                src.position(90);
                check(dst.transferFrom(src, 60, 100) == 10, "transferFrom count beyond source remaining -> 10");
                src.position(0);

                // 版本差异（实测）：目标位置越过 EOF 时 JDK-8 不写入、不扩容
                long nz = dst.transferFrom(src, 5000, 10);
                check(nz == 0, "transferFrom beyond target EOF transfers 0 (JDK-8 实测)");
                check(dst.size() == 72, "JDK-8 does not extend file for beyond-EOF transferFrom");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: map parameter matrix ----
    static void testMapMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-map");
        try {
            Path file = dir.resolve("m.bin");
            Files.write(file, new byte[] {1, 2, 3, 4});
            try (FileChannel ro = FileChannel.open(file, StandardOpenOption.READ)) {
                checkThrows(IllegalArgumentException.class,
                        () -> ro.map(FileChannel.MapMode.READ_ONLY, -1, 4), "map negative position");
                checkThrows(IllegalArgumentException.class,
                        () -> ro.map(FileChannel.MapMode.READ_ONLY, 0, -1), "map negative size");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ro.map(FileChannel.MapMode.READ_WRITE, 0, 4),
                        "READ channel map READ_WRITE");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> ro.map(FileChannel.MapMode.PRIVATE, 0, 4),
                        "READ channel map PRIVATE");
                checkThrows(java.io.IOException.class,
                        () -> ro.map(FileChannel.MapMode.READ_ONLY, 0, 8),
                        "READ_ONLY map beyond EOF");
                checkThrows(java.io.IOException.class,
                        () -> ro.map(FileChannel.MapMode.READ_ONLY, 5, 0),
                        "READ_ONLY map past EOF even with size 0 (实测)");
                ByteBuffer zero = ro.map(FileChannel.MapMode.READ_ONLY, 4, 0);
                check(zero.capacity() == 0, "zero-size map at EOF returns empty buffer");
                check(zero.isDirect(), "mapped buffer is direct");
                check(zero.isReadOnly(), "READ_ONLY map buffer is read-only");
                checkThrows(NullPointerException.class, () -> ro.map(null, 0, 4), "map(null mode)");
                ByteBuffer m = ro.map(FileChannel.MapMode.READ_ONLY, 1, 2);
                check(m.capacity() == 2, "mapped subrange capacity");
                check(m.get(0) == 2 && m.get(1) == 3, "mapped subrange content");
            }
            try (FileChannel rw = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                java.nio.MappedByteBuffer ext = rw.map(FileChannel.MapMode.READ_WRITE, 2, 8);
                check(rw.size() == 10, "READ_WRITE map beyond EOF extends file");
                check(ext.capacity() == 8, "extended map capacity");
                check(ext.get(0) == 3, "extended map starts at file position 2");
                check(ext.get(4) == 0, "extension hole reads zero");
                ext.put(7, (byte) 55);
                ext.force();
                ByteBuffer tail = ByteBuffer.allocate(2);
                rw.read(tail, 9);
                check(tail.get(0) == 55, "mapped write beyond old EOF persisted");
            }
            Path priv = dir.resolve("priv.bin");
            Files.write(priv, new byte[] {9, 9});
            try (FileChannel rw = FileChannel.open(priv, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                rw.map(FileChannel.MapMode.PRIVATE, 0, 6);
                check(rw.size() == 6, "PRIVATE map beyond EOF also extends file (实测)");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: lock matrix ----
    static void testLockMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-lock");
        try {
            Path file = dir.resolve("lock.bin");
            Files.write(file, new byte[100]);
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                FileLock lock = ch.lock(0, 10, false);
                check(lock.acquiredBy() == ch, "acquiredBy is the channel");
                checkThrows(java.nio.channels.OverlappingFileLockException.class,
                        () -> ch.lock(5, 10, false), "overlapping lock");
                checkThrows(java.nio.channels.OverlappingFileLockException.class,
                        () -> ch.tryLock(5, 0, false),
                        "zero-length tryLock inside a held range still overlaps (实测)");
                // 版本差异（实测）：持 (0,10) 时 lock(0,0) —— JDK-8 成功（零长锁不重叠）、JDK-27 抛 OFLE
                FileLock z = ch.lock(0, 0, false);
                check(z.position() == 0 && z.size() == 0, "JDK-8: zero-length lock at range start succeeds");
                z.release();
                checkThrows(IllegalArgumentException.class,
                        () -> ch.lock(-1, 10, false), "lock negative position");
                checkThrows(IllegalArgumentException.class,
                        () -> ch.lock(0, -1, false), "lock negative size");
                checkThrows(IllegalArgumentException.class,
                        () -> ch.tryLock(-1, 10, false), "tryLock negative position");
                checkThrows(IllegalArgumentException.class,
                        () -> ch.tryLock(0, -1, false), "tryLock negative size");

                lock.release();
                check(!lock.isValid(), "invalid after release");
                lock.release();
                check(!lock.isValid(), "second release is a harmless no-op (实测幂等)");
                FileLock again = ch.tryLock(0, 10, false);
                check(again != null && again.isValid(), "range re-lockable after release");

                // 同 JVM 内锁冲突按 (file, JVM) 计，与 channel 实例无关（实测）
                try (FileChannel ch2 = FileChannel.open(file, StandardOpenOption.READ,
                        StandardOpenOption.WRITE)) {
                    checkThrows(java.nio.channels.OverlappingFileLockException.class,
                            () -> ch2.tryLock(5, 10, false),
                            "overlap via a second channel of the same file");
                    FileLock other = ch2.tryLock(50, 10, false);
                    check(other != null && other.acquiredBy() == ch2,
                          "non-overlapping range via second channel succeeds");
                    other.release();
                }
                again.release();

                FileLock beyond = ch.tryLock(1000, 10, false);
                check(beyond != null && beyond.isValid(), "lock beyond file size is legal");
                beyond.release();

                FileLock shared = ch.tryLock(0, 10, true);
                check(shared.isShared(), "tryLock shared on READ+WRITE channel");
                shared.release();

                FileLock whole = ch.lock();
                check(whole.position() == 0 && whole.size() == Long.MAX_VALUE,
                      "no-arg lock() covers the whole file");
                whole.release();

                // JDK-8 实测：零长锁保持字面 (pos,0)，不阻塞其它范围
                FileLock z0 = ch.tryLock(5, 0, false);
                check(z0.position() == 5 && z0.size() == 0, "JDK-8 keeps literal zero-size lock");
                FileLock neighbor = ch.tryLock(50, 1, false);
                check(neighbor != null, "JDK-8: zero-length lock does not block other ranges");
                neighbor.release();
                z0.release();
            }

            // WRITE-only channel：共享锁需要读权限
            try (FileChannel wo = FileChannel.open(file, StandardOpenOption.WRITE)) {
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> wo.tryLock(0, 10, true), "shared lock on WRITE-only channel");
                FileLock excl = wo.tryLock(0, 10, false);
                check(excl != null && !excl.isShared(), "exclusive lock on WRITE-only channel");
                FileLock extra = wo.tryLock(20, 5, false);
                wo.close();
                check(!excl.isValid() && !extra.isValid(), "channel close invalidates every lock");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> excl.release(), "release after channel close");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: closed-channel exception matrix + idempotent close ----
    static void testClosedChannelMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-closed");
        try {
            Path file = dir.resolve("c.bin");
            Files.write(file, new byte[] {1, 2});
            Path file2 = dir.resolve("c2.bin");
            Files.write(file2, new byte[] {3});
            FileChannel ch = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE);
            ch.close();
            check(!ch.isOpen(), "closed channel reports closed");
            ch.close();
            check(!ch.isOpen(), "close is idempotent (second close does not throw)");
            ByteBuffer b = ByteBuffer.allocate(1);
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.read(b), "read after close");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.write(b), "write after close");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> ch.read(ByteBuffer.allocate(1), 0), "positional read after close");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> ch.write(ByteBuffer.allocate(1), 0), "positional write after close");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}), "scatter read after close");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> ch.write(new ByteBuffer[] {ByteBuffer.allocate(1)}), "gather write after close");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.position(), "position() after close");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.position(0), "position(n) after close");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.size(), "size() after close");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.truncate(1), "truncate after close");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.force(true), "force after close");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.lock(), "lock after close");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> ch.tryLock(), "tryLock after close");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> ch.map(FileChannel.MapMode.READ_ONLY, 0, 1), "map after close");
            try (FileChannel other = FileChannel.open(file2, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> ch.transferTo(0, 1, other), "transferTo after close");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> other.transferFrom(ch, 0, 1), "transferFrom with closed source");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: gathering write / scattering read details ----
    static void testGatherScatterExtras() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-gs");
        try {
            Path file = dir.resolve("gs.bin");
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                check(ch.write(new ByteBuffer[] {ByteBuffer.wrap(new byte[] {1, 2, 3})}) == 3,
                      "single-element gather write");
                ByteBuffer b = ByteBuffer.wrap(new byte[] {4, 5});
                ByteBuffer c = ByteBuffer.wrap(new byte[] {6, 7, 8});
                check(ch.write(new ByteBuffer[] {b, c}) == 5, "multi gather write total");
                check(ch.position() == 8 && ch.size() == 8, "channel advanced by gathered bytes");
                check(Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 4, 5, 6, 7, 8}),
                      "gather writes concatenate in array order");

                ByteBuffer one = ByteBuffer.wrap(new byte[] {9});
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.write(new ByteBuffer[] {one}, -1, 1), "write(arr,-1,1)");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.write(new ByteBuffer[] {one}, 0, 2), "write(arr,0,len too big)");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.write(new ByteBuffer[] {one}, 2, 1), "write(arr,offset past end)");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.write(new ByteBuffer[] {one}, 1, -1), "write(arr,1,-1)");
                checkThrows(NullPointerException.class,
                        () -> ch.write(new ByteBuffer[] {null}, 0, 1), "write(arr with null element)");
                check(ch.write(new ByteBuffer[] {one}, 1, 0) == 0, "write(arr,1,0) zero length -> 0");

                ByteBuffer full = ByteBuffer.allocate(2);
                full.position(2);
                ByteBuffer live = ByteBuffer.wrap(new byte[] {11});
                check(ch.write(new ByteBuffer[] {full, live}) == 1, "full buffers skipped in gather");

                // 实测：FileChannel 的 gather write 不拒绝只读 buffer，字节照常写出
                ByteBuffer readOnly = ByteBuffer.allocate(2).asReadOnlyBuffer();
                check(ch.write(new ByteBuffer[] {readOnly}) == 2,
                      "gather write accepts read-only buffer element (实测写入)");

                ch.position(0);
                ByteBuffer r1 = ByteBuffer.allocate(4);
                ByteBuffer r2 = ByteBuffer.allocate(4);
                check(ch.read(new ByteBuffer[] {r1, r2}) == 8, "scatter read fills both buffers");
                check(r1.get(0) == 1 && r1.get(3) == 4, "first scatter chunk");
                check(r2.get(0) == 5 && r2.get(3) == 8, "second scatter chunk");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}, -1, 1), "read(arr,-1,1)");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}, 0, 5), "read(arr,0,len too big)");
                checkThrows(NullPointerException.class,
                        () -> ch.read(new ByteBuffer[] {null}, 0, 1), "read(arr with null element)");
                check(ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}, 1, 0) == 0, "read(arr,1,0) -> 0");
                ByteBuffer f1 = ByteBuffer.allocate(1);
                f1.position(1);
                ByteBuffer f2 = ByteBuffer.allocate(1);
                f2.position(1);
                check(ch.read(new ByteBuffer[] {f1, f2}) == 0, "scatter read with all-full buffers -> 0");
                ch.position((int) ch.size());
                check(ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}) == -1, "scatter read at EOF -> -1");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxOverloads();
        testOpenOptionMatrix();
        testPositionSemantics();
        testAppendMode();
        testTransfer();
        testMapMatrix();
        testLockMatrix();
        testClosedChannelMatrix();
        testGatherScatterExtras();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
