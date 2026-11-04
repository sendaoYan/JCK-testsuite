/*
 * @test
 * @summary sun.jvm.hotspot.runtime.amd64.AMD64Frame conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime.amd64:+open
 * @run main AMD64FrameTest
 */
import java.lang.reflect.*;

public class AMD64FrameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.amd64.AMD64Frame", false, AMD64FrameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.Frame".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "getID"), "method getID");
        check(hasMethod(c, "sender"), "method sender");
        check(hasMethod(c, "getSP"), "method getSP");
        check(hasMethod(c, "isInterpretedFrameValid"), "method isInterpretedFrameValid");
        check(hasMethod(c, "getFP"), "method getFP");
        check(hasMethod(c, "isSignalHandlerFrameDbg"), "method isSignalHandlerFrameDbg");
        check(hasMethod(c, "getUnextendedSP"), "method getUnextendedSP");
        check(hasMethod(c, "hasSenderPD"), "method hasSenderPD");
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
        check(hasMethod(c, "getInterpreterFrameBCI"), "method getInterpreterFrameBCI");
        check(hasMethod(c, "setSP"), "method setSP");
        check(hasMethod(c, "getSignalNumberDbg"), "method getSignalNumberDbg");
        check(hasMethod(c, "getSignalNameDbg"), "method getSignalNameDbg");
        check(hasMethod(c, "frameSize"), "method frameSize");
        check(hasMethod(c, "getLink"), "method getLink");
        check(hasMethod(c, "getSenderPC"), "method getSenderPC");
        check(hasMethod(c, "getSenderSP"), "method getSenderSP");
        check(hasMethod(c, "addressOfInterpreterFrameTOSAt"), "method addressOfInterpreterFrameTOSAt");
        check(hasMethod(c, "getInterpreterFrameSenderSP"), "method getInterpreterFrameSenderSP");
        check(hasMethod(c, "interpreterFrameMonitorBegin"), "method interpreterFrameMonitorBegin");
        check(hasMethod(c, "interpreterFrameMonitorEnd"), "method interpreterFrameMonitorEnd");
        check(hasMethod(c, "getSenderPCAddr"), "method getSenderPCAddr");
        check(hasMethod(c, "addressOfInterpreterFrameMDX"), "method addressOfInterpreterFrameMDX");
        check(hasMethod(c, "getInterpreterFrameBCP"), "method getInterpreterFrameBCP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
