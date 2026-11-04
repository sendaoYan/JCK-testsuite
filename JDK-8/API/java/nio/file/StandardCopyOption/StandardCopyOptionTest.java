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

    // ---- appended: exact enum matrix ----
    static void testEnumMatrix() {
        StandardCopyOption[] vals = StandardCopyOption.values();
        check(vals.length == 3, "exactly 3 constants");
        check(vals[0] == StandardCopyOption.REPLACE_EXISTING, "values[0] REPLACE_EXISTING");
        check(vals[1] == StandardCopyOption.COPY_ATTRIBUTES, "values[1] COPY_ATTRIBUTES");
        check(vals[2] == StandardCopyOption.ATOMIC_MOVE, "values[2] ATOMIC_MOVE");
        check("REPLACE_EXISTING".equals(StandardCopyOption.REPLACE_EXISTING.name()),
              "REPLACE_EXISTING name");
        check("COPY_ATTRIBUTES".equals(StandardCopyOption.COPY_ATTRIBUTES.name()),
              "COPY_ATTRIBUTES name");
        check("ATOMIC_MOVE".equals(StandardCopyOption.ATOMIC_MOVE.name()),
              "ATOMIC_MOVE name");
        check(StandardCopyOption.REPLACE_EXISTING.ordinal() == 0, "REPLACE_EXISTING ordinal 0");
        check(StandardCopyOption.COPY_ATTRIBUTES.ordinal() == 1, "COPY_ATTRIBUTES ordinal 1");
        check(StandardCopyOption.ATOMIC_MOVE.ordinal() == 2, "ATOMIC_MOVE ordinal 2");
        check(StandardCopyOption.REPLACE_EXISTING.compareTo(StandardCopyOption.COPY_ATTRIBUTES) < 0,
              "REPLACE_EXISTING < COPY_ATTRIBUTES");
        check(StandardCopyOption.COPY_ATTRIBUTES.compareTo(StandardCopyOption.ATOMIC_MOVE) < 0,
              "COPY_ATTRIBUTES < ATOMIC_MOVE");
        for (StandardCopyOption o : StandardCopyOption.values())
            check(o.toString().equals(o.name()), "toString == name for " + o);
        for (StandardCopyOption o : StandardCopyOption.values())
            check(StandardCopyOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(java.util.EnumSet.allOf(StandardCopyOption.class).size() == 3,
              "EnumSet allOf size 3");
        check(java.util.EnumSet.of(StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE).size() == 2, "EnumSet of two");
        boolean iae = false;
        try { StandardCopyOption.valueOf("replace_existing"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "valueOf wrong case -> IllegalArgumentException");
    }

    // ---- appended: option-scoped semantics (measured on ext4) ----
    static void testOptionScopes() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            // REPLACE_EXISTING also applies to move
            Path src = dir.resolve("mv-src.txt");
            Files.write(src, "m1".getBytes(StandardCharsets.UTF_8));
            Path dst = dir.resolve("mv-dst.txt");
            Files.write(dst, "old".getBytes(StandardCharsets.UTF_8));
            Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
            check(new String(Files.readAllBytes(dst), StandardCharsets.UTF_8).equals("m1"),
                  "move REPLACE_EXISTING replaces content");
            check(!Files.exists(src), "move REPLACE_EXISTING removes the source");

            // ATOMIC_MOVE is not a copy option; COPY_ATTRIBUTES is not a move option
            boolean uoe = false;
            try { Files.copy(dst, dir.resolve("no.txt"), StandardCopyOption.ATOMIC_MOVE); }
            catch (UnsupportedOperationException e) { uoe = true; }
            check(uoe, "copy with ATOMIC_MOVE -> UnsupportedOperationException");
            boolean uoe2 = false;
            try { Files.move(dst, dir.resolve("no2.txt"), StandardCopyOption.COPY_ATTRIBUTES); }
            catch (UnsupportedOperationException e) { uoe2 = true; }
            check(uoe2, "move with COPY_ATTRIBUTES -> UnsupportedOperationException");

            // directories: plain copy creates the target dir; REPLACE allows overwrite
            Path sub1 = dir.resolve("sub1");
            Files.createDirectory(sub1);
            Path sub2 = dir.resolve("sub2");
            Files.createDirectory(sub2);
            Files.copy(sub1, sub2, StandardCopyOption.REPLACE_EXISTING);
            check(Files.isDirectory(sub2), "empty dir copy onto empty dir with REPLACE ok");
            Path sub3 = dir.resolve("sub3");
            Files.copy(sub1, sub3);
            check(Files.isDirectory(sub3), "plain empty dir copy creates the target");
            Path sub4 = dir.resolve("sub4");
            Files.move(sub3, sub4, StandardCopyOption.ATOMIC_MOVE);
            check(Files.isDirectory(sub4) && !Files.exists(sub3),
                  "directory ATOMIC_MOVE within one store (ext4 measured)");

            // COPY_ATTRIBUTES keeps the mtime on a fresh target to the millisecond
            FileTime mtime = FileTime.fromMillis(1_500_000_000_000L);
            Path attrSrc = dir.resolve("attr-src.txt");
            Files.write(attrSrc, "a".getBytes(StandardCharsets.UTF_8));
            Files.setLastModifiedTime(attrSrc, mtime);
            Path attrDst = dir.resolve("attr-dst.txt");
            Files.copy(attrSrc, attrDst, StandardCopyOption.COPY_ATTRIBUTES);
            check(Files.getLastModifiedTime(attrDst).equals(mtime),
                  "COPY_ATTRIBUTES mtime preserved exactly");
        } finally {
            try (java.util.stream.Stream<Path> st = Files.walk(dir)) {
                java.util.List<Path> l = new java.util.ArrayList<Path>();
                st.forEach(l::add);
                java.util.Collections.reverse(l);
                for (Path p : l) { try { Files.deleteIfExists(p); } catch (Exception ignore) { } }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEnumMatrix();
        testOptionScopes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
