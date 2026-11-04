/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.StAXManager conformance: class hierarchy and members.
 * @run main StAXManagerTest
 */

public class StAXManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.StAXManager", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.StAXManager"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("checkProperty"), "has method checkProperty");
        check(mn.contains("containsProperty"), "has method containsProperty");
        check(mn.contains("getProperties"), "has method getProperties");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("initConfigurableReaderProperties"), "has method initConfigurableReaderProperties");
        check(mn.contains("initWriterProps"), "has method initWriterProps");
        check(mn.contains("setProperty"), "has method setProperty");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
