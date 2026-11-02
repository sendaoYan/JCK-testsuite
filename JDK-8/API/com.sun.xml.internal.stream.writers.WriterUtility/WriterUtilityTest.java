/*
 * @test
 * @summary com.sun.xml.internal.stream.writers.WriterUtility conformance: class hierarchy and members.
 * @run main WriterUtilityTest
 */

public class WriterUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.writers.WriterUtility", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.writers.WriterUtility"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDefaultEncoder"), "has method getDefaultEncoder");
        check(mn.contains("getEscapeCharacters"), "has method getEscapeCharacters");
        check(mn.contains("setEscapeCharacters"), "has method setEscapeCharacters");
        check(mn.contains("setWriter"), "has method setWriter");
        check(mn.contains("writeXMLAttributeValue"), "has method writeXMLAttributeValue");
        check(mn.contains("writeXMLContent"), "has method writeXMLContent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
