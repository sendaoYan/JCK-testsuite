/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.InstructionFactory conformance: reflection.
 * @run main InstructionFactoryTest
 */

public class InstructionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.InstructionFactory", false, InstructionFactoryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("InstructionFactory"), "simple name InstructionFactory");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.generic.InstructionConstants"), "interface com.sun.org.apache.bcel.internal.generic.InstructionConstants");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "createInvoke"), "method createInvoke");
        check(has(c, "createPrintln"), "method createPrintln");
        check(has(c, "createConstant"), "method createConstant");
        check(has(c, "createAppend"), "method createAppend");
        check(has(c, "createFieldAccess"), "method createFieldAccess");
        check(has(c, "createThis"), "method createThis");
        check(has(c, "createReturn"), "method createReturn");
        check(has(c, "createBinaryOperation"), "method createBinaryOperation");
        check(has(c, "createPop"), "method createPop");
        check(has(c, "createDup"), "method createDup");
        check(has(c, "createDup_2"), "method createDup_2");
        check(has(c, "createDup_1"), "method createDup_1");
        check(has(c, "createStore"), "method createStore");
        check(has(c, "createLoad"), "method createLoad");
        check(has(c, "createArrayLoad"), "method createArrayLoad");
        check(has(c, "createArrayStore"), "method createArrayStore");
        check(has(c, "createCast"), "method createCast");
        check(has(c, "createGetField"), "method createGetField");
        check(has(c, "createGetStatic"), "method createGetStatic");
        check(has(c, "createPutField"), "method createPutField");
        check(has(c, "createPutStatic"), "method createPutStatic");
        check(has(c, "createCheckCast"), "method createCheckCast");
        check(has(c, "createInstanceOf"), "method createInstanceOf");
        check(has(c, "createNew"), "method createNew");
        check(has(c, "createNewArray"), "method createNewArray");
        check(has(c, "createNull"), "method createNull");
        check(has(c, "createBranchInstruction"), "method createBranchInstruction");
        check(has(c, "setClassGen"), "method setClassGen");
        check(has(c, "getClassGen"), "method getClassGen");
        check(has(c, "setConstantPool"), "method setConstantPool");
        check(has(c, "getConstantPool"), "method getConstantPool");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
