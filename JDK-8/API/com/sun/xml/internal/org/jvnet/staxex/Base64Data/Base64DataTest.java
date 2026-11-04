/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.staxex.Base64Data conformance: class hierarchy and members.
 * @run main Base64DataTest
 */

public class Base64DataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.staxex.Base64Data", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.staxex.Base64Data"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("java.lang.CharSequence"), "has interface java.lang.CharSequence");
        check(ifs.contains("java.lang.Cloneable"), "has interface java.lang.Cloneable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("charAt"), "has method charAt");
        check(mn.contains("clone"), "has method clone");
        check(mn.contains("get"), "has method get");
        check(mn.contains("getDataHandler"), "has method getDataHandler");
        check(mn.contains("getDataLen"), "has method getDataLen");
        check(mn.contains("getExact"), "has method getExact");
        check(mn.contains("getInputStream"), "has method getInputStream");
        check(mn.contains("getMimeType"), "has method getMimeType");
        check(mn.contains("getProperty"), "has method getProperty");
        check(mn.contains("hasData"), "has method hasData");
        check(mn.contains("length"), "has method length");
        check(mn.contains("set"), "has method set");
        check(mn.contains("subSequence"), "has method subSequence");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("writeTo"), "has method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
