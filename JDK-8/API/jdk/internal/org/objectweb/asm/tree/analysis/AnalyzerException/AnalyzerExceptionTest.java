/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.tree.analysis.AnalyzerException conformance: exception/value behavior.
 * @run main AnalyzerExceptionTest
 */

import java.lang.reflect.Method;

public class AnalyzerExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.tree.analysis.AnalyzerException", false, AnalyzerExceptionTest.class.getClassLoader());
        Class<?> node = Class.forName("jdk.internal.org.objectweb.asm.tree.AbstractInsnNode", false, AnalyzerExceptionTest.class.getClassLoader());
        Object e = c.getConstructor(node, String.class).newInstance(null, "msg");
        check(c.getField("node").get(e) == null, "node field null");
        check("msg".equals(c.getMethod("getMessage").invoke(e)), "getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
