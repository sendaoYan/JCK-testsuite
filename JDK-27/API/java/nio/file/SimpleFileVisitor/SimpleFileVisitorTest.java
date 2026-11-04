/*
 * @test
 * @summary java.nio.file.SimpleFileVisitor conformance: white-box structure + black-box behavior.
 * @run main SimpleFileVisitorTest
 */

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class SimpleFileVisitorTest {
    // the default constructor is protected: subclass to reach it
    static final class MyVisitor extends SimpleFileVisitor<Path> { }
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Op { void run() throws Exception; }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.SimpleFileVisitor");
        check(!java.lang.reflect.Modifier.isInterface(c.getModifiers())
              && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "concrete class");
        check(java.nio.file.FileVisitor.class.isAssignableFrom(c), "implements FileVisitor");
        check(c.getTypeParameters().length == 1, "generic in path type");
        check(c.getDeclaredConstructors().length == 1
              && !java.lang.reflect.Modifier.isPublic(
                     c.getDeclaredConstructors()[0].getModifiers()),
              "protected default constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SimpleFileVisitor<Path> v = new MyVisitor();

        // defaults continue everywhere and pass attributes through
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("d.txt");
            Files.writeString(f, "d");
            BasicFileAttributes attrs = Files.readAttributes(f,
                    java.nio.file.attribute.BasicFileAttributes.class);
            check(v.preVisitDirectory(dir, attrs) == FileVisitResult.CONTINUE,
                  "preVisitDirectory -> CONTINUE");
            check(v.visitFile(f, attrs) == FileVisitResult.CONTINUE,
                  "visitFile -> CONTINUE");
            check(v.postVisitDirectory(dir, null) == FileVisitResult.CONTINUE,
                  "postVisitDirectory(null error) -> CONTINUE");

            // visitFileFailed rethrows the failure unchanged
            IOException ioe = new IOException("boom");
            boolean rethrown = false;
            try { v.visitFileFailed(f, ioe); }
            catch (IOException e) { rethrown = e == ioe; }
            check(rethrown, "visitFileFailed rethrows the same IOException");

            // usable directly in walkFileTree to traverse everything
            java.util.concurrent.atomic.AtomicInteger files =
                    new java.util.concurrent.atomic.AtomicInteger();
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path p, BasicFileAttributes a)
                        throws IOException {
                    files.incrementAndGet();
                    return super.visitFile(p, a);
                }
            });
            check(files.get() == 1, "walk covers the single file");

            // a failing attribute read surfaces through the default failure hook
            Path lockedDir = dir.resolve("locked");
            Files.createDirectory(lockedDir);
            Files.writeString(lockedDir.resolve("s.txt"), "s");
            if (!System.getProperty("user.name").equals("root")) {
                Files.setPosixFilePermissions(lockedDir,
                        java.nio.file.attribute.PosixFilePermissions.fromString("--x------"));
                try {
                    boolean failed = false;
                    try {
                        Files.walkFileTree(dir, new MyVisitor());
                    } catch (IOException e) { failed = true; }
                    check(failed, "unreadable entry -> IOException through default hook");
                } finally {
                    Files.setPosixFilePermissions(lockedDir,
                            java.nio.file.attribute.PosixFilePermissions.fromString("rwx------"));
                }
            }
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) { } });
            }
        }
    }

    // ===== deepened contract coverage (batch A9) =====
    static void testWhiteBoxDeep() throws Exception {
        Class<?> c = Class.forName("java.nio.file.SimpleFileVisitor");
        java.lang.reflect.Method[] dm = c.getDeclaredMethods();
        check(dm.length == 4, "exactly four declared methods, got " + dm.length);
        boolean allPublic = true, allConcrete = true;
        for (java.lang.reflect.Method m : dm) {
            if (!java.lang.reflect.Modifier.isPublic(m.getModifiers())) allPublic = false;
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) allConcrete = false;
        }
        check(allPublic, "all visit methods are public");
        check(allConcrete, "no abstract methods (concrete defaults)");
        check(c.getDeclaredFields().length == 0, "stateless: no declared fields");
        check(c.getDeclaredConstructors()[0].getParameterCount() == 0,
              "default constructor takes no arguments");
        check(java.lang.reflect.Modifier.isProtected(
              c.getDeclaredConstructors()[0].getModifiers()), "constructor is protected");
        for (java.lang.reflect.Method m : java.nio.file.FileVisitor.class.getMethods()) {
            boolean found = false;
            for (java.lang.reflect.Method d : dm)
                if (d.getName().equals(m.getName())) found = true;
            check(found, "implements default for " + m.getName());
        }
    }

    static void testBlackBoxDeep() throws Exception {
        MyVisitor v = new MyVisitor();
        Path dir = Files.createTempDirectory("jck-sfv");
        try {
            Path f = dir.resolve("d.txt");
            Files.writeString(f, "d");
            BasicFileAttributes attrs = Files.readAttributes(f,
                    java.nio.file.attribute.BasicFileAttributes.class);

            // null arguments are rejected by the defaults (Objects.requireNonNull, probe-confirmed)
            checkThrows(NullPointerException.class, () -> v.visitFile(f, null),
                  "visitFile(file, null attrs) -> NullPointerException");
            checkThrows(NullPointerException.class, () -> v.visitFile(null, attrs),
                  "visitFile(null file, attrs) -> NullPointerException");
            checkThrows(NullPointerException.class, () -> v.preVisitDirectory(dir, null),
                  "preVisitDirectory(dir, null attrs) -> NullPointerException");
            checkThrows(NullPointerException.class, () -> v.preVisitDirectory(null, attrs),
                  "preVisitDirectory(null dir, attrs) -> NullPointerException");

            // postVisitDirectory re-throws the terminating IOException by identity
            IOException stop = new IOException("stop-walk");
            boolean sameObject = false;
            try { v.postVisitDirectory(dir, stop); }
            catch (IOException e) { sameObject = (e == stop); }
            check(sameObject, "postVisitDirectory(dir, ioe) rethrows the SAME IOException");

            // null path is rejected before the rethrow (probe-confirmed requireNonNull)
            IOException err = new IOException("failed");
            checkThrows(NullPointerException.class, () -> v.visitFileFailed(null, err),
                  "visitFileFailed(null file, ioe) -> NullPointerException");
            checkThrows(NullPointerException.class,
                  () -> v.postVisitDirectory(null, null),
                  "postVisitDirectory(null dir, ...) -> NullPointerException");

            // subclass can delegate to the protected default via super
            class Delegating extends SimpleFileVisitor<Path> {
                IOException seen;
                public FileVisitResult visitFileFailed(Path p, IOException e) throws IOException {
                    seen = e;
                    return super.visitFileFailed(p, e);
                }
            }
            Delegating d = new Delegating();
            IOException subErr = new IOException("sub");
            boolean superRethrew = false;
            try { d.visitFileFailed(f, subErr); }
            catch (IOException e) { superRethrew = (e == subErr); }
            check(d.seen == subErr && superRethrew, "super.visitFileFailed rethrows after observation");

            // generic instantiation with a non-Path type parameter
            SimpleFileVisitor<Object> ov = new SimpleFileVisitor<>() { };
            check(ov.visitFile(new Object(), attrs) == FileVisitResult.CONTINUE,
                  "SimpleFileVisitor<Object> accepts any T");

            // full traversal through inherited defaults
            Path sub = dir.resolve("sub");
            Files.createDirectory(sub);
            Files.writeString(sub.resolve("x.txt"), "x");
            Files.writeString(sub.resolve("y.txt"), "y");
            final java.util.concurrent.atomic.AtomicInteger pre =
                    new java.util.concurrent.atomic.AtomicInteger();
            final java.util.concurrent.atomic.AtomicInteger files =
                    new java.util.concurrent.atomic.AtomicInteger();
            final java.util.concurrent.atomic.AtomicInteger post =
                    new java.util.concurrent.atomic.AtomicInteger();
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                public FileVisitResult preVisitDirectory(Path p, BasicFileAttributes a)
                        throws IOException {
                    pre.incrementAndGet();
                    return super.preVisitDirectory(p, a);
                }
                public FileVisitResult visitFile(Path p, BasicFileAttributes a)
                        throws IOException {
                    files.incrementAndGet();
                    return super.visitFile(p, a);
                }
                public FileVisitResult postVisitDirectory(Path p, IOException e)
                        throws IOException {
                    post.incrementAndGet();
                    return super.postVisitDirectory(p, e);
                }
            });
            check(pre.get() == 2, "two directories entered (root + sub)");
            check(files.get() == 3, "three files visited, got " + files.get());
            check(post.get() == 2, "two postVisitDirectory callbacks");

            // bare SimpleFileVisitor subclass walks a flat tree without overrides
            Path flat = Files.createTempDirectory("jck-sfv2");
            try {
                Files.writeString(flat.resolve("one"), "1");
                Files.writeString(flat.resolve("two"), "2");
                Files.walkFileTree(flat, new MyVisitor());
                check(true, "bare subclass completes a flat walk (no exception)");
            } finally {
                Files.deleteIfExists(flat.resolve("one"));
                Files.deleteIfExists(flat.resolve("two"));
                Files.deleteIfExists(flat);
            }
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) { } });
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxDeep();
        testBlackBoxDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
