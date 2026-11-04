/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.OctetBufferListener conformance: interface hierarchy and members.
 * @run main OctetBufferListenerTest
 */

public class OctetBufferListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.OctetBufferListener", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.OctetBufferListener"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("onBeforeOctetBufferOverwrite"), "has method onBeforeOctetBufferOverwrite");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
