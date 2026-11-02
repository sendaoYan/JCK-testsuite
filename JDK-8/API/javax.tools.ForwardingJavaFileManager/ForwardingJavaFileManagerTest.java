/*
 * @test
 * @summary javax.tools.ForwardingJavaFileManager conformance: forwarding java file manager.
 * @run main ForwardingJavaFileManagerTest
 */

public class ForwardingJavaFileManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        check(jc != null, "compiler");
        javax.tools.StandardJavaFileManager fm = jc.getStandardFileManager(null, null, null);
        javax.tools.ForwardingJavaFileManager<javax.tools.JavaFileManager> m = new javax.tools.ForwardingJavaFileManager<javax.tools.JavaFileManager>(fm) {};
        check(m.getClassLoader(javax.tools.StandardLocation.CLASS_PATH) != null, "getClassLoader");
        check(m.hasLocation(javax.tools.StandardLocation.CLASS_PATH), "hasLocation");
        check(m.isSupportedOption("-classpath") >= 0, "isSupportedOption");
        m.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
