/*
 * @test
 * @summary JLS 13.5.1: changing a public interface to non-public throws IllegalAccessError for a pre-existing binary in another package.
 * @run main PublicInterfaceToNonPublic
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class PublicInterfaceToNonPublic {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    public static void main(String[] args) throws Exception {
        Path d = Files.createTempDirectory("jls1351-pitnp");
        write(d, "q/I.java", "package q; public interface I { int m(); }");
        write(d, "q/C.java", "package q; public class C implements I { public int m(){ return 1; } }");
        write(d, "p/Client.java", "package p; public class Client { public static void main(String[] a){ q.I i = new q.C(); System.out.println(i.m()); } }");
        javac(d, "q/I.java", "q/C.java", "p/Client.java");
        write(d, "q/I.java", "package q; interface I { int m(); }");
        javac(d, "q/I.java");
        String out = run(d, javaBin(), "-cp", ".", "p.Client");
        check(out.contains("IllegalAccessError"), "output contains IllegalAccessError");
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
