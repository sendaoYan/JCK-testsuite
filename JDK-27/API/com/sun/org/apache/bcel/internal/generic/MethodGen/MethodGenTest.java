/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.generic.MethodGen conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.generic:+open
 * @run main MethodGenTest
 */
public class MethodGenTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.generic.MethodGen", false, MethodGenTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.generic.MethodGen"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.FieldGenOrMethodGen"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 0, "field-count");
        check(md.size() == 60, "method-count");
        check(md.contains("addAnnotationsAsAttribute"), "method-addAnnotationsAsAttribute");
        check(md.contains("addCodeAttribute"), "method-addCodeAttribute");
        check(md.contains("addException"), "method-addException");
        check(md.contains("addExceptionHandler"), "method-addExceptionHandler");
        check(md.contains("addLineNumber"), "method-addLineNumber");
        check(md.contains("addLocalVariable"), "method-addLocalVariable");
        check(md.contains("addObserver"), "method-addObserver");
        check(md.contains("addParameterAnnotation"), "method-addParameterAnnotation");
        check(md.contains("addParameterAnnotationsAsAttribute"), "method-addParameterAnnotationsAsAttribute");
        check(md.contains("copy"), "method-copy");
        check(md.contains("equals"), "method-equals");
        check(md.contains("getAnnotationsOnParameter"), "method-getAnnotationsOnParameter");
        check(md.contains("getArgumentName"), "method-getArgumentName");
        check(md.contains("getArgumentNames"), "method-getArgumentNames");
        check(md.contains("getArgumentType"), "method-getArgumentType");
        check(md.contains("getArgumentTypes"), "method-getArgumentTypes");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("getCodeAttributes"), "method-getCodeAttributes");
        check(md.contains("getComparator"), "method-getComparator");
        check(md.contains("getExceptionHandlers"), "method-getExceptionHandlers");
        check(md.contains("getExceptions"), "method-getExceptions");
        check(md.contains("getInstructionList"), "method-getInstructionList");
        check(md.contains("getLineNumberTable"), "method-getLineNumberTable");
        check(md.contains("getLineNumbers"), "method-getLineNumbers");
        check(md.contains("getLocalVariableTable"), "method-getLocalVariableTable");
        check(md.contains("getLocalVariableTypeTable"), "method-getLocalVariableTypeTable");
        check(md.contains("getLocalVariables"), "method-getLocalVariables");
        check(md.contains("getMaxLocals"), "method-getMaxLocals");
        check(md.contains("getMaxStack"), "method-getMaxStack");
        check(md.contains("getMethod"), "method-getMethod");
        check(md.contains("getReturnType"), "method-getReturnType");
        check(md.contains("getSignature"), "method-getSignature");
        check(md.contains("hashCode"), "method-hashCode");
        check(md.contains("removeCodeAttribute"), "method-removeCodeAttribute");
        check(md.contains("removeCodeAttributes"), "method-removeCodeAttributes");
        check(md.contains("removeException"), "method-removeException");
        check(md.contains("removeExceptionHandler"), "method-removeExceptionHandler");
        check(md.contains("removeExceptionHandlers"), "method-removeExceptionHandlers");
        check(md.contains("removeExceptions"), "method-removeExceptions");
        check(md.contains("removeLineNumber"), "method-removeLineNumber");
        check(md.contains("removeLineNumbers"), "method-removeLineNumbers");
        check(md.contains("removeLocalVariable"), "method-removeLocalVariable");
        check(md.contains("removeLocalVariableTypeTable"), "method-removeLocalVariableTypeTable");
        check(md.contains("removeLocalVariables"), "method-removeLocalVariables");
        check(md.contains("removeNOPs"), "method-removeNOPs");
        check(md.contains("removeObserver"), "method-removeObserver");
        check(md.contains("removeRuntimeAttributes"), "method-removeRuntimeAttributes");
        check(md.contains("setArgumentName"), "method-setArgumentName");
        check(md.contains("setArgumentNames"), "method-setArgumentNames");
        check(md.contains("setArgumentType"), "method-setArgumentType");
        check(md.contains("setArgumentTypes"), "method-setArgumentTypes");
        check(md.contains("setClassName"), "method-setClassName");
        check(md.contains("setComparator"), "method-setComparator");
        check(md.contains("setInstructionList"), "method-setInstructionList");
        check(md.contains("setMaxLocals"), "method-setMaxLocals");
        check(md.contains("setMaxStack"), "method-setMaxStack");
        check(md.contains("setReturnType"), "method-setReturnType");
        check(md.contains("stripAttributes"), "method-stripAttributes");
        check(md.contains("toString"), "method-toString");
        check(md.contains("update"), "method-update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
