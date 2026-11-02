/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.property.AttributeProperty conformance: class hierarchy and members.
 * @run main AttributePropertyTest
 */

public class AttributePropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.property.AttributeProperty", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.property.AttributeProperty"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.property.PropertyImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.property.PropertyImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("java.lang.Comparable"), "has interface java.lang.Comparable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("buildChildElementUnmarshallers"), "has method buildChildElementUnmarshallers");
        check(mn.contains("compareTo"), "has method compareTo");
        check(mn.contains("getElementPropertyAccessor"), "has method getElementPropertyAccessor");
        check(mn.contains("getFieldName"), "has method getFieldName");
        check(mn.contains("getIdValue"), "has method getIdValue");
        check(mn.contains("getInfo"), "has method getInfo");
        check(mn.contains("getKind"), "has method getKind");
        check(mn.contains("hasSerializeURIAction"), "has method hasSerializeURIAction");
        check(mn.contains("isHiddenByOverride"), "has method isHiddenByOverride");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("serializeAttributes"), "has method serializeAttributes");
        check(mn.contains("serializeBody"), "has method serializeBody");
        check(mn.contains("serializeURIs"), "has method serializeURIs");
        check(mn.contains("setHiddenByOverride"), "has method setHiddenByOverride");
        check(mn.contains("wrapUp"), "has method wrapUp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
