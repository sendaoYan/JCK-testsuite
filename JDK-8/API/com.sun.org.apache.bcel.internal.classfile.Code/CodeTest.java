/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.Code conformance: reflection.
 * @run main CodeTest
 */

public class CodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.Code", false, CodeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Code"), "simple name Code");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.Attribute"), "superclass com.sun.org.apache.bcel.internal.classfile.Attribute");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getAttributes"), "method getAttributes");
        check(has(c, "getLineNumberTable"), "method getLineNumberTable");
        check(has(c, "getLocalVariableTable"), "method getLocalVariableTable");
        check(has(c, "getCode"), "method getCode");
        check(has(c, "getExceptionTable"), "method getExceptionTable");
        check(has(c, "getMaxLocals"), "method getMaxLocals");
        check(has(c, "getMaxStack"), "method getMaxStack");
        check(has(c, "setAttributes"), "method setAttributes");
        check(has(c, "setCode"), "method setCode");
        check(has(c, "setExceptionTable"), "method setExceptionTable");
        check(has(c, "setMaxLocals"), "method setMaxLocals");
        check(has(c, "setMaxStack"), "method setMaxStack");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
