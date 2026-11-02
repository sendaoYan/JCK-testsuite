/*
 * @test
 * @summary java.lang.module.ModuleReader conformance: find(), open(), read(), release(), list() and Closeable.
 * @run main ModuleReaderTest
 */

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

    public static void main(String[] args) throws Exception {
        ModuleReference ref = ModuleFinder.ofSystem().find("java.base").orElseThrow();
        check(ref.open() instanceof java.io.Closeable, "ModuleReader is Closeable");

        try (ModuleReader reader = ref.open()) {
            Optional<URI> uri = reader.find("java/lang/Object.class");
            check(uri.isPresent(), "find(Object.class) present");
            check(uri.get().toString().contains("java.base"), "uri identifies java.base");

            try (InputStream in = reader.open("java/lang/Object.class").orElseThrow()) {
                check((in.read() & 0xFF) == 0xCA, "class magic 0xCA");
                check((in.read() & 0xFF) == 0xFE, "class magic 0xFE");
                check((in.read() & 0xFF) == 0xBA, "class magic 0xBA");
                check((in.read() & 0xFF) == 0xBE, "class magic 0xBE");
            }

            ByteBuffer bb = reader.read("java/lang/Object.class").orElseThrow();
            check((bb.get() & 0xFF) == 0xCA, "read magic 0xCA");
            check((bb.get() & 0xFF) == 0xFE, "read magic 0xFE");
            check((bb.get() & 0xFF) == 0xBA, "read magic 0xBA");
            check((bb.get() & 0xFF) == 0xBE, "read magic 0xBE");
            reader.release(bb);

            try (Stream<String> names = reader.list()) {
                check(names.anyMatch("java/lang/Object.class"::equals), "list() contains Object.class");
            }
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
