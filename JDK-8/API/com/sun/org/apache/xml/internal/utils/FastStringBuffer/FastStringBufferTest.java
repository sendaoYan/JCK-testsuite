/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.FastStringBuffer conformance: class hierarchy and members.
 * @run main FastStringBufferTest
 */
public class FastStringBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.FastStringBuffer", false, FastStringBufferTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("size"), "method size");
        check(mn.contains("length"), "method length");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("setLength"), "method setLength");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("append"), "method append");
        check(mn.contains("isWhitespace"), "method isWhitespace");
        check(mn.contains("getString"), "method getString");
        check(mn.contains("getOneChunkString"), "method getOneChunkString");
        check(mn.contains("charAt"), "method charAt");
        check(mn.contains("sendSAXcharacters"), "method sendSAXcharacters");
        check(mn.contains("sendNormalizedSAXcharacters"), "method sendNormalizedSAXcharacters");
        check(mn.contains("sendSAXComment"), "method sendSAXComment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
