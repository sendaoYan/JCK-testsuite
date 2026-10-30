/*
 * @test
 * @summary JLS 12.8: System.exit causes the shutdown hooks started by the JVM to
 *          run before the program exits.
 * @compile ShutdownHook.java Child.java
 * @run main ShutdownHook
 */

import java.io.File;
import java.nio.file.Files;

public class ShutdownHook {
    public static void main(String[] args) throws Exception {
        File f = File.createTempFile("shutdown-hook", ".txt");
        f.delete();
        String javaHome = System.getProperty("java.home");
        String classPath = System.getProperty("java.class.path");
        Process p = new ProcessBuilder(
                javaHome + "/bin/java", "-cp", classPath,
                "Child", f.getAbsolutePath())
                .redirectErrorStream(true)
                .start();
        int exit = p.waitFor();
        if (exit != 0) throw new AssertionError("child exit: " + exit);
        String content = new String(Files.readAllBytes(f.toPath())).trim();
        f.delete();
        if (!"hook-ran".equals(content))
            throw new AssertionError("shutdown hook did not run: '" + content + "'");
        System.out.println("PASS");
    }
}
