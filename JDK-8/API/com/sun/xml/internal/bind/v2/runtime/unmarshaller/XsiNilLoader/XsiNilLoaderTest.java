/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader conformance: class hierarchy and members.
 * @run main XsiNilLoaderTest
 */

public class XsiNilLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.unmarshaller.ProxyLoader".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.unmarshaller.ProxyLoader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getExpectedAttributes"), "has method getExpectedAttributes");
        check(mn.contains("getExpectedChildElements"), "has method getExpectedChildElements");
        check(mn.contains("onNil"), "has method onNil");
        check(mn.contains("selectLoader"), "has method selectLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
