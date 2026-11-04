/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.StackMap conformance: reflection.
 * @run main StackMapTest
 */

public class StackMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.StackMap", false, StackMapTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StackMap"), "simple name StackMap");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.Attribute"), "superclass com.sun.org.apache.bcel.internal.classfile.Attribute");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.classfile.Node"), "interface com.sun.org.apache.bcel.internal.classfile.Node");
        check(has(c, "dump"), "method dump");
        check(has(c, "getStackMap"), "method getStackMap");
        check(has(c, "setStackMap"), "method setStackMap");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        check(has(c, "accept"), "method accept");
        check(has(c, "getMapLength"), "method getMapLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
