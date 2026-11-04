/*
 * @test
 * @summary sun.jvm.hotspot.runtime.Frame conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main FrameTest
 */
import java.lang.reflect.*;

public class FrameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.Frame", false, FrameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "cb"), "method cb");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getIntAt"), "method getIntAt");
        check(hasMethod(c, "getID"), "method getID");
        check(hasMethod(c, "sender"), "method sender");
        check(hasMethod(c, "getOopHandleAt"), "method getOopHandleAt");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "getSP"), "method getSP");
        check(hasMethod(c, "isFirstFrame"), "method isFirstFrame");
        check(hasMethod(c, "isInterpretedFrame"), "method isInterpretedFrame");
        check(hasMethod(c, "isInterpretedFrameValid"), "method isInterpretedFrameValid");
        check(hasMethod(c, "isJavaFrame"), "method isJavaFrame");
        check(hasMethod(c, "isCompiledFrame"), "method isCompiledFrame");
        check(hasMethod(c, "isEntryFrame"), "method isEntryFrame");
        check(hasMethod(c, "isNativeFrame"), "method isNativeFrame");
        check(hasMethod(c, "isRuntimeFrame"), "method isRuntimeFrame");
        check(hasMethod(c, "getPC"), "method getPC");
        check(hasMethod(c, "getFP"), "method getFP");
        check(hasMethod(c, "isSignalHandlerFrameDbg"), "method isSignalHandlerFrameDbg");
        check(hasMethod(c, "oopsDo"), "method oopsDo");
        check(hasMethod(c, "realSender"), "method realSender");
        check(hasMethod(c, "isDeoptimized"), "method isDeoptimized");
        check(hasMethod(c, "printValue"), "method printValue");
        check(hasMethod(c, "canBeDeoptimized"), "method canBeDeoptimized");
        check(hasMethod(c, "oopMapRegToLocation"), "method oopMapRegToLocation");
        check(hasMethod(c, "getUnextendedSP"), "method getUnextendedSP");
        check(hasMethod(c, "pcReturnOffset"), "method pcReturnOffset");
        check(hasMethod(c, "bcpToBci"), "method bcpToBci");
        check(hasMethod(c, "entryFrameIsFirst"), "method entryFrameIsFirst");
        check(hasMethod(c, "hasSenderPD"), "method hasSenderPD");
        check(hasMethod(c, "addressOfStackSlot"), "method addressOfStackSlot");
        check(hasMethod(c, "addressOfInterpreterFrameLocals"), "method addressOfInterpreterFrameLocals");
        check(hasMethod(c, "addressOfInterpreterFrameExpressionStack"), "method addressOfInterpreterFrameExpressionStack");
        check(hasMethod(c, "getInterpreterFrameExpressionStackDirection"), "method getInterpreterFrameExpressionStackDirection");
        check(hasMethod(c, "addressOfInterpreterFrameTOS"), "method addressOfInterpreterFrameTOS");
        check(hasMethod(c, "interpreterFrameMonitorSize"), "method interpreterFrameMonitorSize");
        check(hasMethod(c, "addressOfInterpreterFrameMethod"), "method addressOfInterpreterFrameMethod");
        check(hasMethod(c, "addressOfInterpreterFrameCPCache"), "method addressOfInterpreterFrameCPCache");
        check(hasMethod(c, "getEntryFrameCallWrapper"), "method getEntryFrameCallWrapper");
        check(hasMethod(c, "addressOfSavedOopResult"), "method addressOfSavedOopResult");
        check(hasMethod(c, "addressOfSavedReceiver"), "method addressOfSavedReceiver");
        check(hasMethod(c, "getInterpreterFrameMethod"), "method getInterpreterFrameMethod");
        check(hasMethod(c, "getInterpreterFrameBCI"), "method getInterpreterFrameBCI");
        check(hasMethod(c, "getInterpreterFrameExpressionStackSize"), "method getInterpreterFrameExpressionStackSize");
        check(hasMethod(c, "oopsInterpretedArgumentsDo"), "method oopsInterpretedArgumentsDo");
        check(hasMethod(c, "adjustForDeopt"), "method adjustForDeopt");
        check(hasMethod(c, "setPC"), "method setPC");
        check(hasMethod(c, "setSP"), "method setSP");
        check(hasMethod(c, "isFirstJavaFrame"), "method isFirstJavaFrame");
        check(hasMethod(c, "getSignalNumberDbg"), "method getSignalNumberDbg");
        check(hasMethod(c, "getSignalNameDbg"), "method getSignalNameDbg");
        check(hasMethod(c, "shouldBeDeoptimized"), "method shouldBeDeoptimized");
        check(hasMethod(c, "frameSize"), "method frameSize");
        check(hasMethod(c, "getLink"), "method getLink");
        check(hasMethod(c, "getSenderPC"), "method getSenderPC");
        check(hasMethod(c, "getSenderSP"), "method getSenderSP");
        check(hasMethod(c, "addressOfInterpreterFrameLocal"), "method addressOfInterpreterFrameLocal");
        check(hasMethod(c, "addressOfInterpreterFrameExpressionStackSlot"), "method addressOfInterpreterFrameExpressionStackSlot");
        check(hasMethod(c, "addressOfInterpreterFrameTOSAt"), "method addressOfInterpreterFrameTOSAt");
        check(hasMethod(c, "getInterpreterFrameSenderSP"), "method getInterpreterFrameSenderSP");
        check(hasMethod(c, "interpreterFrameMonitorBegin"), "method interpreterFrameMonitorBegin");
        check(hasMethod(c, "interpreterFrameMonitorEnd"), "method interpreterFrameMonitorEnd");
        check(hasMethod(c, "nextMonitorInInterpreterFrame"), "method nextMonitorInInterpreterFrame");
        check(hasMethod(c, "previousMonitorInInterpreterFrame"), "method previousMonitorInInterpreterFrame");
        check(hasMethod(c, "getInterpreterFrameCPCache"), "method getInterpreterFrameCPCache");
        check(hasMethod(c, "getSavedOopResult"), "method getSavedOopResult");
        check(hasMethod(c, "getSavedReceiver"), "method getSavedReceiver");
        check(hasMethod(c, "interpreterFramePrintOn"), "method interpreterFramePrintOn");
        check(hasField(c, "raw_sp"), "field raw_sp");
        check(hasField(c, "pc"), "field pc");
        check(hasField(c, "deoptimized"), "field deoptimized");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
