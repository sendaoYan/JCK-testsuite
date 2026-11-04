/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.ConstantCP conformance: reflection.
 * @run main ConstantCPTest
 */

public class ConstantCPTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.ConstantCP", false, ConstantCPTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ConstantCP"), "simple name ConstantCP");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.Constant"), "superclass com.sun.org.apache.bcel.internal.classfile.Constant");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "dump"), "method dump");
        check(has(c, "getClassIndex"), "method getClassIndex");
        check(has(c, "getNameAndTypeIndex"), "method getNameAndTypeIndex");
        check(has(c, "setClassIndex"), "method setClassIndex");
        check(has(c, "getClass"), "method getClass");
        check(has(c, "setNameAndTypeIndex"), "method setNameAndTypeIndex");
        check(has(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
