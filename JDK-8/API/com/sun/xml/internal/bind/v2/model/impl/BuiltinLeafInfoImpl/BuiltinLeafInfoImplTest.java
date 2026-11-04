/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.impl.BuiltinLeafInfoImpl conformance: class hierarchy and members.
 * @run main BuiltinLeafInfoImplTest
 */

public class BuiltinLeafInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.impl.BuiltinLeafInfoImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.impl.BuiltinLeafInfoImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.model.impl.LeafInfoImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.model.impl.LeafInfoImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.BuiltinLeafInfo"), "has interface com.sun.xml.internal.bind.v2.model.core.BuiltinLeafInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("asElement"), "has method asElement");
        check(mn.contains("createLeaves"), "has method createLeaves");
        check(mn.contains("getElementName"), "has method getElementName");
        check(mn.contains("getLocation"), "has method getLocation");
        check(mn.contains("getType"), "has method getType");
        check(mn.contains("getTypeName"), "has method getTypeName");
        check(mn.contains("getTypeNames"), "has method getTypeNames");
        check(mn.contains("getUpstream"), "has method getUpstream");
        check(mn.contains("isElement"), "has method isElement");
        check(mn.contains("isSimpleType"), "has method isSimpleType");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
