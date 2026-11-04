/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.Synthetic conformance: reflection.
 * @run main SyntheticTest
 */

public class SyntheticTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.Synthetic", false, SyntheticTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Synthetic"), "simple name Synthetic");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.Attribute"), "superclass com.sun.org.apache.bcel.internal.classfile.Attribute");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getBytes"), "method getBytes");
        check(has(c, "setBytes"), "method setBytes");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
