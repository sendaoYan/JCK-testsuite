/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.Method conformance: reflection.
 * @run main MethodTest
 */

public class MethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.Method", false, MethodTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Method"), "simple name Method");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.FieldOrMethod"), "superclass com.sun.org.apache.bcel.internal.classfile.FieldOrMethod");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "accept"), "method accept");
        check(has(c, "getCode"), "method getCode");
        check(has(c, "getExceptionTable"), "method getExceptionTable");
        check(has(c, "getLocalVariableTable"), "method getLocalVariableTable");
        check(has(c, "getLineNumberTable"), "method getLineNumberTable");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        check(has(c, "getReturnType"), "method getReturnType");
        check(has(c, "getArgumentTypes"), "method getArgumentTypes");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
