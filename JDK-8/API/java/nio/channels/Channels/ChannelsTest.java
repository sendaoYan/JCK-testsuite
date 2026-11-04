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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
