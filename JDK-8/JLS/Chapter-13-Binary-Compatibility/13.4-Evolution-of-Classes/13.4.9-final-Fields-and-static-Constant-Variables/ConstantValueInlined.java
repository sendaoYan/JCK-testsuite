/*
 * @test
 * @summary JLS 13.4.9 / 4.12.4: a static final field with a constant-expression initializer is a constant variable; its value is inlined, so a pre-existing binary keeps the old value.
 * @run main ConstantValueInlined
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConstantValueInlined {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    public static void main(String[] args) throws Exception {
        Path d = Files.createTempDirectory("jls1349-cvi");
        write(d, "C.java", "public class C { public static final int X = 1; public static final String S = \"old\"; }");
        write(d, "Client.java", "public class Client { public static void main(String[] a){ System.out.println(C.X + \":\" + C.S); } }");
        javac(d, "C.java", "Client.java");
        write(d, "C.java", "public class C { public static final int X = 2; public static final String S = \"new\"; }");
        javac(d, "C.java");
        String out = run(d, javaBin(), "-cp", ".", "Client");
        check(out.startsWith("exit=0"), "client exits normally");
        check(out.contains("1:old"), "output contains 1:old");
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
