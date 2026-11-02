/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.util.ValueArray conformance: abstract class hierarchy and members.
 * @run main ValueArrayTest
 */

public class ValueArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.util.ValueArray", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.util.ValueArray"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clear"), "has method clear");
        check(mn.contains("getMaximumCapacity"), "has method getMaximumCapacity");
        check(mn.contains("getSize"), "has method getSize");
        check(mn.contains("setMaximumCapacity"), "has method setMaximumCapacity");
        check(mn.contains("setReadOnlyArray"), "has method setReadOnlyArray");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
