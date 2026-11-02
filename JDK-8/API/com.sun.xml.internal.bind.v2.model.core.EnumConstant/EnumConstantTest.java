/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.core.EnumConstant conformance: interface hierarchy and members.
 * @run main EnumConstantTest
 */

public class EnumConstantTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.core.EnumConstant", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.core.EnumConstant"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getEnclosingClass"), "has method getEnclosingClass");
        check(mn.contains("getLexicalValue"), "has method getLexicalValue");
        check(mn.contains("getName"), "has method getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
