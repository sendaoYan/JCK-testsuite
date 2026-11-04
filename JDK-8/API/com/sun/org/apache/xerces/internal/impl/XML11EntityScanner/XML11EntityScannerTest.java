/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XML11EntityScanner conformance: class hierarchy and members.
 * @run main XML11EntityScannerTest
 */
public class XML11EntityScannerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XML11EntityScanner", false, XML11EntityScannerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.XMLEntityScanner"), "superclass com.sun.org.apache.xerces.internal.impl.XMLEntityScanner");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("peekChar"), "method peekChar");
        check(mn.contains("scanChar"), "method scanChar");
        check(mn.contains("scanNmtoken"), "method scanNmtoken");
        check(mn.contains("scanName"), "method scanName");
        check(mn.contains("scanNCName"), "method scanNCName");
        check(mn.contains("scanQName"), "method scanQName");
        check(mn.contains("scanContent"), "method scanContent");
        check(mn.contains("scanLiteral"), "method scanLiteral");
        check(mn.contains("scanData"), "method scanData");
        check(mn.contains("skipChar"), "method skipChar");
        check(mn.contains("skipSpaces"), "method skipSpaces");
        check(mn.contains("skipString"), "method skipString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
