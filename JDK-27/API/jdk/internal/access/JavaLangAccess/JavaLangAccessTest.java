/*
 * @test
 * @summary jdk.internal.access.JavaLangAccess conformance: internal package reflection.
 * @modules java.base/jdk.internal.access:+open
 * @run main JavaLangAccessTest
 */
public class JavaLangAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.access.JavaLangAccess", false, JavaLangAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addEnableFinalMutationToAllUnnamed"), "method addEnableFinalMutationToAllUnnamed");
        check(hasMethod(c, "addEnableNativeAccess"), "method addEnableNativeAccess");
        check(hasMethod(c, "addEnableNativeAccessToAllUnnamed"), "method addEnableNativeAccessToAllUnnamed");
        check(hasMethod(c, "addExports"), "method addExports");
        check(hasMethod(c, "addExportsToAllUnnamed"), "method addExportsToAllUnnamed");
        check(hasMethod(c, "addOpens"), "method addOpens");
        check(hasMethod(c, "addOpensToAllUnnamed"), "method addOpensToAllUnnamed");
        check(hasMethod(c, "addReads"), "method addReads");
        check(hasMethod(c, "addReadsAllUnnamed"), "method addReadsAllUnnamed");
        check(hasMethod(c, "addUses"), "method addUses");
        check(hasMethod(c, "bindToLoader"), "method bindToLoader");
        check(hasMethod(c, "blockedOn"), "method blockedOn");
        check(hasMethod(c, "bytesCompatible"), "method bytesCompatible");
        check(hasMethod(c, "casAnnotationType"), "method casAnnotationType");
        check(hasMethod(c, "classData"), "method classData");
        check(hasMethod(c, "classFileVersion"), "method classFileVersion");
        check(hasMethod(c, "concat"), "method concat");
        check(hasMethod(c, "copyToSegmentRaw"), "method copyToSegmentRaw");
        check(hasMethod(c, "countNonZeroAscii"), "method countNonZeroAscii");
        check(hasMethod(c, "countPositives"), "method countPositives");
        check(hasMethod(c, "createOrGetClassLoaderValueMap"), "method createOrGetClassLoaderValueMap");
        check(hasMethod(c, "currentCarrierThread"), "method currentCarrierThread");
        check(hasMethod(c, "decodeASCII"), "method decodeASCII");
        check(hasMethod(c, "defineClass"), "method defineClass");
        check(hasMethod(c, "defineModule"), "method defineModule");
        check(hasMethod(c, "definePackage"), "method definePackage");
        check(hasMethod(c, "defineUnnamedModule"), "method defineUnnamedModule");
        check(hasMethod(c, "encodeASCII"), "method encodeASCII");
        check(hasMethod(c, "ensureNativeAccess"), "method ensureNativeAccess");
        check(hasMethod(c, "findBootstrapClassOrNull"), "method findBootstrapClassOrNull");
        check(hasMethod(c, "findMethod"), "method findMethod");
        check(hasMethod(c, "finishInit"), "method finishInit");
        check(hasMethod(c, "getAllThreads"), "method getAllThreads");
        check(hasMethod(c, "getAnnotationType"), "method getAnnotationType");
        check(hasMethod(c, "getBytesUTF8OrThrow"), "method getBytesUTF8OrThrow");
        check(hasMethod(c, "getCarrierThreadLocal"), "method getCarrierThreadLocal");
        check(hasMethod(c, "getClassFileAccessFlags"), "method getClassFileAccessFlags");
        check(hasMethod(c, "getConstantPool"), "method getConstantPool");
        check(hasMethod(c, "getContinuation"), "method getContinuation");
        check(hasMethod(c, "getDeclaredAnnotationMap"), "method getDeclaredAnnotationMap");
        check(hasMethod(c, "getDeclaredPublicMethods"), "method getDeclaredPublicMethods");
        check(hasMethod(c, "getEnumConstantsShared"), "method getEnumConstantsShared");
        check(hasMethod(c, "getLoaderNameID"), "method getLoaderNameID");
        check(hasMethod(c, "getRawClassAnnotations"), "method getRawClassAnnotations");
        check(hasMethod(c, "getRawClassTypeAnnotations"), "method getRawClassTypeAnnotations");
        check(hasMethod(c, "getRawExecutableTypeAnnotations"), "method getRawExecutableTypeAnnotations");
        check(hasMethod(c, "getServicesCatalog"), "method getServicesCatalog");
        check(hasMethod(c, "headStackableScope"), "method headStackableScope");
        check(hasMethod(c, "inflateBytesToChars"), "method inflateBytesToChars");
        check(hasMethod(c, "initialSystemErr"), "method initialSystemErr");
        check(hasMethod(c, "initialSystemIn"), "method initialSystemIn");
        check(hasMethod(c, "invokeFinalize"), "method invokeFinalize");
        check(hasMethod(c, "isFinalMutationEnabled"), "method isFinalMutationEnabled");
        check(hasMethod(c, "isReflectivelyExported"), "method isReflectivelyExported");
        check(hasMethod(c, "isReflectivelyOpened"), "method isReflectivelyOpened");
        check(hasMethod(c, "isStaticallyExported"), "method isStaticallyExported");
        check(hasMethod(c, "isStaticallyOpened"), "method isStaticallyOpened");
        check(hasMethod(c, "join"), "method join");
        check(hasMethod(c, "layers"), "method layers");
        check(hasMethod(c, "nativeLibrariesFor"), "method nativeLibrariesFor");
        check(hasMethod(c, "nativeThreadID"), "method nativeThreadID");
        check(hasMethod(c, "newStackWalkerInstance"), "method newStackWalkerInstance");
        check(hasMethod(c, "parkVirtualThread"), "method parkVirtualThread");
        check(hasMethod(c, "protectionDomain"), "method protectionDomain");
        check(hasMethod(c, "registerShutdownHook"), "method registerShutdownHook");
        check(hasMethod(c, "removeCarrierThreadLocal"), "method removeCarrierThreadLocal");
        check(hasMethod(c, "scopedValueBindings"), "method scopedValueBindings");
        check(hasMethod(c, "scopedValueCache"), "method scopedValueCache");
        check(hasMethod(c, "setCarrierThreadLocal"), "method setCarrierThreadLocal");
        check(hasMethod(c, "setCause"), "method setCause");
        check(hasMethod(c, "setContinuation"), "method setContinuation");
        check(hasMethod(c, "setHeadStackableScope"), "method setHeadStackableScope");
        check(hasMethod(c, "setScopedValueCache"), "method setScopedValueCache");
        check(hasMethod(c, "setThreadNativeID"), "method setThreadNativeID");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "stringCoder"), "method stringCoder");
        check(hasMethod(c, "stringConcatHelper"), "method stringConcatHelper");
        check(hasMethod(c, "stringInitCoder"), "method stringInitCoder");
        check(hasMethod(c, "threadContainer"), "method threadContainer");
        check(hasMethod(c, "tryEnableFinalMutation"), "method tryEnableFinalMutation");
        check(hasMethod(c, "uncheckedGetBytesOrThrow"), "method uncheckedGetBytesOrThrow");
        check(hasMethod(c, "uncheckedGetUTF16Char"), "method uncheckedGetUTF16Char");
        check(hasMethod(c, "uncheckedNewStringOrThrow"), "method uncheckedNewStringOrThrow");
        check(hasMethod(c, "uncheckedNewStringWithLatin1Bytes"), "method uncheckedNewStringWithLatin1Bytes");
        check(hasMethod(c, "uncheckedPutCharUTF16"), "method uncheckedPutCharUTF16");
        check(hasMethod(c, "uncheckedStringConcat1"), "method uncheckedStringConcat1");
        check(hasMethod(c, "unparkVirtualThread"), "method unparkVirtualThread");
        check(hasMethod(c, "virtualThreadContinuationScope"), "method virtualThreadContinuationScope");
        check(hasMethod(c, "virtualThreadDefaultScheduler"), "method virtualThreadDefaultScheduler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
