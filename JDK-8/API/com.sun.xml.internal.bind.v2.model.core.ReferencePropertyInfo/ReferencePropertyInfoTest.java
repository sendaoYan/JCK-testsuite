/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.core.ReferencePropertyInfo conformance: interface hierarchy and members.
 * @run main ReferencePropertyInfoTest
 */

public class ReferencePropertyInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.core.ReferencePropertyInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.core.ReferencePropertyInfo"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.PropertyInfo"), "has interface com.sun.xml.internal.bind.v2.model.core.PropertyInfo");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAdapter"), "has method getAdapter");
        check(mn.contains("getDOMHandler"), "has method getDOMHandler");
        check(mn.contains("getElements"), "has method getElements");
        check(mn.contains("getWildcard"), "has method getWildcard");
        check(mn.contains("getXmlName"), "has method getXmlName");
        check(mn.contains("isCollectionNillable"), "has method isCollectionNillable");
        check(mn.contains("isCollectionRequired"), "has method isCollectionRequired");
        check(mn.contains("isMixed"), "has method isMixed");
        check(mn.contains("isRequired"), "has method isRequired");
        check(mn.contains("ref"), "has method ref");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
