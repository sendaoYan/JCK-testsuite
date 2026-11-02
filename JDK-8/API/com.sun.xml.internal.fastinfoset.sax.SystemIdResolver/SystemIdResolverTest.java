/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.sax.SystemIdResolver conformance: class hierarchy and members.
 * @run main SystemIdResolverTest
 */

public class SystemIdResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.sax.SystemIdResolver", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.sax.SystemIdResolver"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAbsolutePathFromRelativePath"), "has method getAbsolutePathFromRelativePath");
        check(mn.contains("getAbsoluteURI"), "has method getAbsoluteURI");
        check(mn.contains("getAbsoluteURIFromRelative"), "has method getAbsoluteURIFromRelative");
        check(mn.contains("isAbsolutePath"), "has method isAbsolutePath");
        check(mn.contains("isAbsoluteURI"), "has method isAbsoluteURI");
        check(mn.contains("isWindowsAbsolutePath"), "has method isWindowsAbsolutePath");
        check(mn.contains("replaceChars"), "has method replaceChars");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
