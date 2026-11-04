/*
 * @test
 * @summary JLS 13.5.3: deleting a member from an interface breaks a pre-existing binary that references it, throwing NoSuchMethodError.
 * @run main InterfaceMemberDeleted
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class InterfaceMemberDeleted {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    public static void main(String[] args) throws Exception {
        Path d = Files.createTempDirectory("jls1353-imd");
        write(d, "I.java", "interface I { int m(); int n(); }");
        write(d, "C.java", "class C implements I { public int m(){ return 1; } public int n(){ return 2; } }");
        write(d, "Client.java", "public class Client { public static void main(String[] a){ I i = new C(); System.out.println(i.n()); } }");
        javac(d, "I.java", "C.java", "Client.java");
        write(d, "I.java", "interface I { int m(); }");
        javac(d, "I.java");
        String out = run(d, javaBin(), "-cp", ".", "Client");
        check(out.contains("NoSuchMethodError"), "output contains NoSuchMethodError");
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
