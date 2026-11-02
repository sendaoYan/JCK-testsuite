/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen conformance: reflection.
 * @run main FieldGenOrMethodGenTest
 */

public class FieldGenOrMethodGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen", false, FieldGenOrMethodGenTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("FieldGenOrMethodGen"), "simple name FieldGenOrMethodGen");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "superclass com.sun.org.apache.bcel.internal.classfile.AccessFlags");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.NamedAndTyped"), "interface com.sun.org.apache.bcel.internal.generic.NamedAndTyped");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "setType"), "method setType");
        check(has(c, "getType"), "method getType");
        check(has(c, "getName"), "method getName");
        check(has(c, "setName"), "method setName");
        check(has(c, "getConstantPool"), "method getConstantPool");
        check(has(c, "setConstantPool"), "method setConstantPool");
        check(has(c, "addAttribute"), "method addAttribute");
        check(has(c, "removeAttribute"), "method removeAttribute");
        check(has(c, "removeAttributes"), "method removeAttributes");
        check(has(c, "getAttributes"), "method getAttributes");
        check(has(c, "getSignature"), "method getSignature");
        check(has(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
