/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.mimepull.MIMEMessage conformance: class hierarchy and members.
 * @run main MIMEMessageTest
 */

public class MIMEMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.mimepull.MIMEMessage", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.mimepull.MIMEMessage"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAttachments"), "has method getAttachments");
        check(mn.contains("getDecodedCidPart"), "has method getDecodedCidPart");
        check(mn.contains("getPart"), "has method getPart");
        check(mn.contains("makeProgress"), "has method makeProgress");
        check(mn.contains("parseAll"), "has method parseAll");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
