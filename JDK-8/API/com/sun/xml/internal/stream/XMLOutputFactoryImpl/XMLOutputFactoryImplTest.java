/*
 * @test
 * @summary com.sun.xml.internal.stream.XMLOutputFactoryImpl conformance: class hierarchy and members.
 * @run main XMLOutputFactoryImplTest
 */

public class XMLOutputFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.XMLOutputFactoryImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.XMLOutputFactoryImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.stream.XMLOutputFactory".equals(c.getSuperclass().getName()), "extends javax.xml.stream.XMLOutputFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createXMLEventWriter"), "has method createXMLEventWriter");
        check(mn.contains("createXMLStreamWriter"), "has method createXMLStreamWriter");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("isPropertySupported"), "has method isPropertySupported");
        check(mn.contains("setProperty"), "has method setProperty");
        check(mn.contains("toStreamResult"), "has method toStreamResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
