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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
