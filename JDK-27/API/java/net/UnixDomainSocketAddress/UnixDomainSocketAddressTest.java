/*
 * @test
 * @summary java.net.UnixDomainSocketAddress conformance: white-box structure + black-box behavior.
 * @library /lib
 * @run main UnixDomainSocketAddressTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UnixDomainSocketAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.UnixDomainSocketAddress");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(SocketAddress.class.isAssignableFrom(c), "extends SocketAddress");
        check(c.getMethod("of", String.class) != null, "of(String)");
        check(c.getMethod("of", Path.class) != null, "of(Path)");
        check(c.getMethod("getPath") != null, "getPath");
    }

    static void testBlackBox() throws Exception {
        // of(String) is by contract of(Path.of(s)): the text is rendered by the
        // default file system, which uses '\\' separators on Windows.
        Path sample = Path.of("/tmp/jck-sock");
        UnixDomainSocketAddress u = UnixDomainSocketAddress.of(sample);
        check(u.getPath().equals(sample), "path from string");
        check(u.toString().equals(sample.toString()), "toString");

        UnixDomainSocketAddress u2 = UnixDomainSocketAddress.of(Paths.get("/tmp/jck-sock"));
        check(u2.getPath().equals(u.getPath()), "path from Path");

        check(u.equals(UnixDomainSocketAddress.of("/tmp/jck-sock")), "equals");
        check(u.hashCode() == UnixDomainSocketAddress.of("/tmp/jck-sock").hashCode(), "hashCode");
        check(!u.equals(UnixDomainSocketAddress.of("/tmp/other")), "not equals different path");
        check(!u.equals(new InetSocketAddress(1)), "not equals other type");

        fails(NullPointerException.class, "of null string", () -> UnixDomainSocketAddress.of((String) null));
        fails(NullPointerException.class, "of null path", () -> UnixDomainSocketAddress.of((Path) null));
        fails(InvalidPathException.class, "embedded null char", () -> UnixDomainSocketAddress.of("\u0000bad"));
    }

    // ====================== deepening below ======================

    // ---- Part 1: white-box shape, full precision (probed on jdk-27.0.1.0.2+1) ----
    static void testWhiteBoxShape() throws Exception {
        Class<?> c = Class.forName("java.net.UnixDomainSocketAddress");
        check(c.getSuperclass() == SocketAddress.class, "direct superclass is SocketAddress");
        check(Serializable.class.isAssignableFrom(c), "Serializable via SocketAddress");
        // Probed: NO compareTo in JDK-27 - the type is NOT Comparable (task brief
        // assumed Comparable<SocketAddress>; javap + reflection prove otherwise).
        try { c.getMethod("compareTo", Object.class); check(false, "compareTo(Object) must be absent"); }
        catch (NoSuchMethodException e) { check(true, "compareTo(Object) absent (probed)"); }
        try { c.getMethod("compareTo", SocketAddress.class); check(false, "compareTo(SocketAddress) must be absent"); }
        catch (NoSuchMethodException e) { check(true, "compareTo(SocketAddress) absent (probed)"); }
        check(!Comparable.class.isAssignableFrom(c), "does not implement Comparable");

        // Factory-only construction: single private constructor taking Path (probed).
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly one declared constructor");
        check(Modifier.isPrivate(ctors[0].getModifiers()), "constructor is private");
        check(ctors[0].getParameterCount() == 1 && ctors[0].getParameterTypes()[0] == Path.class,
                "private ctor takes a single Path");

        // Serialization plumbing is present (probed): writeReplace + rejected readObject paths.
        Method writeReplace = c.getDeclaredMethod("writeReplace");
        check(Modifier.isPrivate(writeReplace.getModifiers()), "writeReplace is private");
        check(writeReplace.getReturnType() == Object.class, "writeReplace returns Object");
        check(writeReplace.getExceptionTypes().length == 1
                && writeReplace.getExceptionTypes()[0] == ObjectStreamException.class,
                "writeReplace throws ObjectStreamException");
        Method readObject = c.getDeclaredMethod("readObject", ObjectInputStream.class);
        check(Modifier.isPrivate(readObject.getModifiers()), "readObject is private");
        check(readObject.getExceptionTypes()[0] == InvalidObjectException.class,
                "readObject declares throws InvalidObjectException (direct deserialization forbidden)");
        Method readObjectNoData = c.getDeclaredMethod("readObjectNoData");
        check(Modifier.isPrivate(readObjectNoData.getModifiers()), "readObjectNoData is private");
        check(readObjectNoData.getExceptionTypes()[0] == InvalidObjectException.class,
                "readObjectNoData declares throws InvalidObjectException");

        // serialVersionUID (probed value).
        ObjectStreamClass osc = ObjectStreamClass.lookup(c);
        check(osc != null, "ObjectStreamClass lookup non-null");
        check(osc.getSerialVersionUID() == 92902496589351288L,
                "serialVersionUID == 92902496589351288L (probed)");

        // Static factory signatures.
        Method ofString = c.getMethod("of", String.class);
        check(Modifier.isStatic(ofString.getModifiers()) && Modifier.isPublic(ofString.getModifiers()),
                "of(String) public static");
        check(ofString.getReturnType() == c, "of(String) returns UnixDomainSocketAddress");
        Method ofPath = c.getMethod("of", Path.class);
        check(Modifier.isStatic(ofPath.getModifiers()) && Modifier.isPublic(ofPath.getModifiers()),
                "of(Path) public static");
        check(ofPath.getReturnType() == c, "of(Path) returns UnixDomainSocketAddress");
        check(c.getMethod("getPath").getReturnType() == Path.class, "getPath returns Path");
        check(Modifier.isPublic(c.getMethod("getPath").getModifiers()), "getPath public");
        check(c.getConstructors().length == 0, "no public constructors (factory-only)");
    }

    // ---- Part 2: of(String) path matrix (all probed round-trips) ----
    static void testOfStringMatrix() {
        String[] samples = {"/tmp/a.sock", "/a/b/c/d.sock", "a.sock", "rel/sub/b.sock", "",
                "/tmp/a b.sock", "/tmp/日本語.sock", "/tmp/./a.sock", "/tmp/x.sock/"};
        for (int i = 0; i < samples.length; i++) {
            // The contract is of(String) == of(Path.of(String)); the concrete
            // text (separators, '.' handling, absolute-ness of a drive-less
            // rooted path) is supplied by the platform default file system.
            Path expected = Path.of(samples[i]);
            UnixDomainSocketAddress a = UnixDomainSocketAddress.of(samples[i]);
            check(a != null, "of sample " + i + " non-null");
            check(expected.toString().equals(a.getPath().toString()),
                    "round-trip[" + i + "] <" + samples[i] + "> -> <" + a.getPath() + ">");
            check(expected.toString().equals(a.toString()), "toString[" + i + "] equals path text");
            check(a.getPath().isAbsolute() == expected.isAbsolute(), "isAbsolute[" + i + "]");
        }
        // Trailing slash is collapsed by Path semantics (probed).
        check(Path.of("/tmp/x.sock").toString()
                        .equals(UnixDomainSocketAddress.of("/tmp/x.sock/").getPath().toString()),
                "trailing slash collapsed");
        // Deeply nested path keeps all components.
        List<String> names = new ArrayList<String>();
        for (Path p : UnixDomainSocketAddress.of("/x/y/z/w.sock").getPath()) names.add(p.toString());
        check(names.equals(java.util.Arrays.asList("x", "y", "z", "w.sock")), "path components intact");
    }

    // ---- Part 3: of(Path) semantics ----
    static void testOfPath() {
        Path abs = Path.of("/tmp", "deep", "s.sock");
        UnixDomainSocketAddress a = UnixDomainSocketAddress.of(abs);
        check(a.getPath().equals(abs), "of(Path) keeps the path value");
        check(a.getPath() == abs, "of(Path) stores the very same Path reference (probed)");
        check(a.equals(UnixDomainSocketAddress.of("/tmp/deep/s.sock")),
                "of(Path) equal to of(String) with same text");
        check(a.hashCode() == UnixDomainSocketAddress.of("/tmp/deep/s.sock").hashCode(),
                "hashCode consistent across factory overloads");
        Path rel = Path.of("rel", "r.sock");
        UnixDomainSocketAddress b = UnixDomainSocketAddress.of(rel);
        check(!b.getPath().isAbsolute(), "relative Path stays relative");
        check(Path.of("rel", "r.sock").toString().equals(b.getPath().toString()), "relative text");
        check(UnixDomainSocketAddress.of(Path.of("")).getPath().toString().isEmpty(),
                "empty Path accepted (probed)");
        check(UnixDomainSocketAddress.of(Path.of("a.sock")).equals(UnixDomainSocketAddress.of("a.sock")),
                "single-name relative path equals across factories");
        // of(String) creates a fresh address every time (probed identity).
        UnixDomainSocketAddress x1 = UnixDomainSocketAddress.of("/tmp/a.sock");
        UnixDomainSocketAddress x2 = UnixDomainSocketAddress.of("/tmp/a.sock");
        check(x1 != x2, "of(String) returns a new instance per call (no interning, probed)");
        check(x1.equals(x2) && x1.hashCode() == x2.hashCode(), "fresh instances still equal");
        check(x1.getPath() != x2.getPath(), "inner Path instances also distinct");
    }

    // ---- Part 4: equals/hashCode matrix ----
    static void testEqualsHashCode() {
        UnixDomainSocketAddress a = UnixDomainSocketAddress.of("/tmp/e1.sock");
        UnixDomainSocketAddress b = UnixDomainSocketAddress.of("/tmp/e1.sock");
        UnixDomainSocketAddress d = UnixDomainSocketAddress.of("/tmp/e2.sock");
        check(a.equals(b) && b.equals(a), "equals symmetric for same path");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(d) && !d.equals(a), "equals symmetric for different paths");
        check(!a.equals(null), "equals(null) false");
        check(!a.equals("/tmp/e1.sock"), "equals(String) false");
        check(!a.equals(new InetSocketAddress(1)), "equals(InetSocketAddress) false");
        check(!a.equals(InetSocketAddress.createUnresolved("h", 1)), "equals(unresolved InetSocketAddress) false");
        check(a.hashCode() == b.hashCode(), "equal objects equal hashCodes");
        check(a.hashCode() == Path.of("/tmp/e1.sock").hashCode(),
                "hashCode delegates to the path (probed)");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
        // Path semantics: dot components are not normalized away (probed).
        check(!UnixDomainSocketAddress.of("/tmp/./e1.sock").equals(a), "dot segment not normalized in equals");
        // Unicode paths compare by exact text.
        check(UnixDomainSocketAddress.of("/tmp/日.sock").equals(UnixDomainSocketAddress.of("/tmp/日.sock")),
                "unicode path equals");
        check(!UnixDomainSocketAddress.of("/tmp/日.sock").equals(UnixDomainSocketAddress.of("/tmp/本.sock")),
                "different unicode paths unequal");
        // Empty-path addresses all equal each other.
        check(UnixDomainSocketAddress.of("").equals(UnixDomainSocketAddress.of(Path.of(""))),
                "empty-path addresses equal");
        check(!UnixDomainSocketAddress.of("").equals(a), "empty path differs from absolute");
        // hashCode spread sanity on a deterministic sample (no collision assumption, only consistency).
        for (String s : new String[]{"/tmp/1.sock", "/tmp/2.sock", "/tmp/3.sock"}) {
            check(UnixDomainSocketAddress.of(s).hashCode() == UnixDomainSocketAddress.of(s).hashCode(),
                    "repeatable hashCode for " + s);
        }
    }

    // ---- Part 5: toString ----
    static void testToString() {
        check(Path.of("/tmp/t.sock").toString()
                        .equals(UnixDomainSocketAddress.of("/tmp/t.sock").toString()), "absolute text");
        check("t.sock".equals(UnixDomainSocketAddress.of("t.sock").toString()), "relative text");
        check("".equals(UnixDomainSocketAddress.of("").toString()), "empty text");
        check(Path.of("/tmp/a b.sock").toString()
                        .equals(UnixDomainSocketAddress.of("/tmp/a b.sock").toString()),
                "spaces preserved verbatim");
        UnixDomainSocketAddress u = UnixDomainSocketAddress.of("/tmp/rep.sock");
        check(u.toString().equals(u.toString()), "toString stable");
        check(u.toString().equals(u.getPath().toString()), "toString delegates to path text");
    }

    // ---- Part 6: negative construction (exact exceptions, probed) ----
    static void testNegatives() {
        fails(NullPointerException.class, "of null string", () -> UnixDomainSocketAddress.of((String) null));
        fails(NullPointerException.class, "of null path", () -> UnixDomainSocketAddress.of((Path) null));
        fails(InvalidPathException.class, "embedded null char", () -> UnixDomainSocketAddress.of("\u0000bad"));
        fails(InvalidPathException.class, "null char mid-string", () -> UnixDomainSocketAddress.of("a\u0000b"));
        fails(InvalidPathException.class, "null char at end", () -> UnixDomainSocketAddress.of("/tmp/x\u0000"));
        // Exact IPE payload (probed: reason text, input echoed, index -1).
        try {
            UnixDomainSocketAddress.of("a\u0000b");
            check(false, "IPE expected");
        } catch (InvalidPathException e) {
            check("a\u0000b".equals(e.getInput()), "IPE echoes the offending input");
            if (File.separatorChar == '/') {
                check("Nul character not allowed".equals(e.getReason()), "IPE reason exact (probed)");
                check(e.getIndex() == -1, "IPE index -1 for nul-character rejection (probed)");
            } else {
                // The Windows path parser reports the offending character and
                // its position rather than rejecting the whole input.
                check(e.getReason() != null && !e.getReason().isEmpty(),
                        "IPE reason reported: " + e.getReason());
                check(e.getIndex() == 1, "IPE index points at the nul char, got " + e.getIndex());
            }
        }
    }

    // ---- Part 7: serialization round-trip (custom writeReplace form, probed) ----
    static void testSerialization() throws Exception {
        UnixDomainSocketAddress orig = UnixDomainSocketAddress.of("/tmp/ser/a.sock");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(orig);
        }
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof UnixDomainSocketAddress, "deserialized type preserved");
        UnixDomainSocketAddress res = (UnixDomainSocketAddress) back;
        check(orig.equals(res), "round-trip equals");
        check(res.equals(orig), "round-trip equals symmetric");
        check(Path.of("/tmp/ser/a.sock").toString().equals(res.getPath().toString()),
                "round-trip path text");
        check(res.toString().equals(orig.toString()), "round-trip toString");
        check(res.hashCode() == orig.hashCode(), "round-trip hashCode");
        // Relative and empty paths also survive.
        UnixDomainSocketAddress rel = UnixDomainSocketAddress.of("rel/x.sock");
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos2)) {
            oos.writeObject(rel);
        }
        Object back2 = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray())).readObject();
        check(rel.equals(back2), "relative path round-trip");
        UnixDomainSocketAddress empty = UnixDomainSocketAddress.of("");
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos3)) {
            oos.writeObject(empty);
        }
        Object back3 = new ObjectInputStream(new ByteArrayInputStream(bos3.toByteArray())).readObject();
        check(empty.equals(back3), "empty path round-trip");
    }

    // ---- Part 8: live AF_UNIX integration on a throwaway socket path ----

    /**
     * Opens a UNIX-family server channel, or reports the test as skipped when
     * the platform has no AF_UNIX provider (pre-1803 Windows, or a default
     * file system that cannot carry socket paths).
     */
    static ServerSocketChannel openUnixServer() throws java.io.IOException {
        try {
            return ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        } catch (UnsupportedOperationException | java.net.SocketException e) {
            throw new jtreg.SkippedException(
                    "AF_UNIX not supported on this platform: " + e);
        }
    }

    static void testLiveUnixDomainSocket() throws Exception {
        Path dir = Files.createTempDirectory("net-J-uds");
        Path sockPath = dir.resolve("test.sock");
        UnixDomainSocketAddress addr = UnixDomainSocketAddress.of(sockPath);

        try (ServerSocketChannel ssc = openUnixServer()) {
            ssc.bind(addr);
            if (File.separatorChar == '/') {
                // A POSIX AF_UNIX name is a special file in the file system;
                // the Windows implementation stores it differently.
                check(Files.exists(sockPath), "socket special file created in the filesystem");
            }
            SocketAddress local = ssc.getLocalAddress();
            check(local instanceof UnixDomainSocketAddress, "local address type");
            check(local.equals(addr), "bound local address equals the constructed one");
            check(((UnixDomainSocketAddress) local).getPath().equals(sockPath),
                    "local address path equals the socket path");

            // Double-bind of the same filesystem name is refused (probed message).
            try (ServerSocketChannel second = openUnixServer()) {
                second.bind(UnixDomainSocketAddress.of(sockPath));
                check(false, "second bind must fail");
            } catch (BindException e) {
                check(e.getMessage() != null && e.getMessage().contains("Address already in use"),
                        "double bind -> BindException 'Address already in use' (probed)");
            }

            // A client connects through the same UnixDomainSocketAddress value.
            final ServerSocketChannel server = ssc;
            final SocketChannel[] accepted = new SocketChannel[1];
            Thread acceptor = new Thread(() -> {
                try {
                    accepted[0] = server.accept();
                    // Echo one byte back so both directions are exercised.
                    java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(1);
                    accepted[0].read(buf);
                    buf.flip();
                    accepted[0].write(buf);
                } catch (Exception ignored) {
                }
            });
            acceptor.setDaemon(true);
            acceptor.start();

            try (SocketChannel client = SocketChannel.open(StandardProtocolFamily.UNIX)) {
                client.connect(addr);
                check(client.isConnected(), "client connected via UnixDomainSocketAddress");
                SocketAddress remote = client.getRemoteAddress();
                check(remote instanceof UnixDomainSocketAddress, "remote address type");
                check(remote.equals(addr), "remote address equals server path");
                client.write(java.nio.ByteBuffer.wrap(new byte[]{42}));
                java.nio.ByteBuffer in = java.nio.ByteBuffer.allocate(1);
                int n = client.read(in);
                check(n == 1 && in.get(0) == 42, "byte echoed back over the AF_UNIX pair");
            }
            acceptor.join(3000);
            check(!acceptor.isAlive(), "acceptor thread finished within bound");
            check(accepted[0] != null, "server side accepted a channel");
            check(accepted[0].getRemoteAddress() instanceof UnixDomainSocketAddress,
                    "accepted-side peer address type");
            accepted[0].close();

            ssc.close();
            // After close, querying the local address fails deterministically (probed).
            try {
                ssc.getLocalAddress();
                check(false, "local address after close must fail");
            } catch (java.nio.channels.ClosedChannelException e) {
                check(true, "local address after close -> ClosedChannelException (probed)");
            }
        }

        // Binding to a path under a nonexistent directory fails (probed: plain
        // SocketException, not NoSuchFileException). The message is the bare
        // strerror text, localized by OS locale, and carries no path; only the
        // type and a non-empty message are portable.
        try (ServerSocketChannel bad = openUnixServer()) {
            bad.bind(UnixDomainSocketAddress.of(dir.resolve("no-such-subdir").resolve("x.sock")));
            check(false, "bind into missing directory must fail");
        } catch (SocketException e) {
            check(e.getMessage() != null,
                    "missing directory -> SocketException: " + e.getMessage());
        }

        // Cleanup (bounded, best effort).
        Files.deleteIfExists(sockPath);
        Files.delete(dir);
        check(!Files.exists(dir), "temp directory removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxShape();
        testOfStringMatrix();
        testOfPath();
        testEqualsHashCode();
        testToString();
        testNegatives();
        testSerialization();
        testLiveUnixDomainSocket();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
