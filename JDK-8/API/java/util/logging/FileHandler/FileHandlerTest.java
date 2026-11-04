/*
 * @test
 * @summary java.util.logging.FileHandler conformance: white-box structure + black-box behavior.
 * @run main FileHandlerTest
 */

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;
import java.util.stream.Stream;

public class FileHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName() +
                    " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but none thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.FileHandler");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(StreamHandler.class.isAssignableFrom(c), "extends StreamHandler");
        check(c.getSuperclass() == StreamHandler.class, "direct superclass StreamHandler");
        check(Handler.class.isAssignableFrom(c), "is a Handler");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c.getConstructor(String.class) != null, "(String) constructor");
        check(c.getConstructor(String.class, boolean.class) != null, "(String,boolean) constructor");
        check(c.getConstructor(String.class, int.class, int.class) != null, "(String,int,int) constructor");
        check(c.getConstructor(String.class, int.class, int.class, boolean.class) != null,
                "(String,int,int,boolean) constructor");
        check(c.getMethod("publish", LogRecord.class) != null, "publish(LogRecord)");
        check(c.getMethod("close") != null, "close()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // argument validation (no file touched)
        expectThrows(IllegalArgumentException.class,
                () -> new FileHandler("", 0, 1, false), "empty pattern IAE");
        expectThrows(IllegalArgumentException.class,
                () -> new FileHandler("jck.log", 0, 0, false), "count < 1 IAE");
        expectThrows(IllegalArgumentException.class,
                () -> new FileHandler("jck.log", -1, 1, false), "limit < 0 IAE");

        Path dir = Files.createTempDirectory("jck");
        try {
            String pattern = dir.resolve("jck%u%g.log").toString();
            FileHandler fh = new FileHandler(pattern, 1000, 1, false);
            check(fh.getLevel() == Level.ALL, "default level ALL");
            check(fh.getFormatter() instanceof XMLFormatter, "default formatter XMLFormatter");

            fh.publish(new LogRecord(Level.INFO, "hello file"));
            fh.flush();
            fh.close();

            // at least one non-empty data file was produced in the temp dir
            boolean nonEmpty = false;
            try (Stream<Path> paths = Files.list(dir)) {
                for (Path p : (Iterable<Path>) paths::iterator) {
                    if (Files.isRegularFile(p) && Files.size(p) > 0) {
                        nonEmpty = true;
                        break;
                    }
                }
            }
            check(nonEmpty, "generated log file is non-empty");
        } finally {
            deleteRecursively(dir);
        }
    }

    static void deleteRecursively(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception e) { /* ignore */ }
            });
        }
    }


    // ===================== 契约深化：limit/count 轮转/%t %u 模式/临时目录清理（finally） =====================
    static void testContracts() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck-filehandler");
        try {
            // 轮转：limit=100, count=3 → 恰好 3 个文件，按 .0/.1/.2 轮转
            java.util.logging.FileHandler fh = new java.util.logging.FileHandler(
                    dir + "/app_%u.log", 100, 3, true);
            java.util.logging.Logger l = java.util.logging.Logger.getLogger("jck.fh.rotate");
            l.setUseParentHandlers(false);
            l.addHandler(fh);
            l.setLevel(java.util.logging.Level.ALL);
            fh.setLevel(java.util.logging.Level.ALL);
            for (int i = 0; i < 30; i++) l.info("row-" + i + "-paddingpaddingpadding");
            fh.close();
            l.removeHandler(fh);
            java.io.File[] files = new java.io.File(dir.toString()).listFiles();
            check(files.length == 3, "rotation produces exactly count=3 files (saw " + files.length + ")");
            int withSuffix = 0;
            for (java.io.File f : files)
                if (f.getName().matches("app_\\d+\\.log\\.[012]")) withSuffix++;
            check(withSuffix == 3, "rotated names carry generation suffix .0/.1/.2");
            long total = 0;
            for (java.io.File f : files) total += f.length();
            check(total > 0, "records written to disk");
            // %t/%u 模式：写到 java.io.tmpdir 并清理
            java.util.logging.FileHandler tmp = new java.util.logging.FileHandler("%t/jck_fh_probe_%u.log");
            tmp.close();
            java.io.File[] tmps = new java.io.File(System.getProperty("java.io.tmpdir")).listFiles(
                    (d, n) -> n.startsWith("jck_fh_probe_"));
            check(tmps != null && tmps.length >= 1, "%t pattern resolves into java.io.tmpdir");
            for (java.io.File f : tmps) check(f.delete(), "cleaned " + f.getName());
            // getEncoding/setEncoding 往返（默认 null）
            java.util.logging.FileHandler e = new java.util.logging.FileHandler(
                    dir + "/enc_%u.log", true);
            check(e.getEncoding() == null, "default encoding null");
            e.setEncoding("UTF-8");
            check("UTF-8".equals(e.getEncoding()), "setEncoding round-trip");
            e.close();
            // null 模式 NPE
            try {
                new java.util.logging.FileHandler(null);
                check(false, "FileHandler(null) NPE");
            } catch (NullPointerException ex) { check(true, "FileHandler(null) NPE"); }
        } finally {
            for (java.io.File f : dir.toFile().listFiles()) f.delete();
            java.nio.file.Files.delete(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
