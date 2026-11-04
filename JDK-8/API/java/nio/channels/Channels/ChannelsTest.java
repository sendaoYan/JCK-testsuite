/*
 * @test
 * @summary java.nio.channels.Channels conformance: white-box structure + black-box behavior (JDK-8).
 * @run main ChannelsTest
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChannelsTest {
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
        Class<?> c = Class.forName("java.nio.channels.Channels");
        check(Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()),
              "Channels is a final utility class");
        check(c.getDeclaredConstructors().length == 0 || Arrays.stream(c.getDeclaredConstructors())
              .allMatch(ct -> Modifier.isPrivate(ct.getModifiers())), "no public ctor");
        check(Arrays.stream(c.getMethods()).filter(m -> !m.getDeclaringClass().equals(Object.class))
              .allMatch(m -> Modifier.isStatic(m.getModifiers())), "all methods static");
        for (String n : new String[] {"newInputStream", "newOutputStream", "newChannel",
                "newReader", "newWriter"})
            check(Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has factory " + n);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("channels.bin");

            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.CREATE,
                    StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                OutputStream os = Channels.newOutputStream(ch);
                os.write(new byte[] {1, 2, 3});
                os.write(4);
                os.flush();
                check(ch.size() == 4, "stream wrote 4 bytes through channel");
                ch.position(0);
                InputStream is = Channels.newInputStream(ch);
                check(is.read() == 1, "first byte via stream");
                byte[] rest = new byte[3];
                int n = is.read(rest, 0, 3);
                check(n == 3, "bulk read 3");
                check(rest[0] == 2 && rest[2] == 4, "stream read content");
                check(is.read() == -1, "EOF at end");
                check(!is.markSupported(), "channel input stream does not support mark");
                check(is.available() == 0, "nothing available at EOF");
            }

            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(new byte[] {9, 8, 7});
            ReadableByteChannel rbc = Channels.newChannel(bais);
            ByteBuffer b = ByteBuffer.allocate(3);
            while (b.hasRemaining()) {
                int r = rbc.read(b);
                check(r > 0, "channel read from stream");
            }
            check(b.get(0) == 9 && b.get(2) == 7, "stream->channel content");
            check(rbc.read(ByteBuffer.allocate(1)) == -1, "channel EOF at stream end");
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            WritableByteChannel wbc = Channels.newChannel(baos);
            ByteBuffer o = ByteBuffer.wrap(new byte[] {5, 6});
            while (o.hasRemaining()) wbc.write(o);
            check(baos.toByteArray()[0] == 5 && baos.toByteArray()[1] == 6,
                  "channel->stream content");

            Path text = dir.resolve("text.txt");
            try (WritableByteChannel wch = FileChannel.open(text, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                Writer w = Channels.newWriter(wch, StandardCharsets.UTF_8.newEncoder(), 8192);
                w.write("hello");
                w.write(22835); // fixed BMP code point used on both sides of the assertion
                w.flush();
            }
            try (ReadableByteChannel rch = FileChannel.open(text, StandardOpenOption.READ)) {
                Reader r = Channels.newReader(rch, StandardCharsets.UTF_8.newDecoder(), 8192);
                char[] buf = new char[6]; // "hello" + one CJK char
                int got = 0;
                while (got < buf.length) {
                    int k = r.read(buf, got, buf.length - got);
                    check(k > 0, "reader produced data");
                    got += k;
                }
                String expect = "hello" + (char) 22835;
                check(new String(buf).equals(expect), "reader decoded UTF-8");
                check(r.read() == -1, "reader EOF after content");
            }
            check(new String(Files.readAllBytes(text), StandardCharsets.UTF_8)
                  .equals("hello" + (char) 22835), "writer encoded UTF-8");

            try (ReadableByteChannel rch = FileChannel.open(text, StandardOpenOption.READ)) {
                Reader r2 = Channels.newReader(rch, StandardCharsets.UTF_8.newDecoder(), 8);
                int first = r2.read();
                check(first == 'h', "decoder reader first char");
            }

            try (ReadableByteChannel rch = FileChannel.open(text, StandardOpenOption.READ)) {
                Reader r3 = Channels.newReader(rch, "UTF-8");
                check(r3.read() == 'h', "string-charset reader");
            }
            try (WritableByteChannel wch = FileChannel.open(dir.resolve("t2.txt"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                Writer w3 = Channels.newWriter(wch, "UTF-8");
                w3.write("ok");
                w3.flush();
            }
            check(new String(Files.readAllBytes(dir.resolve("t2.txt")), StandardCharsets.UTF_8)
                  .equals("ok"), "string-charset writer");

            try (AsynchronousFileChannelShim shim = new AsynchronousFileChannelShim(file)) {
                InputStream ais = Channels.newInputStream(shim);
                int v = ais.read();
                check(v == 1, "async stream adapter read");
                OutputStream aos = Channels.newOutputStream(new AsynchronousFileChannelShim(
                        dir.resolve("async-out.bin"), true));
                aos.write(42);
                aos.close();
                check(Files.readAllBytes(dir.resolve("async-out.bin"))[0] == 42,
                      "async stream adapter wrote");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    /** Minimal AsynchronousByteChannel built on a FileChannel for adapter tests. */
    static final class AsynchronousFileChannelShim implements AsynchronousByteChannel, AutoCloseable {
        private final FileChannel ch;
        AsynchronousFileChannelShim(Path p, boolean create) throws IOException {
            ch = FileChannel.open(p, create
                    ? new StandardOpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.WRITE}
                    : new StandardOpenOption[] {StandardOpenOption.READ});
        }
        AsynchronousFileChannelShim(Path p) throws IOException { this(p, false); }
        public void close() throws IOException { ch.close(); }
        public boolean isOpen() { return ch.isOpen(); }
        public Future<Integer> read(ByteBuffer dst) {
            CompletableFuture<Integer> fut = new CompletableFuture<Integer>();
            try { fut.complete(ch.read(dst)); } catch (IOException e) { fut.completeExceptionally(e); }
            return fut;
        }
        public Future<Integer> write(ByteBuffer src) {
            CompletableFuture<Integer> fut = new CompletableFuture<Integer>();
            try { fut.complete(ch.write(src)); } catch (IOException e) { fut.completeExceptionally(e); }
            return fut;
        }
        public <A> void read(ByteBuffer dst, A att, CompletionHandler<Integer, ? super A> h) {
            try { h.completed(ch.read(dst), att); } catch (IOException e) { h.failed(e, att); }
        }
        public <A> void write(ByteBuffer src, A att, CompletionHandler<Integer, ? super A> h) {
            try { h.completed(ch.write(src), att); } catch (IOException e) { h.failed(e, att); }
        }
    }

    // ===================== A5 contract deepening =====================

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

    // ---- A5: newInputStream adapter contract ----
    static void testInputStreamAdapter() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-is");
        try {
            Path file = dir.resolve("in.bin");
            Files.write(file, new byte[] {10, 20, 30});
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
                InputStream is = Channels.newInputStream(ch);
                check(is.available() == 3, "available == bytes remaining");
                check(is.read() == 10, "single byte exact");
                check(is.available() == 2, "available decrements");
                byte[] all = new byte[4];
                check(is.read(all) == 2, "bulk read to EOF returns remaining");
                check(all[0] == 20 && all[1] == 30, "bulk read content");
                check(is.available() == 0, "available 0 at EOF");
                check(is.read() == -1, "read at EOF -> -1");
                check(is.read(new byte[2]) == -1, "bulk read at EOF -> -1");
                check(is.read(new byte[2], 1, 0) == 0, "len 0 read returns 0");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> is.read(new byte[2], -1, 1), "read(b,-1,1) -> IndexOOB");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> is.read(new byte[2], 0, 5), "read(b,0,5) -> IndexOOB");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> is.read(new byte[2], 3, 1), "read(b,3,1) -> IndexOOB");
                checkThrows(java.io.IOException.class, () -> is.reset(),
                        "reset unsupported -> IOException");
                check(!is.markSupported(), "mark unsupported");
            }
            // skip 只前进到 EOF
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
                InputStream is = Channels.newInputStream(ch);
                check(is.skip(2) == 2, "skip(2) skips 2");
                check(is.read() == 30, "read after skip lands on 3rd byte");
                check(is.skip(100) == 0, "skip at EOF skips 0");
            }
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
                InputStream is = Channels.newInputStream(ch);
                check(is.skip(100) == 3, "skip beyond EOF clamps to remaining");
                check(is.read() == -1, "EOF after clamped skip");
                is.close();
                check(!ch.isOpen(), "stream close closes the channel");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> is.read(), "read after stream close -> CCE");
                is.close();
                check(true, "double stream close tolerated");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: newOutputStream adapter contract ----
    static void testOutputStreamAdapter() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-os");
        try {
            Path file = dir.resolve("out.bin");
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                OutputStream os = Channels.newOutputStream(ch);
                os.write(new byte[] {1, 2, 3});
                os.write(new byte[] {4, 5, 6, 7}, 1, 2); // off/len slice
                os.write(8);
                os.write(new byte[0]);
                os.write(new byte[2], 1, 0);
                os.flush();
                check(ch.isOpen(), "flush does not close the channel");
                check(Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 5, 6, 8}),
                      "stream writes land in the file in order");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> os.write(new byte[2], -1, 1), "write(b,-1,1) -> IndexOOB");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> os.write(new byte[2], 0, 5), "write(b,0,5) -> IndexOOB");
                os.close();
                check(!ch.isOpen(), "stream close closes the channel");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> os.write(9), "write after close -> CCE");
                os.flush();
                check(true, "flush after close tolerated (实测)");
            }
            // 只读 channel 上的流适配：写直接 NonWritableChannelException
            try (FileChannel ro = FileChannel.open(file, StandardOpenOption.READ)) {
                OutputStream os = Channels.newOutputStream(ro);
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> os.write(1), "write via stream on READ channel");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: newChannel adapters close semantics ----
    static void testChannelAdapters() throws Exception {
        // 正常路径：读尽 + EOF
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(new byte[] {1, 2});
        ReadableByteChannel rbc = Channels.newChannel(bais);
        check(rbc.isOpen(), "stream-backed channel open");
        ByteBuffer b = ByteBuffer.allocate(2);
        check(rbc.read(b) == 2, "stream-backed channel drains stream");
        check(b.get(0) == 1 && b.get(1) == 2, "stream-backed content");
        check(rbc.read(ByteBuffer.allocate(1)) == -1, "EOF -> -1");

        // 版本差异（实测）：close 后立即 read —— JDK-8 透传底层流不抛；JDK-27 抛 ClosedChannelException
        java.io.ByteArrayInputStream bais2 = new java.io.ByteArrayInputStream(new byte[] {5});
        ReadableByteChannel closed = Channels.newChannel(bais2);
        closed.close();
        check(!closed.isOpen(), "adapter channel closed");
        ByteBuffer after = ByteBuffer.allocate(1);
        closed.read(after);
        check(true, "JDK-8: read right after close still delegates to the stream (实测)");

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        WritableByteChannel wbc = Channels.newChannel(baos);
        check(wbc.isOpen(), "stream-backed writable channel open");
        wbc.write(ByteBuffer.wrap(new byte[] {7, 8}));
        check(baos.toByteArray()[0] == 7 && baos.toByteArray()[1] == 8, "writable adapter drains");

        java.io.ByteArrayOutputStream baos2 = new java.io.ByteArrayOutputStream();
        WritableByteChannel wbc2 = Channels.newChannel(baos2);
        wbc2.close();
        check(!wbc2.isOpen(), "writable adapter closed");
        wbc2.write(ByteBuffer.wrap(new byte[] {9}));
        check(baos2.toByteArray().length == 1 && baos2.toByteArray()[0] == 9,
              "JDK-8: write right after close still delegates (实测)");
    }

    // ---- A5: reader/writer charset contracts ----
    static void testReaderWriterDeep() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-rw");
        try {
            // 中文 + emoji 往返（String charset 与 decoder/encoder 重载等价）
            String text = "中文😀end";
            Path t1 = dir.resolve("t1.txt");
            try (WritableByteChannel w = FileChannel.open(t1, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE)) {
                Writer wr = Channels.newWriter(w, "UTF-8");
                wr.write(text);
                wr.flush();
            }
            try (ReadableByteChannel r = FileChannel.open(t1, StandardOpenOption.READ)) {
                Reader rr = Channels.newReader(r, "UTF-8");
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[3];
                int n;
                while ((n = rr.read(buf, 0, buf.length)) != -1) sb.append(buf, 0, n);
                check(sb.toString().equals(text), "reader round-trips CJK + emoji via String charset");
                check(rr.read() == -1, "reader EOF after full content");
            }
            Path t2 = dir.resolve("t2.txt");
            try (WritableByteChannel w = FileChannel.open(t2, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE)) {
                Writer wr = Channels.newWriter(w, StandardCharsets.UTF_8.newEncoder(), 4);
                wr.write(text);
                wr.flush();
            }
            try (ReadableByteChannel r = FileChannel.open(t2, StandardOpenOption.READ)) {
                Reader rr = Channels.newReader(r, StandardCharsets.UTF_8.newDecoder(), 4);
                StringBuilder sb = new StringBuilder();
                int c;
                while ((c = rr.read()) != -1) sb.append((char) c);
                check(sb.toString().equals(text), "encoder/decoder overloads round-trip equally");
            }

            // 单字符读取与 ready
            try (ReadableByteChannel r = FileChannel.open(t1, StandardOpenOption.READ)) {
                Reader rr = Channels.newReader(r, "UTF-8");
                check(rr.read() == '中', "first char exact");
                check(rr.ready(), "reader ready with data left");
                check(rr.read(new char[0], 0, 0) == 0, "len 0 read returns 0");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> rr.read(new char[2], 0, 5), "reader read OOB");
            }

            // REPLACE 行为：畸形 UTF-8 解码为 U+FFFD
            Path bad = dir.resolve("bad.bin");
            Files.write(bad, new byte[] {0x41, (byte) 0xC3, 0x28, 0x42});
            try (ReadableByteChannel r = FileChannel.open(bad, StandardOpenOption.READ)) {
                java.nio.charset.CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE);
                Reader rr = Channels.newReader(r, dec, 16);
                StringBuilder sb = new StringBuilder();
                int c;
                while ((c = rr.read()) != -1) sb.append((char) c);
                check(sb.toString().equals("A�(B"),
                      "REPLACE decoder emits U+FFFD for malformed sequence");
            }

            // writer close 联动 channel
            Path t3 = dir.resolve("t3.txt");
            FileChannel wch = FileChannel.open(t3, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
            Writer wr = Channels.newWriter(wch, "UTF-8");
            wr.write("x");
            wr.flush();
            check(wch.isOpen(), "writer flush keeps channel open");
            wr.close();
            check(!wch.isOpen(), "writer close closes the channel");
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: overload surface (JDK-8 无 Charset 重载，白盒确认) ----
    static void testOverloadSurface() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.Channels");
        boolean hasCharsetReader = false, hasCharsetWriter = false;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (!m.getName().equals("newReader") && !m.getName().equals("newWriter")) continue;
            Class<?>[] ps = m.getParameterTypes();
            if (m.getName().equals("newReader") && ps.length == 2 && ps[1] == java.nio.charset.Charset.class)
                hasCharsetReader = true;
            if (m.getName().equals("newWriter") && ps.length == 2 && ps[1] == java.nio.charset.Charset.class)
                hasCharsetWriter = true;
        }
        check(!hasCharsetReader, "JDK-8: no newReader(ReadableByteChannel,Charset) overload");
        check(!hasCharsetWriter, "JDK-8: no newWriter(WritableByteChannel,Charset) overload");
        int readerOverloads = 0, writerOverloads = 0;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals("newReader")) readerOverloads++;
            if (m.getName().equals("newWriter")) writerOverloads++;
        }
        check(readerOverloads == 2, "JDK-8 exposes two newReader overloads");
        check(writerOverloads == 2, "JDK-8 exposes two newWriter overloads");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInputStreamAdapter();
        testOutputStreamAdapter();
        testChannelAdapters();
        testReaderWriterDeep();
        testOverloadSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
