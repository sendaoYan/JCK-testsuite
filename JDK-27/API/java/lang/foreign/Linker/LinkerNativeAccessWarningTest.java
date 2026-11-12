/*
 * @test
 * @summary --enable-native-access warning semantics (JEP 472): without the flag
 *          both System.load and Linker::downcallHandle print the restricted-
 *          method warning block to stderr; with --enable-native-access=ALL-UNNAMED
 *          the same operations produce no warnings. Verified by driving fresh
 *          child JVMs (the test JVM itself runs with the flag enabled).
 * @run main/othervm --enable-native-access=ALL-UNNAMED LinkerNativeAccessWarningTest
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class LinkerNativeAccessWarningTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean gccAvailable() {
        try {
            Process p = new ProcessBuilder("gcc", "--version").start();
            try { p.getInputStream().close(); } catch (Exception ignore) { }
            try { p.getOutputStream().close(); } catch (Exception ignore) { }
            try { p.getErrorStream().close(); } catch (Exception ignore) { }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Child program executed in fresh JVMs. Mode "load" calls System.load on the
    // compiled library, mode "ffm" links and invokes a downcall. Everything the
    // child prints goes to stdout; the assertions inspect stderr.
    static final String CHILD_SRC = """
            import java.lang.foreign.*;
            import java.lang.invoke.MethodHandle;

            public class NativeAccessChild {
                public static void main(String[] args) throws Throwable {
                    if (args[0].equals("load")) {
                        System.load(args[1]);
                        System.out.println("child-loaded");
                    } else {
                        Linker linker = Linker.nativeLinker();
                        MethodHandle labs = linker.downcallHandle(
                                linker.defaultLookup().findOrThrow("labs"),
                                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
                        System.out.println("child-labs=" + (long) labs.invoke(-7L));
                    }
                }
            }
            """;

    static final String C_SRC = "int jck_identity(int v) { return v; }\n";

    static Path scratch;

    static void compileChildAndLib() throws Exception {
        scratch = Paths.get(System.getProperty("user.dir"));
        Files.writeString(scratch.resolve("NativeAccessChild.java"), CHILD_SRC);
        Path cSrc = scratch.resolve("LinkerNativeAccessWarningTest_jckffm.c");
        Path so = scratch.resolve("libLinkerNativeAccessWarningTest_jckffm.so");
        Files.writeString(cSrc, C_SRC);
        Process gcc = new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-o", so.toString(), cSrc.toString()).inheritIO().start();
        if (!gcc.waitFor(60, TimeUnit.SECONDS)) {
            gcc.destroyForcibly();
            throw new IllegalStateException("gcc did not finish within 60s");
        }
        if (gcc.exitValue() != 0)
            throw new IllegalStateException("gcc exited " + gcc.exitValue());
    }

    static String javaBin() {
        return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
    }

    record ChildResult(int exit, String stdout, String stderr) { }

    static ChildResult runChild(String flag, String mode) throws Exception {
        ProcessBuilder pb = flag == null
                ? new ProcessBuilder(javaBin(), "NativeAccessChild.java", mode,
                        scratch.resolve("libLinkerNativeAccessWarningTest_jckffm.so").toString())
                : new ProcessBuilder(javaBin(), flag, "NativeAccessChild.java", mode,
                        scratch.resolve("libLinkerNativeAccessWarningTest_jckffm.so").toString());
        pb.directory(scratch.toFile());
        Process p = pb.start();
        StringBuilder out = new StringBuilder(), err = new StringBuilder();
        Thread outReader = new Thread(() -> { try { out.append(new String(p.getInputStream().readAllBytes())); } catch (IOException ignored) { } });
        Thread errReader = new Thread(() -> { try { err.append(new String(p.getErrorStream().readAllBytes())); } catch (IOException ignored) { } });
        outReader.start();
        errReader.start();
        if (!p.waitFor(60, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("child JVM did not finish within 60s");
        }
        // 子进程退出后其管道已 EOF，但读线程可能仍在排空缓冲；join 后再取结果，避免读到截断输出。
        // join 不带超时：上面 waitFor 已返回 true（子进程退出），管道必已 EOF，读线程会立即读完退出；
        // 带 10s 超时反而会在 -Xcomp 下 CPU 争用导致读线程未排空就被取走、读到截断输出。
        outReader.join();
        errReader.join();
        return new ChildResult(p.exitValue(), out.toString(), err.toString());
    }

    public static void main(String[] args) throws Exception {
        if (!gccAvailable()) {
            System.out.println("SKIP: no C compiler (gcc) available; native library cannot be built");
            return;
        }
        compileChildAndLib();
        String flag = "--enable-native-access=ALL-UNNAMED";

        // System.load without the flag: warning block on stderr, still succeeds
        ChildResult loadNo = runChild(null, "load");
        check(loadNo.exit() == 0, "System.load succeeds without flag (exit 0)");
        check(loadNo.stdout().contains("child-loaded"), "child load produced expected stdout");
        check(loadNo.stderr().contains("WARNING"), "System.load without flag warns on stderr");
        check(loadNo.stderr().contains("System"), "warning names System as the restricted callee");
        check(loadNo.stderr().contains("native-access"), "warning suggests --enable-native-access");

        // System.load with the flag: no warnings at all
        ChildResult loadYes = runChild(flag, "load");
        check(loadYes.exit() == 0, "System.load succeeds with flag (exit 0)");
        check(loadYes.stdout().contains("child-loaded"), "child load with flag produced expected stdout");
        check(!loadYes.stderr().contains("WARNING"), "System.load with flag prints no warning");

        // Linker downcall without the flag: restricted-method warning
        ChildResult ffmNo = runChild(null, "ffm");
        check(ffmNo.exit() == 0, "downcall succeeds without flag (exit 0)");
        check(ffmNo.stdout().contains("child-labs=7"), "child downcall computed labs(-7) == 7");
        check(ffmNo.stderr().contains("WARNING"), "downcallHandle without flag warns on stderr");
        check(ffmNo.stderr().contains("Linker"), "warning names Linker as the restricted callee");

        // Linker downcall with the flag: clean run
        ChildResult ffmYes = runChild(flag, "ffm");
        check(ffmYes.exit() == 0, "downcall succeeds with flag (exit 0)");
        check(ffmYes.stdout().contains("child-labs=7"), "child downcall with flag computed labs(-7) == 7");
        check(!ffmYes.stderr().contains("WARNING"), "downcallHandle with flag prints no warning");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
