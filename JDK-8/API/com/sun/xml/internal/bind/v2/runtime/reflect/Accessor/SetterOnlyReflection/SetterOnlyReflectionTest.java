/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.reflect.Accessor.SetterOnlyReflection conformance: class hierarchy and members.
 * @run main SetterOnlyReflectionTest
 */

public class SetterOnlyReflectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.reflect.Accessor$SetterOnlyReflection", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.reflect.Accessor$SetterOnlyReflection"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.reflect.Accessor$GetterSetterReflection".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.reflect.Accessor$GetterSetterReflection");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("get"), "has method get");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
