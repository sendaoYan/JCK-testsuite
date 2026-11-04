/*
 * @test
 * @summary com.sun.xml.internal.stream.XMLInputFactoryImpl conformance: class hierarchy and members.
 * @run main XMLInputFactoryImplTest
 */

public class XMLInputFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.XMLInputFactoryImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.XMLInputFactoryImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.stream.XMLInputFactory".equals(c.getSuperclass().getName()), "extends javax.xml.stream.XMLInputFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createFilteredReader"), "has method createFilteredReader");
        check(mn.contains("createXMLEventReader"), "has method createXMLEventReader");
        check(mn.contains("createXMLStreamReader"), "has method createXMLStreamReader");
        check(mn.contains("getEventAllocator"), "has method getEventAllocator");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("getXMLReporter"), "has method getXMLReporter");
        check(mn.contains("getXMLResolver"), "has method getXMLResolver");
        check(mn.contains("getXMLStreamReaderImpl"), "has method getXMLStreamReaderImpl");
        check(mn.contains("initEventReader"), "has method initEventReader");
        check(mn.contains("isPropertySupported"), "has method isPropertySupported");
        check(mn.contains("jaxpSourcetoXMLInputSource"), "has method jaxpSourcetoXMLInputSource");
        check(mn.contains("setEventAllocator"), "has method setEventAllocator");
        check(mn.contains("setProperty"), "has method setProperty");
        check(mn.contains("setXMLReporter"), "has method setXMLReporter");
        check(mn.contains("setXMLResolver"), "has method setXMLResolver");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
