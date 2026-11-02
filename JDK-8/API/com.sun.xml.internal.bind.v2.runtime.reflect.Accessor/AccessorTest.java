/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.reflect.Accessor conformance: abstract class hierarchy and members.
 * @run main AccessorTest
 */

public class AccessorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.reflect.Accessor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.reflect.Accessor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.unmarshaller.Receiver"), "has interface com.sun.xml.internal.bind.v2.runtime.unmarshaller.Receiver");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("adapt"), "has method adapt");
        check(mn.contains("get"), "has method get");
        check(mn.contains("getErrorInstance"), "has method getErrorInstance");
        check(mn.contains("getUnadapted"), "has method getUnadapted");
        check(mn.contains("getValueType"), "has method getValueType");
        check(mn.contains("isAbstractable"), "has method isAbstractable");
        check(mn.contains("isAdapted"), "has method isAdapted");
        check(mn.contains("isValueTypeAbstractable"), "has method isValueTypeAbstractable");
        check(mn.contains("optimize"), "has method optimize");
        check(mn.contains("receive"), "has method receive");
        check(mn.contains("set"), "has method set");
        check(mn.contains("setUnadapted"), "has method setUnadapted");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
