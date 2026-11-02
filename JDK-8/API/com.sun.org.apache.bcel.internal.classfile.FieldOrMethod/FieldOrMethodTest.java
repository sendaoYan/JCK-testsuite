/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.FieldOrMethod conformance: reflection.
 * @run main FieldOrMethodTest
 */

public class FieldOrMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.FieldOrMethod", false, FieldOrMethodTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FieldOrMethod"), "simple name FieldOrMethod");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "superclass com.sun.org.apache.bcel.internal.classfile.AccessFlags");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.classfile.Node"), "interface com.sun.org.apache.bcel.internal.classfile.Node");
        check(has(c, "dump"), "method dump");
        check(has(c, "getAttributes"), "method getAttributes");
        check(has(c, "setAttributes"), "method setAttributes");
        check(has(c, "getConstantPool"), "method getConstantPool");
        check(has(c, "setConstantPool"), "method setConstantPool");
        check(has(c, "getNameIndex"), "method getNameIndex");
        check(has(c, "setNameIndex"), "method setNameIndex");
        check(has(c, "getSignatureIndex"), "method getSignatureIndex");
        check(has(c, "setSignatureIndex"), "method setSignatureIndex");
        check(has(c, "getName"), "method getName");
        check(has(c, "getSignature"), "method getSignature");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
