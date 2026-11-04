/*
 * @test
 * @summary JLS 13.4.12: adding a method or constructor to a class does not break pre-existing binaries.
 * @run main AddMethod
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class AddMethod {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    public static void main(String[] args) throws Exception {
        Path d = Files.createTempDirectory("jls13412-am");
        write(d, "C.java", "class C { }");
        write(d, "Client.java", "public class Client { public static void main(String[] a){ new C(); System.out.println(\"ok\"); } }");
        javac(d, "C.java", "Client.java");
        write(d, "C.java", "class C { int m(){ return 1; } }");
        javac(d, "C.java");
        String out = run(d, javaBin(), "-cp", ".", "Client");
        check(out.startsWith("exit=0"), "client exits normally");
        check(out.contains("ok"), "output contains ok");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String jdkHome() {
        String h = System.getProperty("java.home");
        java.io.File f = new java.io.File(h);
        return "jre".equals(f.getName()) ? f.getParent() : h;
    }
    static String javaBin() { return jdkHome() + "/bin/java"; }
    static String javacBin() { return jdkHome() + "/bin/javac"; }
    static void write(Path d, String name, String content) throws IOException {
        Path p = d.resolve(name);
        if (p.getParent() != null) Files.createDirectories(p.getParent());
        Files.write(p, content.getBytes("UTF-8"));
    }
    static void javac(Path d, String... files) throws Exception {
        String[] c = new String[files.length + 5];
        c[0] = javacBin(); c[1] = "-cp"; c[2] = "."; c[3] = "-d"; c[4] = ".";
        System.arraycopy(files, 0, c, 5, files.length);
        String out = run(d, c);
        if (!out.startsWith("exit=0")) throw new AssertionError("javac failed:\n" + out);
    }
    static String run(Path d, String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).directory(d.toFile()).redirectErrorStream(true).start();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
        }
        int exit = p.waitFor();
        return "exit=" + exit + "\n" + sb;
    }
}
