/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.LocalVariable conformance: reflection.
 * @run main LocalVariableTest
 */

public class LocalVariableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.LocalVariable", false, LocalVariableTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("LocalVariable"), "simple name LocalVariable");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 4, "interfaces count 4");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.Constants"), "interface com.sun.org.apache.bcel.internal.Constants");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.classfile.Node"), "interface com.sun.org.apache.bcel.internal.classfile.Node");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getConstantPool"), "method getConstantPool");
        check(has(c, "getLength"), "method getLength");
        check(has(c, "getName"), "method getName");
        check(has(c, "getNameIndex"), "method getNameIndex");
        check(has(c, "getSignature"), "method getSignature");
        check(has(c, "getSignatureIndex"), "method getSignatureIndex");
        check(has(c, "getIndex"), "method getIndex");
        check(has(c, "getStartPC"), "method getStartPC");
        check(has(c, "setConstantPool"), "method setConstantPool");
        check(has(c, "setLength"), "method setLength");
        check(has(c, "setNameIndex"), "method setNameIndex");
        check(has(c, "setSignatureIndex"), "method setSignatureIndex");
        check(has(c, "setIndex"), "method setIndex");
        check(has(c, "setStartPC"), "method setStartPC");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
