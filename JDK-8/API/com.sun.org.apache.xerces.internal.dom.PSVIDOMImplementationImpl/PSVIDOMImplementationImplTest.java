/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.dom.PSVIDOMImplementationImpl conformance: class hierarchy and members.
 * @run main PSVIDOMImplementationImplTest
 */
public class PSVIDOMImplementationImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.dom.PSVIDOMImplementationImpl", false, PSVIDOMImplementationImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl"), "superclass com.sun.org.apache.xerces.internal.dom.CoreDOMImplementationImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDOMImplementation"), "method getDOMImplementation");
        check(mn.contains("hasFeature"), "method hasFeature");
        check(mn.contains("createDocument"), "method createDocument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
