/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl conformance: class hierarchy and members.
 * @run main DatatypeFactoryImplTest
 */
public class DatatypeFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl", false, DatatypeFactoryImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("javax.xml.datatype.DatatypeFactory"), "superclass javax.xml.datatype.DatatypeFactory");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("newDuration"), "method newDuration");
        check(mn.contains("newDurationYearMonth"), "method newDurationYearMonth");
        check(mn.contains("newDurationDayTime"), "method newDurationDayTime");
        check(mn.contains("newXMLGregorianCalendar"), "method newXMLGregorianCalendar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
