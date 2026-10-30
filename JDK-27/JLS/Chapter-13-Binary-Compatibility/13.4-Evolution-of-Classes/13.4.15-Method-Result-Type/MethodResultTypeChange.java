/*
 * @test
 * @summary JLS 13.4.15: changing a method's result type changes its descriptor, so
 *          a pre-existing binary that invokes the old signature throws
 *          NoSuchMethodError.
 * @run main MethodResultTypeChange
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class MethodResultTypeChange {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("jls13415");
        write(dir, "C.java", "public class C { public int m() { return 1; } }");
        write(dir, "Client.java",
                "public class Client { public static void main(String[] a) { System.out.println(new C().m()); } }");
        javac(dir, "C.java", "Client.java");

        write(dir, "C.java", "public class C { public long m() { return 1L; } }");
        javac(dir, "C.java");

        String out = run(dir, javaBin(), "-cp", ".", "Client");
        if (!out.contains("NoSuchMethodError"))
            throw new AssertionError("expected NoSuchMethodError, got:\n" + out);
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
