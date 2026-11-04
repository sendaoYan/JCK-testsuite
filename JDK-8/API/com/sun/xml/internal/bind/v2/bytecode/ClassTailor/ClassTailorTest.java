/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.bytecode.ClassTailor conformance: class hierarchy and members.
 * @run main ClassTailorTest
 */

public class ClassTailorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.bytecode.ClassTailor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.bytecode.ClassTailor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("tailor"), "has method tailor");
        check(mn.contains("toVMClassName"), "has method toVMClassName");
        check(mn.contains("toVMTypeName"), "has method toVMTypeName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
