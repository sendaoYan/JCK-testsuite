/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11NMTOKENDatatypeValidator conformance: class hierarchy and members.
 * @run main XML11NMTOKENDatatypeValidatorTest
 */
public class XML11NMTOKENDatatypeValidatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.dtd.XML11NMTOKENDatatypeValidator", false, XML11NMTOKENDatatypeValidatorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dv.dtd.NMTOKENDatatypeValidator"), "superclass com.sun.org.apache.xerces.internal.impl.dv.dtd.NMTOKENDatatypeValidator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("validate"), "method validate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
