/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.impl.ClassInfoImpl conformance: class hierarchy and members.
 * @run main ClassInfoImplTest
 */

public class ClassInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.impl.ClassInfoImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.impl.ClassInfoImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.model.impl.TypeInfoImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.model.impl.TypeInfoImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.ClassInfo"), "has interface com.sun.xml.internal.bind.v2.model.core.ClassInfo");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.core.Element"), "has interface com.sun.xml.internal.bind.v2.model.core.Element");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addProperty"), "has method addProperty");
        check(mn.contains("asElement"), "has method asElement");
        check(mn.contains("canBeReferencedByIDREF"), "has method canBeReferencedByIDREF");
        check(mn.contains("checkConflict"), "has method checkConflict");
        check(mn.contains("checkFieldXmlLocation"), "has method checkFieldXmlLocation");
        check(mn.contains("collectGetterSetters"), "has method collectGetterSetters");
        check(mn.contains("createAccessorSeed"), "has method createAccessorSeed");
        check(mn.contains("createAttributeProperty"), "has method createAttributeProperty");
        check(mn.contains("createElementProperty"), "has method createElementProperty");
        check(mn.contains("createFieldSeed"), "has method createFieldSeed");
        check(mn.contains("createMapProperty"), "has method createMapProperty");
        check(mn.contains("createReferenceProperty"), "has method createReferenceProperty");
        check(mn.contains("createValueProperty"), "has method createValueProperty");
        check(mn.contains("declaresAttributeWildcard"), "has method declaresAttributeWildcard");
        check(mn.contains("ensureNoAnnotation"), "has method ensureNoAnnotation");
        check(mn.contains("findFieldProperties"), "has method findFieldProperties");
        check(mn.contains("findGetterSetterProperties"), "has method findGetterSetterProperties");
        check(mn.contains("getAccessType"), "has method getAccessType");
        check(mn.contains("getAccessorOrder"), "has method getAccessorOrder");
        check(mn.contains("getBaseClass"), "has method getBaseClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
