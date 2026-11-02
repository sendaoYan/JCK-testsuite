/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.MethodGen conformance: reflection.
 * @run main MethodGenTest
 */

public class MethodGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.MethodGen", false, MethodGenTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("MethodGen"), "simple name MethodGen");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen"), "superclass com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "addLocalVariable"), "method addLocalVariable");
        check(has(c, "removeLocalVariable"), "method removeLocalVariable");
        check(has(c, "removeLocalVariables"), "method removeLocalVariables");
        check(has(c, "getLocalVariables"), "method getLocalVariables");
        check(has(c, "getLocalVariableTable"), "method getLocalVariableTable");
        check(has(c, "getLocalVariableTypeTable"), "method getLocalVariableTypeTable");
        check(has(c, "addLineNumber"), "method addLineNumber");
        check(has(c, "removeLineNumber"), "method removeLineNumber");
        check(has(c, "removeLineNumbers"), "method removeLineNumbers");
        check(has(c, "getLineNumbers"), "method getLineNumbers");
        check(has(c, "getLineNumberTable"), "method getLineNumberTable");
        check(has(c, "addExceptionHandler"), "method addExceptionHandler");
        check(has(c, "removeExceptionHandler"), "method removeExceptionHandler");
        check(has(c, "removeExceptionHandlers"), "method removeExceptionHandlers");
        check(has(c, "getExceptionHandlers"), "method getExceptionHandlers");
        check(has(c, "addException"), "method addException");
        check(has(c, "removeException"), "method removeException");
        check(has(c, "removeExceptions"), "method removeExceptions");
        check(has(c, "getExceptions"), "method getExceptions");
        check(has(c, "addCodeAttribute"), "method addCodeAttribute");
        check(has(c, "removeCodeAttribute"), "method removeCodeAttribute");
        check(has(c, "removeCodeAttributes"), "method removeCodeAttributes");
        check(has(c, "getCodeAttributes"), "method getCodeAttributes");
        check(has(c, "getMethod"), "method getMethod");
        check(has(c, "removeNOPs"), "method removeNOPs");
        check(has(c, "setMaxLocals"), "method setMaxLocals");
        check(has(c, "getMaxLocals"), "method getMaxLocals");
        check(has(c, "setMaxStack"), "method setMaxStack");
        check(has(c, "getMaxStack"), "method getMaxStack");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "setClassName"), "method setClassName");
        check(has(c, "setReturnType"), "method setReturnType");
        check(has(c, "getReturnType"), "method getReturnType");
        check(has(c, "setArgumentTypes"), "method setArgumentTypes");
        check(has(c, "getArgumentTypes"), "method getArgumentTypes");
        check(has(c, "setArgumentType"), "method setArgumentType");
        check(has(c, "getArgumentType"), "method getArgumentType");
        check(has(c, "setArgumentNames"), "method setArgumentNames");
        check(has(c, "getArgumentNames"), "method getArgumentNames");
        check(has(c, "setArgumentName"), "method setArgumentName");
        check(has(c, "getArgumentName"), "method getArgumentName");
        check(has(c, "getInstructionList"), "method getInstructionList");
        check(has(c, "setInstructionList"), "method setInstructionList");
        check(has(c, "getSignature"), "method getSignature");
        check(has(c, "stripAttributes"), "method stripAttributes");
        check(has(c, "addObserver"), "method addObserver");
        check(has(c, "removeObserver"), "method removeObserver");
        check(has(c, "update"), "method update");
        check(has(c, "toString"), "method toString");
        check(has(c, "copy"), "method copy");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
