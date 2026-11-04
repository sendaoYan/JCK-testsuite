/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.FragmentContentDriver conformance: class hierarchy and members.
 * @run main FragmentContentDriverTest
 */
public class FragmentContentDriverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl$FragmentContentDriver", false, FragmentContentDriverTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl$Driver"), "has interface com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl$Driver");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("decideSubState"), "method decideSubState");
        check(mn.contains("next"), "method next");
        check(mn.contains("scanForDoctypeHook"), "method scanForDoctypeHook");
        check(mn.contains("elementDepthIsZeroHook"), "method elementDepthIsZeroHook");
        check(mn.contains("scanRootElementHook"), "method scanRootElementHook");
        check(mn.contains("endOfFileHook"), "method endOfFileHook");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
