/*
 * @test
 * @summary com.sun.xml.internal.bind.api.Bridge conformance: abstract class hierarchy and members.
 * @run main BridgeTest
 */

public class BridgeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.api.Bridge", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.api.Bridge"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("exit"), "has method exit");
        check(mn.contains("getContext"), "has method getContext");
        check(mn.contains("getTypeReference"), "has method getTypeReference");
        check(mn.contains("marshal"), "has method marshal");
        check(mn.contains("unmarshal"), "has method unmarshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
