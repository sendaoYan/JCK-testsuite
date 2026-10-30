/*
 * @test
 * @summary JLS 13.4.9 (Example 13.4.9-1): changing a non-final field to final
 *          causes a pre-existing binary that assigns to it to throw
 *          IllegalAccessError.
 * @run main FinalFieldChange
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class FinalFieldChange {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("jls1349");
        write(dir, "Super.java", "public class Super { public char s; }");
        write(dir, "Client.java",
                "public class Client { public static void main(String[] a) { Super x = new Super(); x.s = 'a'; System.out.println(x.s); } }");
        javac(dir, "Super.java", "Client.java");

        write(dir, "Super.java", "public class Super { public final char s = 'b'; }");
        javac(dir, "Super.java");

        String out = run(dir, javaBin(), "-cp", ".", "Client");
        if (!out.contains("IllegalAccessError"))
            throw new AssertionError("expected IllegalAccessError, got:\n" + out);
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
