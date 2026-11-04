/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.IntArrayData conformance: class hierarchy and members.
 * @run main IntArrayDataTest
 */

public class IntArrayDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.IntArrayData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.IntArrayData"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.output.Pcdata".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.output.Pcdata");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("charAt"), "has method charAt");
        check(mn.contains("getLiteral"), "has method getLiteral");
        check(mn.contains("length"), "has method length");
        check(mn.contains("set"), "has method set");
        check(mn.contains("subSequence"), "has method subSequence");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("writeTo"), "has method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
