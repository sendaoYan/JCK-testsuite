/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.impl.RuntimeModelBuilder conformance: class hierarchy and members.
 * @run main RuntimeModelBuilderTest
 */

public class RuntimeModelBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.impl.RuntimeModelBuilder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.impl.RuntimeModelBuilder"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.model.impl.ModelBuilder".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.model.impl.ModelBuilder");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createArrayInfo"), "has method createArrayInfo");
        check(mn.contains("createClassInfo"), "has method createClassInfo");
        check(mn.contains("createElementInfo"), "has method createElementInfo");
        check(mn.contains("createEnumLeafInfo"), "has method createEnumLeafInfo");
        check(mn.contains("createTransducer"), "has method createTransducer");
        check(mn.contains("createTypeInfoSet"), "has method createTypeInfoSet");
        check(mn.contains("createXSSimpleType"), "has method createXSSimpleType");
        check(mn.contains("getClassInfo"), "has method getClassInfo");
        check(mn.contains("link"), "has method link");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
