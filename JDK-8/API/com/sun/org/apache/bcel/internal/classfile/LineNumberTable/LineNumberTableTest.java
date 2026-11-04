/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.LineNumberTable conformance: reflection.
 * @run main LineNumberTableTest
 */

public class LineNumberTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.LineNumberTable", false, LineNumberTableTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LineNumberTable"), "simple name LineNumberTable");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.Attribute"), "superclass com.sun.org.apache.bcel.internal.classfile.Attribute");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getLineNumberTable"), "method getLineNumberTable");
        check(has(c, "setLineNumberTable"), "method setLineNumberTable");
        check(has(c, "toString"), "method toString");
        check(has(c, "getSourceLine"), "method getSourceLine");
        check(has(c, "copy"), "method copy");
        check(has(c, "getTableLength"), "method getTableLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
