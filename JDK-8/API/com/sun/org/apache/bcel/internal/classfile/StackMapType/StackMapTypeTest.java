/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.StackMapType conformance: reflection.
 * @run main StackMapTypeTest
 */

public class StackMapTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.StackMapType", false, StackMapTypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StackMapType"), "simple name StackMapType");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "setType"), "method setType");
        check(has(c, "getType"), "method getType");
        check(has(c, "setIndex"), "method setIndex");
        check(has(c, "getIndex"), "method getIndex");
        check(has(c, "dump"), "method dump");
        check(has(c, "hasIndex"), "method hasIndex");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        check(has(c, "getConstantPool"), "method getConstantPool");
        check(has(c, "setConstantPool"), "method setConstantPool");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
