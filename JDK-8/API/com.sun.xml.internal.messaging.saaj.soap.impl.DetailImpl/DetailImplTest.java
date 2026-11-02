/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.soap.impl.DetailImpl conformance: abstract class hierarchy and members.
 * @run main DetailImplTest
 */

public class DetailImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.soap.impl.DetailImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.soap.impl.DetailImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.messaging.saaj.soap.impl.FaultElementImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.messaging.saaj.soap.impl.FaultElementImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.soap.Detail"), "has interface javax.xml.soap.Detail");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addDetailEntry"), "has method addDetailEntry");
        check(mn.contains("addElement"), "has method addElement");
        check(mn.contains("convertToSoapElement"), "has method convertToSoapElement");
        check(mn.contains("createDetailEntry"), "has method createDetailEntry");
        check(mn.contains("getDetailEntries"), "has method getDetailEntries");
        check(mn.contains("isStandardFaultElement"), "has method isStandardFaultElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
