/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl conformance: abstract class hierarchy and members.
 * @run main RuntimeBuiltinLeafInfoImplTest
 */

public class RuntimeBuiltinLeafInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("com.sun.xml.internal.bind.v2.model.impl.BuiltinLeafInfoImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.model.impl.BuiltinLeafInfoImpl");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.runtime.RuntimeBuiltinLeafInfo"), "has interface com.sun.xml.internal.bind.v2.model.runtime.RuntimeBuiltinLeafInfo");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.Transducer"), "has interface com.sun.xml.internal.bind.v2.runtime.Transducer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("checkXmlGregorianCalendarFieldRef"), "has method checkXmlGregorianCalendarFieldRef");
        check(mn.contains("createXS"), "has method createXS");
        check(mn.contains("declareNamespace"), "has method declareNamespace");
        check(mn.contains("decodeBase64"), "has method decodeBase64");
        check(mn.contains("getClazz"), "has method getClazz");
        check(mn.contains("getTransducer"), "has method getTransducer");
        check(mn.contains("getTypeName"), "has method getTypeName");
        check(mn.contains("isDefault"), "has method isDefault");
        check(mn.contains("useNamespace"), "has method useNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
