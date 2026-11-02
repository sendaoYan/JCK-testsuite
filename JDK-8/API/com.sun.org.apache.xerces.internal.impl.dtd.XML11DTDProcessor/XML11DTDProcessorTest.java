/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDProcessor conformance: class hierarchy and members.
 * @run main XML11DTDProcessorTest
 */
public class XML11DTDProcessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDProcessor", false, XML11DTDProcessorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader"), "superclass com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDLoader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isValidNmtoken"), "method isValidNmtoken");
        check(mn.contains("isValidName"), "method isValidName");
        check(mn.contains("createDTDScanner"), "method createDTDScanner");
        check(mn.contains("getScannerVersion"), "method getScannerVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
