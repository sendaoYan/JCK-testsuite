/*
 * @test
 * @summary JLS 13.4.8: deleting a field from a class breaks compatibility with
 *          pre-existing binaries that reference it, throwing NoSuchFieldError.
 * @run main DeleteFieldBreaksBinary
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeleteFieldBreaksBinary {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("jls1348");

        write(dir, "Super.java", "public class Super { public int f = 1; }");
        write(dir, "Client.java",
                "public class Client { public static void main(String[] a) { System.out.println(new Super().f); } }");
        javac(dir, "Super.java", "Client.java");

        // Replace Super with a version that no longer declares field f.
        write(dir, "Super.java", "public class Super { }");
        javac(dir, "Super.java");

        String out = run(dir, javaBin(), "-cp", ".", "Client");
        if (!out.contains("NoSuchFieldError"))
            throw new AssertionError("expected NoSuchFieldError, got:\n" + out);
        System.out.println("PASS");
    }

    static String javaBin() { return System.getProperty("java.home") + "/bin/java"; }
    static String javacBin() { return System.getProperty("java.home") + "/bin/javac"; }

    static void write(Path dir, String name, String content) throws IOException {
        Files.writeString(dir.resolve(name), content);
    }

    static void javac(Path dir, String... files) throws Exception {
        String[] cmd = new String[files.length + 3];
        cmd[0] = javacBin();
        cmd[1] = "-d";
        cmd[2] = ".";
        System.arraycopy(files, 0, cmd, 3, files.length);
        String out = run(dir, cmd);
        if (!out.startsWith("exit=0")) throw new AssertionError("javac failed:\n" + out);
    }

    static String run(Path dir, String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).directory(dir.toFile())
                .redirectErrorStream(true).start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) out.append(line).append('\n');
        }
        int exit = p.waitFor();
        return "exit=" + exit + "\n" + out;
    }
}
