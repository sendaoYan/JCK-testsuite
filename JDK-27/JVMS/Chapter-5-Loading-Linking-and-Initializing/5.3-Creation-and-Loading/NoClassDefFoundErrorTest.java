/*
 * @test
 * @summary JVMS 5.3: when a class needed at run time cannot be found during
 *          linking, the JVM throws NoClassDefFoundError.
 * @run main NoClassDefFoundErrorTest
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NoClassDefFoundErrorTest {
    static String javaBin() { return System.getProperty("java.home") + "/bin/java"; }
    static String javacBin() { return System.getProperty("java.home") + "/bin/javac"; }

    static void write(Path dir, String name, String content) throws IOException {
        Files.writeString(dir.resolve(name), content);
    }

    static void javac(Path dir, String... files) throws Exception {
        String[] cmd = new String[files.length + 1];
        cmd[0] = javacBin();
        System.arraycopy(files, 0, cmd, 1, files.length);
        Process p = new ProcessBuilder(cmd).directory(dir.toFile())
            .redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        if (p.waitFor() != 0) throw new AssertionError("javac failed: " + out);
    }

    static String run(Path dir, String mainClass) throws Exception {
        Process p = new ProcessBuilder(javaBin(), "-cp", dir.toString(), mainClass)
            .redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        p.waitFor();
        return out;
    }

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("ncdfe");
        write(dir, "A.java", "public class A { public static void main(String[] x) { new B(); } }");
        write(dir, "B.java", "public class B {}");
        javac(dir, "A.java", "B.java");

        // Remove the class needed at link/run time.
        Files.delete(dir.resolve("B.class"));

        String out = run(dir, "A");
        if (!out.contains("NoClassDefFoundError"))
            throw new AssertionError("expected NoClassDefFoundError, got:\n" + out);

        System.out.println("PASS");
    }
}
