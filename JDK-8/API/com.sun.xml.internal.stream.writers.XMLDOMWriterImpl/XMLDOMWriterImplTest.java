/*
 * @test
 * @summary com.sun.xml.internal.stream.writers.XMLDOMWriterImpl conformance: class hierarchy and members.
 * @run main XMLDOMWriterImplTest
 */

public class XMLDOMWriterImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.writers.XMLDOMWriterImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.writers.XMLDOMWriterImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.XMLStreamWriter"), "has interface javax.xml.stream.XMLStreamWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("getDLThreeMethods"), "has method getDLThreeMethods");
        check(mn.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(mn.contains("getNode"), "has method getNode");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("getQName"), "has method getQName");
        check(mn.contains("incDepth"), "has method incDepth");
        check(mn.contains("setDefaultNamespace"), "has method setDefaultNamespace");
        check(mn.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(mn.contains("setPrefix"), "has method setPrefix");
        check(mn.contains("writeAttribute"), "has method writeAttribute");
        check(mn.contains("writeCData"), "has method writeCData");
        check(mn.contains("writeCharacters"), "has method writeCharacters");
        check(mn.contains("writeComment"), "has method writeComment");
        check(mn.contains("writeDTD"), "has method writeDTD");
        check(mn.contains("writeDefaultNamespace"), "has method writeDefaultNamespace");
        check(mn.contains("writeEmptyElement"), "has method writeEmptyElement");
        check(mn.contains("writeEndDocument"), "has method writeEndDocument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
