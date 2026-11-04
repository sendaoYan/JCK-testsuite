/*
 * @test
 * @summary com.sun.xml.internal.stream.events.AttributeImpl conformance: class hierarchy and members.
 * @run main AttributeImplTest
 */

public class AttributeImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.AttributeImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.AttributeImpl"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.events.DummyEvent".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.events.DummyEvent");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.Attribute"), "has interface javax.xml.stream.events.Attribute");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDTDType"), "has method getDTDType");
        check(mn.contains("getName"), "has method getName");
        check(mn.contains("getNonNormalizedValue"), "has method getNonNormalizedValue");
        check(mn.contains("getValue"), "has method getValue");
        check(mn.contains("init"), "has method init");
        check(mn.contains("isSpecified"), "has method isSpecified");
        check(mn.contains("setAttributeType"), "has method setAttributeType");
        check(mn.contains("setName"), "has method setName");
        check(mn.contains("setNonNormalizedValue"), "has method setNonNormalizedValue");
        check(mn.contains("setSpecified"), "has method setSpecified");
        check(mn.contains("setValue"), "has method setValue");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("writeAsEncodedUnicodeEx"), "has method writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
