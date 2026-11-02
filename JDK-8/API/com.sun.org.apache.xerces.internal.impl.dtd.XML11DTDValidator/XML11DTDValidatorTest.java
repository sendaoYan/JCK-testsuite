/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDValidator conformance: class hierarchy and members.
 * @run main XML11DTDValidatorTest
 */
public class XML11DTDValidatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dtd.XML11DTDValidator", false, XML11DTDValidatorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator"), "superclass com.sun.org.apache.xerces.internal.impl.dtd.XMLDTDValidator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("reset"), "method reset");
        check(mn.contains("init"), "method init");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
