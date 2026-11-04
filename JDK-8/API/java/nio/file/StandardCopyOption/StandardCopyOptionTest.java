/*
 * @test
 * @summary java.nio.file.StandardCopyOption conformance: white-box structure + black-box behavior (JDK-8).
 * @run main StandardCopyOptionTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;

public class StandardCopyOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.StandardCopyOption");
        check(c.isEnum(), "StandardCopyOption is an enum");
        check(c.getEnumConstants().length == 3, "exactly 3 constants");
        for (String n : new String[] {"REPLACE_EXISTING", "COPY_ATTRIBUTES", "ATOMIC_MOVE"})
            check(java.util.Arrays.stream(c.getEnumConstants())
                  .anyMatch(k -> ((Enum<?>) k).name().equals(n)), "constant " + n);
        check(java.nio.file.CopyOption.class.isAssignableFrom(c), "implements CopyOption");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        check(StandardCopyOption.values().length == 3, "values length");
        check(StandardCopyOption.values()[0] == StandardCopyOption.REPLACE_EXISTING,
              "REPLACE_EXISTING first");
        check(StandardCopyOption.valueOf("ATOMIC_MOVE") == StandardCopyOption.ATOMIC_MOVE,
              "valueOf");

        Path dir = Files.createTempDirectory("jck");
        try {
            Path src = dir.resolve("src.txt");
            Files.write(src, "copy-me".getBytes(StandardCharsets.UTF_8));

            Path dst = dir.resolve("dst.txt");
            Files.copy(src, dst);
            check(new String(Files.readAllBytes(dst), StandardCharsets.UTF_8).equals("copy-me"),
                  "content copied");
            boolean refused = false;
            try { Files.copy(src, dst); }
            catch (java.nio.file.FileAlreadyExistsException e) { refused = true; }
            check(refused, "second copy without option -> FileAlreadyExistsException");

            Files.write(src, "updated".getBytes(StandardCharsets.UTF_8));
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            check(new String(Files.readAllBytes(dst), StandardCharsets.UTF_8).equals("updated"),
                  "REPLACE_EXISTING overwrites");

            FileTime mtime = FileTime.from(java.time.Instant.parse("2020-01-02T03:04:05Z"));
            Files.setLastModifiedTime(src, mtime);
            Path attrDst = dir.resolve("attr-dst.txt");
            Files.copy(src, attrDst, StandardCopyOption.COPY_ATTRIBUTES);
            check(Files.getLastModifiedTime(attrDst).toMillis() == mtime.toMillis(),
                  "COPY_ATTRIBUTES copies mtime");

            Path moved = dir.resolve("moved.txt");
            Files.move(src, moved, StandardCopyOption.ATOMIC_MOVE);
            check(new String(Files.readAllBytes(moved), StandardCharsets.UTF_8).equals("updated"),
                  "ATOMIC_MOVE within a store");
            check(!Files.exists(src), "source gone after move");
        } finally {
            for (String n : new String[] {"src.txt", "dst.txt", "attr-dst.txt", "moved.txt"})
                Files.deleteIfExists(dir.resolve(n));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
