/*
 * @test
 * @summary JLS 13.4.6 (Example 13.4.6-1): deleting an overriding method and
 *          recompiling the class does not break a pre-existing client binary,
 *          which now resolves the call to the superclass method.
 * @run main ChangeClassBody
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChangeClassBody {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("jls1346");
        write(dir, "Hyper.java",
                "public class Hyper { void hello() { System.out.println(\"hello from Hyper\"); } }");
        write(dir, "Super.java",
                "public class Super extends Hyper { void hello() { System.out.println(\"hello from Super\"); } }");
        write(dir, "Test.java",
                "public class Test { public static void main(String[] a) { new Super().hello(); } }");
        javac(dir, "Hyper.java", "Super.java", "Test.java");

        write(dir, "Super.java", "public class Super extends Hyper {}");
        javac(dir, "Super.java");

        String out = run(dir, javaBin(), "-cp", ".", "Test");
        if (!out.contains("hello from Hyper"))
            throw new AssertionError("expected 'hello from Hyper', got:\n" + out);
        System.out.println("PASS");
    }

    static String javaBin() { return System.getProperty("java.home") + "/bin/java"; }
    static String javacBin() { return System.getProperty("java.home") + "/bin/javac"; }

    static void write(Path dir, String name, String content) throws IOException {
        Path p = dir.resolve(name);
        if (p.getParent() != null) Files.createDirectories(p.getParent());
        Files.writeString(p, content);
    }

    static void javac(Path dir, String... files) throws Exception {
        String[] cmd = new String[files.length + 5];
        cmd[0] = javacBin();
        cmd[1] = "-cp"; cmd[2] = ".";
        cmd[3] = "-d";  cmd[4] = ".";
        System.arraycopy(files, 0, cmd, 5, files.length);
        String out = run(dir, cmd);
        if (!out.startsWith("exit=0")) throw new AssertionError("javac failed:\n" + out);
    }

    static String run(Path dir, String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true).start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) out.append(line).append('\n');
        }
        int exit = p.waitFor();
        return "exit=" + exit + "\n" + out;
    }
}
