/*
 * @test
 * @summary com.sun.rowset.internal.WebRowSetXmlReader conformance: hierarchy and declared members via reflection.
 * @run main WebRowSetXmlReaderTest
 */

public class WebRowSetXmlReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.WebRowSetXmlReader", false, null);
        check(c.getName().equals("com.sun.rowset.internal.WebRowSetXmlReader"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.sql.rowset.spi.XmlReader"), "implements javax.sql.rowset.spi.XmlReader");
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("readData"), "declares readData");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("readXML"), "declares readXML");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
