/*
 * @test
 * @summary JLS 12.8: Runtime.halt terminates the program without starting shutdown
 *          hooks (unlike System.exit, which does run them).
 * @compile RuntimeHalt.java HaltChild.java
 * @run main RuntimeHalt
 */

import java.io.File;
import java.nio.file.Files;

public class RuntimeHalt {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        File started = File.createTempFile("halt-started", ".txt");
        File hookRan = File.createTempFile("halt-hook", ".txt");
        started.delete();
        hookRan.delete();

        String javaHome = System.getProperty("java.home");
        String classPath = System.getProperty("java.class.path");
        Process p = new ProcessBuilder(
                javaHome + "/bin/java", "-cp", classPath,
                "HaltChild", started.getAbsolutePath(), hookRan.getAbsolutePath())
                .redirectErrorStream(true)
                .start();
        int exit = p.waitFor();
        check(exit == 0, "child exit code 0 (was " + exit + ")");
        check(started.exists(), "child started");
        check(!hookRan.exists(), "shutdown hook did NOT run under Runtime.halt");

        started.delete();
        hookRan.delete();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
