/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.ConstantInteger conformance: reflection.
 * @run main ConstantIntegerTest
 */

public class ConstantIntegerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.ConstantInteger", false, ConstantIntegerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ConstantInteger"), "simple name ConstantInteger");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.Constant"), "superclass com.sun.org.apache.bcel.internal.classfile.Constant");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.classfile.ConstantObject"), "interface com.sun.org.apache.bcel.internal.classfile.ConstantObject");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getBytes"), "method getBytes");
        check(has(c, "setBytes"), "method setBytes");
        check(has(c, "toString"), "method toString");
        check(has(c, "getConstantValue"), "method getConstantValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
