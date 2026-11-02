/*
 * @test
 * @summary sun.jvm.hotspot.oops.ConstMethod conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main ConstMethodTest
 */
import java.lang.reflect.*;

public class ConstMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.ConstMethod", false, ConstMethodTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.oops.Metadata".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getMethod"), "method getMethod");
        check(hasMethod(c, "getGenericSignature"), "method getGenericSignature");
        check(hasMethod(c, "getParameterAnnotations"), "method getParameterAnnotations");
        check(hasMethod(c, "getSignature"), "method getSignature");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getFlags"), "method getFlags");
        check(hasMethod(c, "getNameIndex"), "method getNameIndex");
        check(hasMethod(c, "getConstants"), "method getConstants");
        check(hasMethod(c, "getSignatureIndex"), "method getSignatureIndex");
        check(hasMethod(c, "getGenericSignatureIndex"), "method getGenericSignatureIndex");
        check(hasMethod(c, "iterateFields"), "method iterateFields");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "getCodeSize"), "method getCodeSize");
        check(hasMethod(c, "hasStackMapTable"), "method hasStackMapTable");
        check(hasMethod(c, "getStackMapData"), "method getStackMapData");
        check(hasMethod(c, "getMaxStack"), "method getMaxStack");
        check(hasMethod(c, "getMaxLocals"), "method getMaxLocals");
        check(hasMethod(c, "getSizeOfParameters"), "method getSizeOfParameters");
        check(hasMethod(c, "getBytecodeOrBPAt"), "method getBytecodeOrBPAt");
        check(hasMethod(c, "getBytecodeByteArg"), "method getBytecodeByteArg");
        check(hasMethod(c, "getBytecodeShortArg"), "method getBytecodeShortArg");
        check(hasMethod(c, "getNativeShortArg"), "method getNativeShortArg");
        check(hasMethod(c, "getBytecodeIntArg"), "method getBytecodeIntArg");
        check(hasMethod(c, "getNativeIntArg"), "method getNativeIntArg");
        check(hasMethod(c, "getByteCode"), "method getByteCode");
        check(hasMethod(c, "hasLineNumberTable"), "method hasLineNumberTable");
        check(hasMethod(c, "getLineNumberFromBCI"), "method getLineNumberFromBCI");
        check(hasMethod(c, "getLineNumberTable"), "method getLineNumberTable");
        check(hasMethod(c, "hasLocalVariableTable"), "method hasLocalVariableTable");
        check(hasMethod(c, "getLocalVariableTable"), "method getLocalVariableTable");
        check(hasMethod(c, "hasExceptionTable"), "method hasExceptionTable");
        check(hasMethod(c, "getExceptionTable"), "method getExceptionTable");
        check(hasMethod(c, "hasCheckedExceptions"), "method hasCheckedExceptions");
        check(hasMethod(c, "getCheckedExceptions"), "method getCheckedExceptions");
        check(hasMethod(c, "getMethodAnnotations"), "method getMethodAnnotations");
        check(hasMethod(c, "getTypeAnnotations"), "method getTypeAnnotations");
        check(hasMethod(c, "getDefaultAnnotations"), "method getDefaultAnnotations");
        check(hasMethod(c, "getLocalVariableName"), "method getLocalVariableName");
        check(hasMethod(c, "isAddressInMethod"), "method isAddressInMethod");
        check(hasMethod(c, "getIdNum"), "method getIdNum");
        check(hasMethod(c, "getConstMethodSize"), "method getConstMethodSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
