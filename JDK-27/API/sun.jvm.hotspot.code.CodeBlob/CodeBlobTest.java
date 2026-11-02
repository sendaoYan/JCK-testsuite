/*
 * @test
 * @summary sun.jvm.hotspot.code.CodeBlob conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main CodeBlobTest
 */
import java.lang.reflect.*;

public class CodeBlobTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.CodeBlob", false, CodeBlobTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "isNativeMethod"), "method isNativeMethod");
        check(hasMethod(c, "getHeaderSize"), "method getHeaderSize");
        check(hasMethod(c, "getContentOffset"), "method getContentOffset");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "contentBegin"), "method contentBegin");
        check(hasMethod(c, "isNMethod"), "method isNMethod");
        check(hasMethod(c, "codeEnd"), "method codeEnd");
        check(hasMethod(c, "codeBegin"), "method codeBegin");
        check(hasMethod(c, "getMutableDataSize"), "method getMutableDataSize");
        check(hasMethod(c, "contentEnd"), "method contentEnd");
        check(hasMethod(c, "getFrameCompleteOffset"), "method getFrameCompleteOffset");
        check(hasMethod(c, "getOopMaps"), "method getOopMaps");
        check(hasMethod(c, "getKind"), "method getKind");
        check(hasMethod(c, "headerBegin"), "method headerBegin");
        check(hasMethod(c, "getDataOffset"), "method getDataOffset");
        check(hasMethod(c, "getCodeOffset"), "method getCodeOffset");
        check(hasMethod(c, "mutableDataBegin"), "method mutableDataBegin");
        check(hasMethod(c, "dataEnd"), "method dataEnd");
        check(hasMethod(c, "dataBegin"), "method dataBegin");
        check(hasMethod(c, "contentContains"), "method contentContains");
        check(hasMethod(c, "codeContains"), "method codeContains");
        check(hasMethod(c, "getFrameSizeWords"), "method getFrameSizeWords");
        check(hasMethod(c, "printComponentsOn"), "method printComponentsOn");
        check(hasMethod(c, "getFrameSize"), "method getFrameSize");
        check(hasMethod(c, "getClassFor"), "method getClassFor");
        check(hasMethod(c, "headerEnd"), "method headerEnd");
        check(hasMethod(c, "mutableDataEnd"), "method mutableDataEnd");
        check(hasMethod(c, "isRuntimeStub"), "method isRuntimeStub");
        check(hasMethod(c, "isUpcallStub"), "method isUpcallStub");
        check(hasMethod(c, "isContinuationStub"), "method isContinuationStub");
        check(hasMethod(c, "isJavaMethod"), "method isJavaMethod");
        check(hasMethod(c, "asNMethodOrNull"), "method asNMethodOrNull");
        check(hasMethod(c, "getContentSize"), "method getContentSize");
        check(hasMethod(c, "getCodeSize"), "method getCodeSize");
        check(hasMethod(c, "getDataSize"), "method getDataSize");
        check(hasMethod(c, "getRelocationSize"), "method getRelocationSize");
        check(hasMethod(c, "blobContains"), "method blobContains");
        check(hasMethod(c, "dataContains"), "method dataContains");
        check(hasMethod(c, "isFrameCompleteAt"), "method isFrameCompleteAt");
        check(hasMethod(c, "getOopMapForReturnAddress"), "method getOopMapForReturnAddress");
        check(hasMethod(c, "callerMustGCArguments"), "method callerMustGCArguments");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
