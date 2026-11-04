/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl.JAXBContextBuilder conformance: class hierarchy and members.
 * @run main JAXBContextBuilderTest
 */

public class JAXBContextBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl$JAXBContextBuilder"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("build"), "has method build");
        check(mn.contains("setAllNillable"), "has method setAllNillable");
        check(mn.contains("setAnnotationReader"), "has method setAnnotationReader");
        check(mn.contains("setC14NSupport"), "has method setC14NSupport");
        check(mn.contains("setClasses"), "has method setClasses");
        check(mn.contains("setDefaultNsUri"), "has method setDefaultNsUri");
        check(mn.contains("setDisableSecurityProcessing"), "has method setDisableSecurityProcessing");
        check(mn.contains("setImprovedXsiTypeHandling"), "has method setImprovedXsiTypeHandling");
        check(mn.contains("setRetainPropertyInfo"), "has method setRetainPropertyInfo");
        check(mn.contains("setSubclassReplacements"), "has method setSubclassReplacements");
        check(mn.contains("setSupressAccessorWarnings"), "has method setSupressAccessorWarnings");
        check(mn.contains("setTypeRefs"), "has method setTypeRefs");
        check(mn.contains("setXmlAccessorFactorySupport"), "has method setXmlAccessorFactorySupport");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
