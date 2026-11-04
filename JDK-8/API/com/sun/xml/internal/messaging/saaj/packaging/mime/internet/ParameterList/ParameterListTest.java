/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParameterList conformance: class hierarchy and members.
 * @run main ParameterListTest
 */

public class ParameterListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParameterList", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParameterList"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("copy"), "has method copy");
        check(mn.contains("get"), "has method get");
        check(mn.contains("getNames"), "has method getNames");
        check(mn.contains("quote"), "has method quote");
        check(mn.contains("remove"), "has method remove");
        check(mn.contains("set"), "has method set");
        check(mn.contains("size"), "has method size");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
