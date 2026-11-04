/*
 * @test
 * @summary javax.tools.ToolProvider conformance: tool provider.
 * @run main ToolProviderTest
 */

public class ToolProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        check(jc != null, "getSystemJavaCompiler");
        check(jc instanceof javax.tools.Tool, "compiler is a Tool");
        check(javax.tools.ToolProvider.getSystemDocumentationTool() != null, "getSystemDocumentationTool");
        check(javax.tools.ToolProvider.getSystemToolClassLoader() != null, "getSystemToolClassLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
