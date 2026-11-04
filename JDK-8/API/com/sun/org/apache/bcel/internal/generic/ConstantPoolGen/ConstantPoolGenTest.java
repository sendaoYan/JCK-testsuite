/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.ConstantPoolGen conformance: reflection.
 * @run main ConstantPoolGenTest
 */

public class ConstantPoolGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.ConstantPoolGen", false, ConstantPoolGenTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ConstantPoolGen"), "simple name ConstantPoolGen");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "lookupString"), "method lookupString");
        check(has(c, "addString"), "method addString");
        check(has(c, "lookupClass"), "method lookupClass");
        check(has(c, "addClass"), "method addClass");
        check(has(c, "addArrayClass"), "method addArrayClass");
        check(has(c, "lookupInteger"), "method lookupInteger");
        check(has(c, "addInteger"), "method addInteger");
        check(has(c, "lookupFloat"), "method lookupFloat");
        check(has(c, "addFloat"), "method addFloat");
        check(has(c, "lookupUtf8"), "method lookupUtf8");
        check(has(c, "addUtf8"), "method addUtf8");
        check(has(c, "lookupLong"), "method lookupLong");
        check(has(c, "addLong"), "method addLong");
        check(has(c, "lookupDouble"), "method lookupDouble");
        check(has(c, "addDouble"), "method addDouble");
        check(has(c, "lookupNameAndType"), "method lookupNameAndType");
        check(has(c, "addNameAndType"), "method addNameAndType");
        check(has(c, "lookupMethodref"), "method lookupMethodref");
        check(has(c, "addMethodref"), "method addMethodref");
        check(has(c, "lookupInterfaceMethodref"), "method lookupInterfaceMethodref");
        check(has(c, "addInterfaceMethodref"), "method addInterfaceMethodref");
        check(has(c, "lookupFieldref"), "method lookupFieldref");
        check(has(c, "addFieldref"), "method addFieldref");
        check(has(c, "getConstant"), "method getConstant");
        check(has(c, "setConstant"), "method setConstant");
        check(has(c, "getConstantPool"), "method getConstantPool");
        check(has(c, "getSize"), "method getSize");
        check(has(c, "getFinalConstantPool"), "method getFinalConstantPool");
        check(has(c, "toString"), "method toString");
        check(has(c, "addConstant"), "method addConstant");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
