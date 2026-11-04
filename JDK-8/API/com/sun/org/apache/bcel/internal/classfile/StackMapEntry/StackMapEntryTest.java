/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.StackMapEntry conformance: reflection.
 * @run main StackMapEntryTest
 */

public class StackMapEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.StackMapEntry", false, StackMapEntryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("StackMapEntry"), "simple name StackMapEntry");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "dump"), "method dump");
        check(has(c, "toString"), "method toString");
        check(has(c, "setByteCodeOffset"), "method setByteCodeOffset");
        check(has(c, "getByteCodeOffset"), "method getByteCodeOffset");
        check(has(c, "setNumberOfLocals"), "method setNumberOfLocals");
        check(has(c, "getNumberOfLocals"), "method getNumberOfLocals");
        check(has(c, "setTypesOfLocals"), "method setTypesOfLocals");
        check(has(c, "getTypesOfLocals"), "method getTypesOfLocals");
        check(has(c, "setNumberOfStackItems"), "method setNumberOfStackItems");
        check(has(c, "getNumberOfStackItems"), "method getNumberOfStackItems");
        check(has(c, "setTypesOfStackItems"), "method setTypesOfStackItems");
        check(has(c, "getTypesOfStackItems"), "method getTypesOfStackItems");
        check(has(c, "copy"), "method copy");
        check(has(c, "accept"), "method accept");
        check(has(c, "getConstantPool"), "method getConstantPool");
        check(has(c, "setConstantPool"), "method setConstantPool");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
