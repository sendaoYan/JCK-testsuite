/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.UtfHelpper conformance: class hierarchy and members.
 * @run main UtfHelpperTest
 */
public class UtfHelpperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.UtfHelpper", false, UtfHelpperTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("writeByte"), "method writeByte");
        check(mn.contains("writeCodePointToUtf8"), "method writeCodePointToUtf8");
        check(mn.contains("writeCharToUtf8"), "method writeCharToUtf8");
        check(mn.contains("writeStringToUtf8"), "method writeStringToUtf8");
        check(mn.contains("getStringInUtf8"), "method getStringInUtf8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
