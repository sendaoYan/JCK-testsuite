/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.BCELifier conformance: reflection.
 * @run main BCELifierTest
 */

public class BCELifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.util.BCELifier", false, BCELifierTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("BCELifier"), "simple name BCELifier");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.EmptyVisitor"), "superclass com.sun.org.apache.bcel.internal.classfile.EmptyVisitor");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "start"), "method start");
        check(has(c, "visitJavaClass"), "method visitJavaClass");
        check(has(c, "visitField"), "method visitField");
        check(has(c, "visitMethod"), "method visitMethod");
        check(has(c, "_main"), "method _main");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
