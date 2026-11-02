/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.tools.TransformInputOutput conformance: abstract class hierarchy and members.
 * @run main TransformInputOutputTest
 */

public class TransformInputOutputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.tools.TransformInputOutput", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.tools.TransformInputOutput"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("convertToNewWorkingDirectory"), "has method convertToNewWorkingDirectory");
        check(mn.contains("createRelativePathResolver"), "has method createRelativePathResolver");
        check(mn.contains("parse"), "has method parse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
