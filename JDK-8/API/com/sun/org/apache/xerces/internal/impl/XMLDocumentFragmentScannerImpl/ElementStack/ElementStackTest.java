/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.ElementStack conformance: class hierarchy and members.
 * @run main ElementStackTest
 */
public class ElementStackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl$ElementStack", false, ElementStackTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("pushElement"), "method pushElement");
        check(mn.contains("getNext"), "method getNext");
        check(mn.contains("push"), "method push");
        check(mn.contains("matchElement"), "method matchElement");
        check(mn.contains("nextElement"), "method nextElement");
        check(mn.contains("popElement"), "method popElement");
        check(mn.contains("reposition"), "method reposition");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("getLastPoppedElement"), "method getLastPoppedElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
