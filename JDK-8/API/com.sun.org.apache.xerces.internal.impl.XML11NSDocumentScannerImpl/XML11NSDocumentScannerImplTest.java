/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XML11NSDocumentScannerImpl conformance: class hierarchy and members.
 * @run main XML11NSDocumentScannerImplTest
 */
public class XML11NSDocumentScannerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XML11NSDocumentScannerImpl", false, XML11NSDocumentScannerImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.XML11DocumentScannerImpl"), "superclass com.sun.org.apache.xerces.internal.impl.XML11DocumentScannerImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setDTDValidator"), "method setDTDValidator");
        check(mn.contains("scanStartElement"), "method scanStartElement");
        check(mn.contains("scanStartElementName"), "method scanStartElementName");
        check(mn.contains("scanStartElementAfterName"), "method scanStartElementAfterName");
        check(mn.contains("scanAttribute"), "method scanAttribute");
        check(mn.contains("scanEndElement"), "method scanEndElement");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("createContentDriver"), "method createContentDriver");
        check(mn.contains("next"), "method next");
        check(mn.contains("access$000"), "method access$000");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
