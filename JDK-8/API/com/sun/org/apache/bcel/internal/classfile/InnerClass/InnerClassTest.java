/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.InnerClass conformance: reflection.
 * @run main InnerClassTest
 */

public class InnerClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.InnerClass", false, InnerClassTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("InnerClass"), "simple name InnerClass");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.classfile.Node"), "interface com.sun.org.apache.bcel.internal.classfile.Node");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getInnerAccessFlags"), "method getInnerAccessFlags");
        check(has(c, "getInnerClassIndex"), "method getInnerClassIndex");
        check(has(c, "getInnerNameIndex"), "method getInnerNameIndex");
        check(has(c, "getOuterClassIndex"), "method getOuterClassIndex");
        check(has(c, "setInnerAccessFlags"), "method setInnerAccessFlags");
        check(has(c, "setInnerClassIndex"), "method setInnerClassIndex");
        check(has(c, "setInnerNameIndex"), "method setInnerNameIndex");
        check(has(c, "setOuterClassIndex"), "method setOuterClassIndex");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
