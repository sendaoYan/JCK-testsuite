/*
 * @test
 * @summary com.sun.xml.internal.stream.buffer.stax.StreamWriterBufferProcessor conformance: class hierarchy and members.
 * @run main StreamWriterBufferProcessorTest
 */

public class StreamWriterBufferProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.buffer.stax.StreamWriterBufferProcessor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.buffer.stax.StreamWriterBufferProcessor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.buffer.AbstractProcessor".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.buffer.AbstractProcessor");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("fixNull"), "has method fixNull");
        check(mn.contains("isInscope"), "has method isInscope");
        check(mn.contains("process"), "has method process");
        check(mn.contains("setXMLStreamBuffer"), "has method setXMLStreamBuffer");
        check(mn.contains("write"), "has method write");
        check(mn.contains("writeAttributes"), "has method writeAttributes");
        check(mn.contains("writeFragment"), "has method writeFragment");
        check(mn.contains("writeFragmentEx"), "has method writeFragmentEx");
        check(mn.contains("writeFragmentNoEx"), "has method writeFragmentNoEx");
        check(mn.contains("writeInscopeNamespaces"), "has method writeInscopeNamespaces");
        check(mn.contains("writeNamespaceAttributes"), "has method writeNamespaceAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
