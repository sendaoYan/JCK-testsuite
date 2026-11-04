/*
 * @test
 * @summary sun.jvm.hotspot.oops.Method conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main MethodTest
 */
import java.lang.reflect.*;

public class MethodTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.Method", false, MethodTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.oops.Metadata".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "isAbstract"), "method isAbstract");
        check(hasMethod(c, "isStatic"), "method isStatic");
        check(hasMethod(c, "isFinal"), "method isFinal");
        check(hasMethod(c, "isMethod"), "method isMethod");
        check(hasMethod(c, "isConstructor"), "method isConstructor");
        check(hasMethod(c, "isPublic"), "method isPublic");
        check(hasMethod(c, "isSynthetic"), "method isSynthetic");
        check(hasMethod(c, "getAnnotations"), "method getAnnotations");
        check(hasMethod(c, "isProtected"), "method isProtected");
        check(hasMethod(c, "getGenericSignature"), "method getGenericSignature");
        check(hasMethod(c, "isVarArgs"), "method isVarArgs");
        check(hasMethod(c, "getParameterAnnotations"), "method getParameterAnnotations");
        check(hasMethod(c, "isSynchronized"), "method isSynchronized");
        check(hasMethod(c, "isNative"), "method isNative");
        check(hasMethod(c, "isStrict"), "method isStrict");
        check(hasMethod(c, "isBridge"), "method isBridge");
        check(hasMethod(c, "getSignature"), "method getSignature");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "isPrivate"), "method isPrivate");
        check(hasMethod(c, "getAccessFlags"), "method getAccessFlags");
        check(hasMethod(c, "getAccessFlagsObj"), "method getAccessFlagsObj");
        check(hasMethod(c, "getNameIndex"), "method getNameIndex");
        check(hasMethod(c, "getConstants"), "method getConstants");
        check(hasMethod(c, "getSignatureIndex"), "method getSignatureIndex");
        check(hasMethod(c, "getGenericSignatureIndex"), "method getGenericSignatureIndex");
        check(hasMethod(c, "iterateFields"), "method iterateFields");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "getMethodHolder"), "method getMethodHolder");
        check(hasMethod(c, "getCodeSize"), "method getCodeSize");
        check(hasMethod(c, "isPackagePrivate"), "method isPackagePrivate");
        check(hasMethod(c, "getConstMethod"), "method getConstMethod");
        check(hasMethod(c, "hasStackMapTable"), "method hasStackMapTable");
        check(hasMethod(c, "getStackMapData"), "method getStackMapData");
        check(hasMethod(c, "getMaxStack"), "method getMaxStack");
        check(hasMethod(c, "getMaxLocals"), "method getMaxLocals");
        check(hasMethod(c, "getSizeOfParameters"), "method getSizeOfParameters");
        check(hasMethod(c, "getMethodCounters"), "method getMethodCounters");
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
        check(hasMethod(c, "interpreterThrowoutCount"), "method interpreterThrowoutCount");
        check(hasMethod(c, "getInvocationCount"), "method getInvocationCount");
        check(hasMethod(c, "getTypeAnnotations"), "method getTypeAnnotations");
        check(hasMethod(c, "getVtableIndex"), "method getVtableIndex");
        check(hasMethod(c, "getBackedgeCount"), "method getBackedgeCount");
        check(hasMethod(c, "getNativeMethod"), "method getNativeMethod");
        check(hasMethod(c, "getOrigBytecodeAt"), "method getOrigBytecodeAt");
        check(hasMethod(c, "isStaticInitializer"), "method isStaticInitializer");
        check(hasMethod(c, "getMaskFor"), "method getMaskFor");
        check(hasMethod(c, "getLocalVariableName"), "method getLocalVariableName");
        check(hasMethod(c, "externalNameAndSignature"), "method externalNameAndSignature");
        check(hasMethod(c, "interpreterInvocationCount"), "method interpreterInvocationCount");
        check(hasMethod(c, "nameAsAscii"), "method nameAsAscii");
        check(hasMethod(c, "getAnnotationDefault"), "method getAnnotationDefault");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
