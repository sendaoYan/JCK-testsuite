/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.InstructionList conformance: reflection.
 * @run main InstructionListTest
 */

public class InstructionListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.InstructionList", false, InstructionListTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("InstructionList"), "simple name InstructionList");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.io.Serializable"), "interface java.io.Serializable");
        check(has(c, "isEmpty"), "method isEmpty");
        check(has(c, "findHandle"), "method findHandle");
        check(has(c, "append"), "method append");
        check(has(c, "insert"), "method insert");
        check(has(c, "move"), "method move");
        check(has(c, "delete"), "method delete");
        check(has(c, "contains"), "method contains");
        check(has(c, "setPositions"), "method setPositions");
        check(has(c, "getByteCode"), "method getByteCode");
        check(has(c, "getInstructions"), "method getInstructions");
        check(has(c, "toString"), "method toString");
        check(has(c, "iterator"), "method iterator");
        check(has(c, "getInstructionHandles"), "method getInstructionHandles");
        check(has(c, "getInstructionPositions"), "method getInstructionPositions");
        check(has(c, "copy"), "method copy");
        check(has(c, "replaceConstantPool"), "method replaceConstantPool");
        check(has(c, "dispose"), "method dispose");
        check(has(c, "getStart"), "method getStart");
        check(has(c, "getEnd"), "method getEnd");
        check(has(c, "getLength"), "method getLength");
        check(has(c, "size"), "method size");
        check(has(c, "redirectBranches"), "method redirectBranches");
        check(has(c, "redirectLocalVariables"), "method redirectLocalVariables");
        check(has(c, "redirectExceptionHandlers"), "method redirectExceptionHandlers");
        check(has(c, "addObserver"), "method addObserver");
        check(has(c, "removeObserver"), "method removeObserver");
        check(has(c, "update"), "method update");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
