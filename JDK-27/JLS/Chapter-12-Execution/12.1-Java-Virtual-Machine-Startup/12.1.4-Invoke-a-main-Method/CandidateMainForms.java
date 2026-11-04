/*
 * @test
 * @summary JLS 12.1.4 (Java SE 27): a candidate main method may be static or an
 *          instance method, with a single String[]/String... parameter or no
 *          parameter, and public/protected/package access.
 * @compile CandidateMainForms.java NoArgStaticMain.java InstanceParamMain.java
 * @run main CandidateMainForms
 */

public class CandidateMainForms {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        String javaHome = System.getProperty("java.home");
        String classPath = System.getProperty("java.class.path");

        run(javaHome, classPath, "NoArgStaticMain", "NOARG-STATIC-OK");
        run(javaHome, classPath, "InstanceParamMain", "INSTANCE-PARAM-OK");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void run(String javaHome, String classPath, String className, String marker) throws Exception {
        Process p = new ProcessBuilder(javaHome + "/bin/java", "-cp", classPath, className)
                .redirectErrorStream(true)
                .start();
        String out = new String(p.getInputStream().readAllBytes());
        int exit = p.waitFor();
        check(exit == 0, className + " exit code 0 (was " + exit + ")");
        check(out.contains(marker), className + " printed " + marker + " (was: " + out.trim() + ")");
    }
}
