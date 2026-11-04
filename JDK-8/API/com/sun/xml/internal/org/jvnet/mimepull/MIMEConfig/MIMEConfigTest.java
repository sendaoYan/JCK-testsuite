/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.mimepull.MIMEConfig conformance: class hierarchy and members.
 * @run main MIMEConfigTest
 */

public class MIMEConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.mimepull.MIMEConfig", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.mimepull.MIMEConfig"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getChunkSize"), "has method getChunkSize");
        check(mn.contains("getMemoryThreshold"), "has method getMemoryThreshold");
        check(mn.contains("getTempDir"), "has method getTempDir");
        check(mn.contains("getTempFilePrefix"), "has method getTempFilePrefix");
        check(mn.contains("getTempFileSuffix"), "has method getTempFileSuffix");
        check(mn.contains("isOnlyMemory"), "has method isOnlyMemory");
        check(mn.contains("isParseEagerly"), "has method isParseEagerly");
        check(mn.contains("setChunkSize"), "has method setChunkSize");
        check(mn.contains("setDir"), "has method setDir");
        check(mn.contains("setMemoryThreshold"), "has method setMemoryThreshold");
        check(mn.contains("setParseEagerly"), "has method setParseEagerly");
        check(mn.contains("validate"), "has method validate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
