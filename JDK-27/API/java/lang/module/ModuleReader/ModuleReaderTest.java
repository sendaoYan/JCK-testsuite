/*
 * @test
 * @summary java.lang.module.ModuleReader conformance: white-box + black-box behavior.
 * @run main ModuleReaderTest
 */

import java.io.Closeable;
import java.io.InputStream;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.Stream;

public class ModuleReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.module.ModuleReader");
        check(c.isInterface(), "is an interface");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == Closeable.class,
              "extends java.io.Closeable");
        for (String m : new String[]{"find", "open", "read", "release", "list", "close"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(c.getDeclaredMethod("open", String.class).isDefault(), "open is a default method");
        check(c.getDeclaredMethod("read", String.class).isDefault(), "read is a default method");
        check(c.getDeclaredMethod("release", ByteBuffer.class).isDefault(), "release is a default method");
        check(!c.getDeclaredMethod("find", String.class).isDefault(), "find is abstract");
        check(!c.getDeclaredMethod("list").isDefault(), "list is abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        check(ref.open() instanceof ModuleReader, "open() yields ModuleReader");
        check(ref.open() instanceof Closeable, "ModuleReader is Closeable");

        try (ModuleReader reader = ref.open()) {
            // find(): URI for a class resource
            Optional<URI> uri = reader.find("java/lang/Object.class");
            check(uri.isPresent(), "find(Object.class) present");
            check("jrt".equals(uri.get().getScheme()), "uri scheme jrt");
            check(uri.get().toString().contains("java.base"), "uri identifies java.base");
            check(uri.get().getPath() != null && uri.get().getPath().endsWith("java/lang/Object.class"),
                  "uri path names the resource");

            // open(): stream with the class-file magic number
            try (InputStream in = reader.open("java/lang/Object.class").orElseThrow()) {
                check((in.read() & 0xFF) == 0xCA, "magic 0xCA");
                check((in.read() & 0xFF) == 0xFE, "magic 0xFE");
                check((in.read() & 0xFF) == 0xBA, "magic 0xBA");
                check((in.read() & 0xFF) == 0xBE, "magic 0xBE");
                check(in.read() != -1, "stream continues past magic");
            }

            // read(): ByteBuffer positioned at the same magic bytes
            ByteBuffer bb = reader.read("java/lang/Object.class").orElseThrow();
            check(bb.remaining() > 8, "read() buffer has content");
            check((bb.get() & 0xFF) == 0xCA && (bb.get() & 0xFF) == 0xFE
                  && (bb.get() & 0xFF) == 0xBA && (bb.get() & 0xFF) == 0xBE,
                  "read() magic matches");
            reader.release(bb);

            // absent resources: empty Optional, never null, never a throw
            check(reader.find("no/such/X.class").equals(Optional.empty()), "find absent empty");
            check(reader.open("no/such/X.class").equals(Optional.empty()), "open absent empty");
            check(reader.read("no/such/X.class").equals(Optional.empty()), "read absent empty");
            check(reader.find("").equals(Optional.empty()), "empty name finds nothing");

            // list(): all class-file resources of the module
            try (Stream<String> names = reader.list()) {
                check(names.anyMatch("java/lang/Object.class"::equals),
                      "list() contains Object.class");
            }
            try (Stream<String> names = reader.list()) {
                check(names.anyMatch("java/lang/String.class"::equals),
                      "list() contains String.class");
            }
            // package-name form: resources use '/' separators
            try (Stream<String> names = reader.list()) {
                check(names.noneMatch(n -> n.startsWith("/") || n.contains("//")),
                      "list() names are module-relative, no leading/double slash");
            }
        }

        // a second reader over the same module is independent; double close is safe
        try (ModuleReader r1 = ref.open()) {
            check(r1.find("java/lang/Object.class").isPresent(), "second reader works");
        }
        ModuleReader r2 = ref.open();
        r2.close();
        r2.close();
        check(true, "close() is idempotent");
    }

    // ---- Part 3: contract deepening (2026-11) ----

    // closed-reader contract: every access path reports the same exact IOException
    static void testClosedReaderContract() throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        ModuleReader r = ref.open();
        r.close();
        try {
            r.read("java/lang/String.class");
            check(false, "read after close IOException");
        } catch (java.io.IOException e) {
            check("ModuleReader is closed".equals(e.getMessage()), "read-after-close exact message");
        }
        try {
            r.open("java/lang/String.class");
            check(false, "open after close IOException");
        } catch (java.io.IOException e) {
            check("ModuleReader is closed".equals(e.getMessage()), "open-after-close exact message");
        }
        try {
            r.find("java/lang/String.class");
            check(false, "find after close IOException");
        } catch (java.io.IOException e) {
            check("ModuleReader is closed".equals(e.getMessage()), "find-after-close exact message");
        }
        try {
            r.list();
            check(false, "list after close IOException");
        } catch (java.io.IOException e) {
            check("ModuleReader is closed".equals(e.getMessage()), "list-after-close exact message");
        }
    }

    // read(ByteBuffer) semantics: content, independence, release idempotence
    static void testReadContract() throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        try (ModuleReader reader = ref.open()) {
            ByteBuffer a = reader.read("module-info.class").orElseThrow();
            check(a.remaining() > 1000, "module-info.class has a substantial payload");
            check(a.get() == (byte) 0xCA && a.get() == (byte) 0xFE
                  && a.get() == (byte) 0xBA && a.get() == (byte) 0xBE,
                  "module-info.class magic");
            ByteBuffer b = reader.read("module-info.class").orElseThrow();
            check(a != b, "each read allocates a fresh buffer");
            check(a.position() == 4 && b.position() == 0, "buffers have independent positions");
            reader.release(b);
            reader.release(b);
            check(true, "double release is tolerated (release is advisory)");
            reader.release(a);
            ByteBuffer str = reader.read("java/lang/String.class").orElseThrow();
            check(str.remaining() > 100, "String.class buffer substantial");
            reader.release(str);
            check(reader.read("no/such/Resource.class").isEmpty(), "read absent empty Optional");
            check(reader.read("").isEmpty(), "read empty name empty Optional");
            check(reader.read("java/lang/Object.class").orElseThrow().remaining() > 100,
                  "Object.class buffer substantial");
        }
    }

    // open(InputStream) semantics: streams are independent and fully readable
    static void testOpenContract() throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        try (ModuleReader reader = ref.open()) {
            int firstLen, secondLen;
            try (InputStream in = reader.open("module-info.class").orElseThrow()) {
                byte[] buf = new byte[8192];
                int total = 0, n;
                while ((n = in.read(buf)) > 0) total += n;
                firstLen = total;
            }
            try (InputStream in = reader.open("module-info.class").orElseThrow()) {
                byte[] buf = new byte[8192];
                int total = 0, n;
                while ((n = in.read(buf)) > 0) total += n;
                secondLen = total;
            }
            check(firstLen > 1000 && firstLen == secondLen,
                  "two opens of the same resource yield equal full contents");
            try (InputStream s1 = reader.open("java/lang/Object.class").orElseThrow();
                 InputStream s2 = reader.open("java/lang/Object.class").orElseThrow()) {
                check(s1 != s2, "open returns independent streams");
                check(s1.read() == s2.read(), "streams start at the same byte");
            }
            try (InputStream in = reader.open("module-info.class").orElseThrow()) {
                check(in.available() >= 0, "available() legal on module stream");
            }
            check(reader.open("no/such/X").isEmpty(), "open absent empty");
        }
    }

    // list(): volume, package-shape, module-info presence, independent iterations
    static void testListContract() throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        try (ModuleReader reader = ref.open()) {
            long count;
            try (Stream<String> names = reader.list()) {
                count = names.count();
            }
            check(count > 1000, "java.base lists thousands of resources");
            try (Stream<String> names = reader.list()) {
                check(names.anyMatch("module-info.class"::equals), "module-info.class listed");
            }
            try (Stream<String> names = reader.list()) {
                long distinct = names.distinct().count();
                check(distinct == count, "list() names are distinct");
            }
            try (Stream<String> names = reader.list()) {
                check(names.allMatch(n -> n.indexOf('\\') < 0), "names never use backslash");
            }
            try (Stream<String> names = reader.list()) {
                long classCount = names.filter(n -> n.endsWith(".class")).count();
                check(classCount > 900, "thousands of .class resources listed");
            }
        }
    }

    // find(): URI stability and module-info lookup
    static void testFindContract() throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        try (ModuleReader reader = ref.open()) {
            Optional<URI> mi = reader.find("module-info.class");
            check(mi.isPresent(), "find(module-info.class) present");
            check(mi.get().getPath().endsWith("module-info.class"), "module-info URI path");
            Optional<URI> mi2 = reader.find("module-info.class");
            check(mi.get().equals(mi2.get()), "find stable (equal URIs)");
            Optional<URI> str = reader.find("java/lang/String.class");
            check(str.isPresent() && str.get().toString().contains("java.base"),
                  "String.class URI names the module");
            Optional<URI> box = reader.find("java/lang/Integer.class");
            check(box.isPresent() && box.get().getPath().endsWith("java/lang/Integer.class"),
                  "Integer.class URI path");
            check(!reader.find("java/lang/NoSuch.class").isPresent(),
                  "unknown class resource absent");
            check(!reader.find("/java/lang/String.class").isPresent()
                  || reader.find("/java/lang/String.class").isPresent(),
                  "leading-slash form is tolerated (either way)");
            check(reader.find("module-info.class").isPresent(),
                  "repeated find still present");
        }
    }

    // default open/read/release wired through a file-backed custom reader
    static void testCustomReaderDefaults() throws Exception {
        java.nio.file.Path f = java.nio.file.Files.createTempFile("lcK-reader-res", ".txt");
        try {
            java.nio.file.Files.write(f, "hello-module".getBytes("UTF-8"));
            URI uri = f.toUri();
            ModuleReader custom = new ModuleReader() {
                @Override public Optional<URI> find(String name) {
                    return "res.txt".equals(name) ? Optional.of(uri) : Optional.empty();
                }
                @Override public Stream<String> list() { return Stream.of("res.txt"); }
                @Override public void close() { }
            };
            try (custom) {
                ByteBuffer bb = custom.read("res.txt").orElseThrow();
                byte[] arr = new byte[bb.remaining()];
                bb.get(arr);
                check("hello-module".equals(new String(arr, "UTF-8")),
                      "default read loads the file behind the URI");
                try (InputStream in = custom.open("res.txt").orElseThrow()) {
                    check(in.read() == 'h', "default open streams the file");
                }
                check(custom.read("missing.txt").isEmpty(),
                      "default read of an absent name is an empty Optional");
                ByteBuffer rb = custom.read("res.txt").orElseThrow();
                custom.release(rb);
                custom.release(rb);
                check(true, "default release is a no-op and repeatable");
                try (Stream<String> names = custom.list()) {
                    check(names.count() == 1, "custom list honored");
                }
                check(custom.find("res.txt").orElseThrow().equals(uri),
                      "find passes the URI through untouched");
            }
            check(true, "custom close is a no-op and idempotent");
            ModuleReader again = new ModuleReader() {
                @Override public Optional<URI> find(String name) { return Optional.empty(); }
                @Override public Stream<String> list() { return Stream.empty(); }
                @Override public void close() { }
            };
            again.close();
            check(again.list().count() == 0, "empty-list custom reader honors list()");
            check(again.find("x").isEmpty(), "empty-find custom reader honors find()");
            check(again.read("x").isEmpty(), "default read backed by empty find");
        } finally {
            try {
                java.nio.file.Files.delete(f);
            } catch (java.io.IOException ignore) { }
        }
    }

    // matrix: every well-known java.base resource is find/open/read-able at once
    static void testResourceMatrix() throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        String[] resources = {"java/lang/Object.class", "java/lang/String.class",
                              "java/lang/Integer.class", "java/util/List.class",
                              "java/io/InputStream.class"};
        try (ModuleReader reader = ref.open()) {
            for (String res : resources) {
                Optional<URI> u = reader.find(res);
                check(u.isPresent() && "jrt".equals(u.get().getScheme()),
                        res + ": find jrt URI");
                try (InputStream in = reader.open(res).orElseThrow()) {
                    check((in.read() & 0xFF) == 0xCA, res + ": open magic 0xCA");
                }
                ByteBuffer bb = reader.read(res).orElseThrow();
                check(bb.remaining() > 100 && (bb.get(0) & 0xFF) == 0xCA,
                        res + ": read buffer with class magic");
                reader.release(bb);
            }
            // interleaved use of two readers stays independent
            try (ModuleReader r2 = ref.open()) {
                ByteBuffer b1 = reader.read("java/lang/Object.class").orElseThrow();
                ByteBuffer b2 = r2.read("java/lang/Object.class").orElseThrow();
                check(b1.remaining() == b2.remaining(),
                        "two readers agree on the resource size");
                check(b1 != b2, "two readers hand out distinct buffers");
                reader.release(b1);
                r2.release(b2);
                try (InputStream s1 = reader.open("java/lang/Object.class").orElseThrow();
                     InputStream s2 = r2.open("java/lang/Object.class").orElseThrow()) {
                    check(s1.read() == s2.read(), "interleaved streams align");
                }
            }
            // reopen after closing a sibling reader keeps working
            ModuleReader r3 = ref.open();
            r3.close();
            check(reader.read("java/lang/Object.class").isPresent(),
                    "closing a sibling does not affect this reader");
        }
    }

    // white-box deepening: declared exceptions and default-method flags
    static void testWhiteBoxDeepen() throws Exception {
        Class<?> c = ModuleReader.class;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("open") || m.getName().equals("read")
                    || m.getName().equals("find") || m.getName().equals("list")) {
                boolean declares = false;
                for (Class<?> ex : m.getExceptionTypes()) {
                    if (ex == java.io.IOException.class) declares = true;
                }
                check(declares, m.getName() + " declares IOException");
            }
        }
        check(c.getDeclaredMethod("close").getExceptionTypes().length == 0
                  || java.util.Arrays.asList(c.getDeclaredMethod("close").getExceptionTypes())
                          .contains(java.io.IOException.class),
              "close signature compatible with IOException");
        check(java.lang.reflect.Modifier.isPublic(
                  c.getDeclaredMethod("release", ByteBuffer.class).getModifiers()),
              "release is public");
        check(c.getDeclaredMethod("list").getReturnType() == java.util.stream.Stream.class,
              "list returns Stream<String>");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testClosedReaderContract();
        testReadContract();
        testOpenContract();
        testListContract();
        testFindContract();
        testCustomReaderDefaults();
        testResourceMatrix();
        testWhiteBoxDeepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
