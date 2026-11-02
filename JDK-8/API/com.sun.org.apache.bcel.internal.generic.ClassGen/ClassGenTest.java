/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.ClassGen conformance: reflection.
 * @run main ClassGenTest
 */

public class ClassGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.ClassGen", false, ClassGenTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ClassGen"), "simple name ClassGen");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.classfile.AccessFlags"), "superclass com.sun.org.apache.bcel.internal.classfile.AccessFlags");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(has(c, "getJavaClass"), "method getJavaClass");
        check(has(c, "addInterface"), "method addInterface");
        check(has(c, "removeInterface"), "method removeInterface");
        check(has(c, "getMajor"), "method getMajor");
        check(has(c, "setMajor"), "method setMajor");
        check(has(c, "setMinor"), "method setMinor");
        check(has(c, "getMinor"), "method getMinor");
        check(has(c, "addAttribute"), "method addAttribute");
        check(has(c, "addMethod"), "method addMethod");
        check(has(c, "addEmptyConstructor"), "method addEmptyConstructor");
        check(has(c, "addField"), "method addField");
        check(has(c, "containsField"), "method containsField");
        check(has(c, "containsMethod"), "method containsMethod");
        check(has(c, "removeAttribute"), "method removeAttribute");
        check(has(c, "removeMethod"), "method removeMethod");
        check(has(c, "replaceMethod"), "method replaceMethod");
        check(has(c, "replaceField"), "method replaceField");
        check(has(c, "removeField"), "method removeField");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "getSuperclassName"), "method getSuperclassName");
        check(has(c, "getFileName"), "method getFileName");
        check(has(c, "setClassName"), "method setClassName");
        check(has(c, "setSuperclassName"), "method setSuperclassName");
        check(has(c, "getMethods"), "method getMethods");
        check(has(c, "setMethods"), "method setMethods");
        check(has(c, "setMethodAt"), "method setMethodAt");
        check(has(c, "getMethodAt"), "method getMethodAt");
        check(has(c, "getInterfaceNames"), "method getInterfaceNames");
        check(has(c, "getInterfaces"), "method getInterfaces");
        check(has(c, "getFields"), "method getFields");
        check(has(c, "getAttributes"), "method getAttributes");
        check(has(c, "getConstantPool"), "method getConstantPool");
        check(has(c, "setConstantPool"), "method setConstantPool");
        check(has(c, "setClassNameIndex"), "method setClassNameIndex");
        check(has(c, "setSuperclassNameIndex"), "method setSuperclassNameIndex");
        check(has(c, "getSuperclassNameIndex"), "method getSuperclassNameIndex");
        check(has(c, "getClassNameIndex"), "method getClassNameIndex");
        check(has(c, "addObserver"), "method addObserver");
        check(has(c, "removeObserver"), "method removeObserver");
        check(has(c, "update"), "method update");
        check(has(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
