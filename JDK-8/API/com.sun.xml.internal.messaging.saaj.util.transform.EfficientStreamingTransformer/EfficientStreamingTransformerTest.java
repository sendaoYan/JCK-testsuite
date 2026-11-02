/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.transform.EfficientStreamingTransformer conformance: class hierarchy and members.
 * @run main EfficientStreamingTransformerTest
 */

public class EfficientStreamingTransformerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.transform.EfficientStreamingTransformer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.transform.EfficientStreamingTransformer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.transform.Transformer".equals(c.getSuperclass().getName()), "extends javax.xml.transform.Transformer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clearParameters"), "has method clearParameters");
        check(mn.contains("getErrorListener"), "has method getErrorListener");
        check(mn.contains("getInputStreamFromSource"), "has method getInputStreamFromSource");
        check(mn.contains("getOutputProperties"), "has method getOutputProperties");
        check(mn.contains("getOutputProperty"), "has method getOutputProperty");
        check(mn.contains("getParameter"), "has method getParameter");
        check(mn.contains("getURIResolver"), "has method getURIResolver");
        check(mn.contains("materialize"), "has method materialize");
        check(mn.contains("newTransformer"), "has method newTransformer");
        check(mn.contains("setErrorListener"), "has method setErrorListener");
        check(mn.contains("setOutputProperties"), "has method setOutputProperties");
        check(mn.contains("setOutputProperty"), "has method setOutputProperty");
        check(mn.contains("setParameter"), "has method setParameter");
        check(mn.contains("setURIResolver"), "has method setURIResolver");
        check(mn.contains("transform"), "has method transform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
