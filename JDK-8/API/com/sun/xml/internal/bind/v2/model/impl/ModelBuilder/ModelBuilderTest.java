/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.impl.ModelBuilder conformance: class hierarchy and members.
 * @run main ModelBuilderTest
 */

public class ModelBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.impl.ModelBuilder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.impl.ModelBuilder"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.impl.ModelBuilderI"), "has interface com.sun.xml.internal.bind.v2.model.impl.ModelBuilderI");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addRegistry"), "has method addRegistry");
        check(mn.contains("addToRegistry"), "has method addToRegistry");
        check(mn.contains("addTypeName"), "has method addTypeName");
        check(mn.contains("createArrayInfo"), "has method createArrayInfo");
        check(mn.contains("createClassInfo"), "has method createClassInfo");
        check(mn.contains("createElementInfo"), "has method createElementInfo");
        check(mn.contains("createEnumLeafInfo"), "has method createEnumLeafInfo");
        check(mn.contains("createTypeInfoSet"), "has method createTypeInfoSet");
        check(mn.contains("getClassInfo"), "has method getClassInfo");
        check(mn.contains("getNavigator"), "has method getNavigator");
        check(mn.contains("getParametrizedTypes"), "has method getParametrizedTypes");
        check(mn.contains("getReader"), "has method getReader");
        check(mn.contains("getRegistry"), "has method getRegistry");
        check(mn.contains("getTypeInfo"), "has method getTypeInfo");
        check(mn.contains("isReplaced"), "has method isReplaced");
        check(mn.contains("link"), "has method link");
        check(mn.contains("reportError"), "has method reportError");
        check(mn.contains("setErrorHandler"), "has method setErrorHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
