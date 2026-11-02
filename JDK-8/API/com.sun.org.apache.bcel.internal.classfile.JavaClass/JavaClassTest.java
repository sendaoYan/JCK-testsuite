/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.JavaClass conformance: reflection.
 * @run main JavaClassTest
 */

public class JavaClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.JavaClass", false, JavaClassTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("JavaClass"), "simple name JavaClass");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "superclass com.sun.org.apache.bcel.internal.classfile.AccessFlags");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.classfile.Node"), "interface com.sun.org.apache.bcel.internal.classfile.Node");
        check(has(c, "accept"), "method accept");
        check(has(c, "dump"), "method dump");
        check(has(c, "getBytes"), "method getBytes");
        check(has(c, "getAttributes"), "method getAttributes");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "getPackageName"), "method getPackageName");
        check(has(c, "getClassNameIndex"), "method getClassNameIndex");
        check(has(c, "getConstantPool"), "method getConstantPool");
        check(has(c, "getFields"), "method getFields");
        check(has(c, "getFileName"), "method getFileName");
        check(has(c, "getInterfaceNames"), "method getInterfaceNames");
        check(has(c, "getInterfaceIndices"), "method getInterfaceIndices");
        check(has(c, "getMajor"), "method getMajor");
        check(has(c, "getMethods"), "method getMethods");
        check(has(c, "getMethod"), "method getMethod");
        check(has(c, "getMinor"), "method getMinor");
        check(has(c, "getSourceFileName"), "method getSourceFileName");
        check(has(c, "getSuperclassName"), "method getSuperclassName");
        check(has(c, "getSuperclassNameIndex"), "method getSuperclassNameIndex");
        check(has(c, "setAttributes"), "method setAttributes");
        check(has(c, "setClassName"), "method setClassName");
        check(has(c, "setClassNameIndex"), "method setClassNameIndex");
        check(has(c, "setConstantPool"), "method setConstantPool");
        check(has(c, "setFields"), "method setFields");
        check(has(c, "setFileName"), "method setFileName");
        check(has(c, "setInterfaceNames"), "method setInterfaceNames");
        check(has(c, "setInterfaces"), "method setInterfaces");
        check(has(c, "setMajor"), "method setMajor");
        check(has(c, "setMethods"), "method setMethods");
        check(has(c, "setMinor"), "method setMinor");
        check(has(c, "setSourceFileName"), "method setSourceFileName");
        check(has(c, "setSuperclassName"), "method setSuperclassName");
        check(has(c, "setSuperclassNameIndex"), "method setSuperclassNameIndex");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        check(has(c, "isSuper"), "method isSuper");
        check(has(c, "isClass"), "method isClass");
        check(has(c, "getSource"), "method getSource");
        check(has(c, "getRepository"), "method getRepository");
        check(has(c, "setRepository"), "method setRepository");
        check(has(c, "instanceOf"), "method instanceOf");
        check(has(c, "implementationOf"), "method implementationOf");
        check(has(c, "getSuperClass"), "method getSuperClass");
        check(has(c, "getSuperClasses"), "method getSuperClasses");
        check(has(c, "getInterfaces"), "method getInterfaces");
        check(has(c, "getAllInterfaces"), "method getAllInterfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
