/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.impl.ElementInfoImpl.PropertyImpl conformance: class hierarchy and members.
 * @run main PropertyImplTest
 */

public class PropertyImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.impl.ElementInfoImpl$PropertyImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.impl.ElementInfoImpl$PropertyImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is not public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.annotation.AnnotationSource"), "has interface com.sun.xml.internal.bind.v2.model.annotation.AnnotationSource");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.ElementPropertyInfo"), "has interface com.sun.xml.internal.bind.v2.model.core.ElementPropertyInfo");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.TypeRef"), "has interface com.sun.xml.internal.bind.v2.model.core.TypeRef");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("displayName"), "has method displayName");
        check(mn.contains("getAdapter"), "has method getAdapter");
        check(mn.contains("getDefaultValue"), "has method getDefaultValue");
        check(mn.contains("getExpectedMimeType"), "has method getExpectedMimeType");
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getSchemaType"), "has method getSchemaType");
        check(mn.contains("getSource"), "has method getSource");
        check(mn.contains("getTagName"), "has method getTagName");
        check(mn.contains("getTarget"), "has method getTarget");
        check(mn.contains("getTypes"), "has method getTypes");
        check(mn.contains("getXmlName"), "has method getXmlName");
        check(mn.contains("hasAnnotation"), "has method hasAnnotation");
        check(mn.contains("id"), "has method id");
        check(mn.contains("inlineBinaryData"), "has method inlineBinaryData");
        check(mn.contains("isCollection"), "has method isCollection");
        check(mn.contains("isCollectionNillable"), "has method isCollectionNillable");
        check(mn.contains("isCollectionRequired"), "has method isCollectionRequired");
        check(mn.contains("isNillable"), "has method isNillable");
        check(mn.contains("isRequired"), "has method isRequired");
        check(mn.contains("isValueList"), "has method isValueList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
