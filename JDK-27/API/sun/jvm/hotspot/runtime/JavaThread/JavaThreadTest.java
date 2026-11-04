/*
 * @test
 * @summary sun.jvm.hotspot.runtime.JavaThread conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main JavaThreadTest
 */
import java.lang.reflect.*;

public class JavaThreadTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.JavaThread", false, JavaThreadTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.Thread".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isTerminated"), "method isTerminated");
        check(hasMethod(c, "getThreadObj"), "method getThreadObj");
        check(hasMethod(c, "getLastJavaVFrame"), "method getLastJavaVFrame");
        check(hasMethod(c, "getThreadName"), "method getThreadName");
        check(hasMethod(c, "getCurrentFrameGuess"), "method getCurrentFrameGuess");
        check(hasMethod(c, "newRegisterMap"), "method newRegisterMap");
        check(hasMethod(c, "printInfoOn"), "method printInfoOn");
        check(hasMethod(c, "isLockOwned"), "method isLockOwned");
        check(hasMethod(c, "getTerminated"), "method getTerminated");
        check(hasMethod(c, "getLastJavaFP"), "method getLastJavaFP");
        check(hasMethod(c, "getBaseOfStackPointer"), "method getBaseOfStackPointer");
        check(hasMethod(c, "getLastJavaSP"), "method getLastJavaSP");
        check(hasMethod(c, "getLastFramePD"), "method getLastFramePD");
        check(hasMethod(c, "getLastFrame"), "method getLastFrame");
        check(hasMethod(c, "printThreadIDOn"), "method printThreadIDOn");
        check(hasMethod(c, "getThreadProxy"), "method getThreadProxy");
        check(hasMethod(c, "getStackBase"), "method getStackBase");
        check(hasMethod(c, "oopsDo"), "method oopsDo");
        check(hasMethod(c, "lastSPDbg"), "method lastSPDbg");
        check(hasMethod(c, "getStackSize"), "method getStackSize");
        check(hasMethod(c, "getThreadState"), "method getThreadState");
        check(hasMethod(c, "getLastJavaPC"), "method getLastJavaPC");
        check(hasMethod(c, "getOSThread"), "method getOSThread");
        check(hasMethod(c, "isExiting"), "method isExiting");
        check(hasMethod(c, "getAnchorField"), "method getAnchorField");
        check(hasMethod(c, "hasLastJavaFrame"), "method hasLastJavaFrame");
        check(hasMethod(c, "getLastJavaVFrameDbg"), "method getLastJavaVFrameDbg");
        check(hasMethod(c, "printThreadID"), "method printThreadID");
        check(hasMethod(c, "getStackBaseValue"), "method getStackBaseValue");
        check(hasMethod(c, "getContEntry"), "method getContEntry");
        check(hasMethod(c, "getMonitorOwnerID"), "method getMonitorOwnerID");
        check(hasMethod(c, "isInStack"), "method isInStack");
        check(hasMethod(c, "getCurrentParkBlocker"), "method getCurrentParkBlocker");
        check(hasMethod(c, "activeHandles"), "method activeHandles");
        check(hasMethod(c, "printThreadContextOn"), "method printThreadContextOn");
        check(hasMethod(c, "printThreadInfoOn"), "method printThreadInfoOn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
