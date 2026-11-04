/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.HTMLdtd conformance: class hierarchy and members.
 * @run main HTMLdtdTest
 */
public class HTMLdtdTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.HTMLdtd", false, HTMLdtdTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isEmptyTag"), "method isEmptyTag");
        check(mn.contains("isElementContent"), "method isElementContent");
        check(mn.contains("isPreserveSpace"), "method isPreserveSpace");
        check(mn.contains("isOptionalClosing"), "method isOptionalClosing");
        check(mn.contains("isOnlyOpening"), "method isOnlyOpening");
        check(mn.contains("isClosing"), "method isClosing");
        check(mn.contains("isURI"), "method isURI");
        check(mn.contains("isBoolean"), "method isBoolean");
        check(mn.contains("charFromName"), "method charFromName");
        check(mn.contains("fromChar"), "method fromChar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
