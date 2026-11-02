/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.ConstantNameAndType conformance: reflection.
 * @run main ConstantNameAndTypeTest
 */

public class ConstantNameAndTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.ConstantNameAndType", false, ConstantNameAndTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ConstantNameAndType"), "simple name ConstantNameAndType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.Constant"), "superclass com.sun.org.apache.bcel.internal.classfile.Constant");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getNameIndex"), "method getNameIndex");
        check(has(c, "getName"), "method getName");
        check(has(c, "getSignatureIndex"), "method getSignatureIndex");
        check(has(c, "getSignature"), "method getSignature");
        check(has(c, "setNameIndex"), "method setNameIndex");
        check(has(c, "setSignatureIndex"), "method setSignatureIndex");
        check(has(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
