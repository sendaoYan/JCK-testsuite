/*
 * @test
 * @summary sun.jvm.hotspot.code.NMethod conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main NMethodTest
 */
import java.lang.reflect.*;

public class NMethodTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.NMethod", false, NMethodTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.code.CodeBlob".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getMethod"), "method getMethod");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getMethodAt"), "method getMethodAt");
        check(hasMethod(c, "getAddress"), "method getAddress");
        check(hasMethod(c, "isNativeMethod"), "method isNativeMethod");
        check(hasMethod(c, "getScopeDescNearDbg"), "method getScopeDescNearDbg");
        check(hasMethod(c, "getScopeDescAt"), "method getScopeDescAt");
        check(hasMethod(c, "printComponentsOn"), "method printComponentsOn");
        check(hasMethod(c, "isJavaMethod"), "method isJavaMethod");
        check(hasMethod(c, "immutableDataBegin"), "method immutableDataBegin");
        check(hasMethod(c, "getImmutableDataSize"), "method getImmutableDataSize");
        check(hasMethod(c, "constantsEnd"), "method constantsEnd");
        check(hasMethod(c, "constantsBegin"), "method constantsBegin");
        check(hasMethod(c, "instsEnd"), "method instsEnd");
        check(hasMethod(c, "instsBegin"), "method instsBegin");
        check(hasMethod(c, "stubEnd"), "method stubEnd");
        check(hasMethod(c, "stubBegin"), "method stubBegin");
        check(hasMethod(c, "oopsEnd"), "method oopsEnd");
        check(hasMethod(c, "oopsBegin"), "method oopsBegin");
        check(hasMethod(c, "metadataEnd"), "method metadataEnd");
        check(hasMethod(c, "metadataBegin"), "method metadataBegin");
        check(hasMethod(c, "scopesDataEnd"), "method scopesDataEnd");
        check(hasMethod(c, "scopesDataBegin"), "method scopesDataBegin");
        check(hasMethod(c, "scopesPCsEnd"), "method scopesPCsEnd");
        check(hasMethod(c, "scopesPCsBegin"), "method scopesPCsBegin");
        check(hasMethod(c, "dependenciesEnd"), "method dependenciesEnd");
        check(hasMethod(c, "dependenciesBegin"), "method dependenciesBegin");
        check(hasMethod(c, "handlerTableEnd"), "method handlerTableEnd");
        check(hasMethod(c, "handlerTableBegin"), "method handlerTableBegin");
        check(hasMethod(c, "nulChkTableEnd"), "method nulChkTableEnd");
        check(hasMethod(c, "nulChkTableBegin"), "method nulChkTableBegin");
        check(hasMethod(c, "constantsSize"), "method constantsSize");
        check(hasMethod(c, "instsSize"), "method instsSize");
        check(hasMethod(c, "stubSize"), "method stubSize");
        check(hasMethod(c, "scopesDataSize"), "method scopesDataSize");
        check(hasMethod(c, "scopesPCsSize"), "method scopesPCsSize");
        check(hasMethod(c, "dependenciesSize"), "method dependenciesSize");
        check(hasMethod(c, "handlerTableSize"), "method handlerTableSize");
        check(hasMethod(c, "nulChkTableSize"), "method nulChkTableSize");
        check(hasMethod(c, "oopsSize"), "method oopsSize");
        check(hasMethod(c, "metadataSize"), "method metadataSize");
        check(hasMethod(c, "getEntryPointOffset"), "method getEntryPointOffset");
        check(hasMethod(c, "getVerifiedEntryPointOffset"), "method getVerifiedEntryPointOffset");
        check(hasMethod(c, "getOopsLength"), "method getOopsLength");
        check(hasMethod(c, "getMetadataLength"), "method getMetadataLength");
        check(hasMethod(c, "getMetadataAt"), "method getMetadataAt");
        check(hasMethod(c, "isDeoptEntry"), "method isDeoptEntry");
        check(hasMethod(c, "deoptHandlerEntry"), "method deoptHandlerEntry");
        check(hasMethod(c, "getPCDescAt"), "method getPCDescAt");
        check(hasMethod(c, "getPCDescNearDbg"), "method getPCDescNearDbg");
        check(hasMethod(c, "isOSRMethod"), "method isOSRMethod");
        check(hasMethod(c, "exceptionBegin"), "method exceptionBegin");
        check(hasMethod(c, "immutableDataEnd"), "method immutableDataEnd");
        check(hasMethod(c, "immutableDataRefCountBegin"), "method immutableDataRefCountBegin");
        check(hasMethod(c, "origPCOffset"), "method origPCOffset");
        check(hasMethod(c, "totalSize"), "method totalSize");
        check(hasMethod(c, "immutableDataSize"), "method immutableDataSize");
        check(hasMethod(c, "constantsContains"), "method constantsContains");
        check(hasMethod(c, "instsContains"), "method instsContains");
        check(hasMethod(c, "stubContains"), "method stubContains");
        check(hasMethod(c, "oopsContains"), "method oopsContains");
        check(hasMethod(c, "metadataContains"), "method metadataContains");
        check(hasMethod(c, "scopesDataContains"), "method scopesDataContains");
        check(hasMethod(c, "scopesPCsContains"), "method scopesPCsContains");
        check(hasMethod(c, "handlerTableContains"), "method handlerTableContains");
        check(hasMethod(c, "nulChkTableContains"), "method nulChkTableContains");
        check(hasMethod(c, "getEntryPoint"), "method getEntryPoint");
        check(hasMethod(c, "getVerifiedEntryPoint"), "method getVerifiedEntryPoint");
        check(hasMethod(c, "getOopAt"), "method getOopAt");
        check(hasMethod(c, "getOSREntryBCI"), "method getOSREntryBCI");
        check(hasMethod(c, "getOSRLink"), "method getOSRLink");
        check(hasMethod(c, "isDeoptPc"), "method isDeoptPc");
        check(hasMethod(c, "canBeDeoptimized"), "method canBeDeoptimized");
        check(hasMethod(c, "decodeAllScopeDescs"), "method decodeAllScopeDescs");
        check(hasMethod(c, "scope_desc_in"), "method scope_desc_in");
        check(hasMethod(c, "getSafepoints"), "method getSafepoints");
        check(hasMethod(c, "getOSREntryPointOffset"), "method getOSREntryPointOffset");
        check(hasMethod(c, "getEntryBCIOffset"), "method getEntryBCIOffset");
        check(hasMethod(c, "getMethodOffset"), "method getMethodOffset");
        check(hasMethod(c, "flagsToString"), "method flagsToString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
