/*
 * @test
 * @summary jdk.internal.misc.VM conformance: internal package reflection.
 * @modules java.base/jdk.internal.misc:+open
 * @run main VMTest
 */
public class VMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.misc.VM", false, VMTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addFinalRefCount"), "method addFinalRefCount");
        check(hasMethod(c, "awaitInitLevel"), "method awaitInitLevel");
        check(hasMethod(c, "getBufferPools"), "method getBufferPools");
        check(hasMethod(c, "getFinalRefCount"), "method getFinalRefCount");
        check(hasMethod(c, "getNanoTimeAdjustment"), "method getNanoTimeAdjustment");
        check(hasMethod(c, "getPeakFinalRefCount"), "method getPeakFinalRefCount");
        check(hasMethod(c, "getRuntimeArguments"), "method getRuntimeArguments");
        check(hasMethod(c, "getSavedProperties"), "method getSavedProperties");
        check(hasMethod(c, "getSavedProperty"), "method getSavedProperty");
        check(hasMethod(c, "getegid"), "method getegid");
        check(hasMethod(c, "geteuid"), "method geteuid");
        check(hasMethod(c, "getgid"), "method getgid");
        check(hasMethod(c, "getuid"), "method getuid");
        check(hasMethod(c, "initLevel"), "method initLevel");
        check(hasMethod(c, "initialErr"), "method initialErr");
        check(hasMethod(c, "initializeOSEnvironment"), "method initializeOSEnvironment");
        check(hasMethod(c, "isBooted"), "method isBooted");
        check(hasMethod(c, "isDirectMemoryPageAligned"), "method isDirectMemoryPageAligned");
        check(hasMethod(c, "isJavaLangInvokeInited"), "method isJavaLangInvokeInited");
        check(hasMethod(c, "isModuleSystemInited"), "method isModuleSystemInited");
        check(hasMethod(c, "isSetUID"), "method isSetUID");
        check(hasMethod(c, "isShutdown"), "method isShutdown");
        check(hasMethod(c, "isSupportedClassFileVersion"), "method isSupportedClassFileVersion");
        check(hasMethod(c, "isSupportedModuleDescriptorVersion"), "method isSupportedModuleDescriptorVersion");
        check(hasMethod(c, "isSystemDomainLoader"), "method isSystemDomainLoader");
        check(hasMethod(c, "latestUserDefinedLoader"), "method latestUserDefinedLoader");
        check(hasMethod(c, "maxDirectMemory"), "method maxDirectMemory");
        check(hasMethod(c, "saveProperties"), "method saveProperties");
        check(hasMethod(c, "setJavaLangInvokeInited"), "method setJavaLangInvokeInited");
        check(hasMethod(c, "shutdown"), "method shutdown");
        check(hasMethod(c, "toThreadState"), "method toThreadState");
        check(hasField(c, "JAVA_LANG_SYSTEM_INITED"), "field JAVA_LANG_SYSTEM_INITED");
        check(hasField(c, "JVMTI_THREAD_STATE_ALIVE"), "field JVMTI_THREAD_STATE_ALIVE");
        check(hasField(c, "JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER"), "field JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER");
        check(hasField(c, "JVMTI_THREAD_STATE_RUNNABLE"), "field JVMTI_THREAD_STATE_RUNNABLE");
        check(hasField(c, "JVMTI_THREAD_STATE_TERMINATED"), "field JVMTI_THREAD_STATE_TERMINATED");
        check(hasField(c, "JVMTI_THREAD_STATE_WAITING_INDEFINITELY"), "field JVMTI_THREAD_STATE_WAITING_INDEFINITELY");
        check(hasField(c, "JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT"), "field JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT");
        check(hasField(c, "MODULE_SYSTEM_INITED"), "field MODULE_SYSTEM_INITED");
        check(hasField(c, "SYSTEM_BOOTED"), "field SYSTEM_BOOTED");
        check(hasField(c, "SYSTEM_LOADER_INITIALIZING"), "field SYSTEM_LOADER_INITIALIZING");
        check(hasField(c, "SYSTEM_SHUTDOWN"), "field SYSTEM_SHUTDOWN");
        check(hasField(c, "directMemory"), "field directMemory");
        check(hasField(c, "finalRefCount"), "field finalRefCount");
        check(hasField(c, "initLevel"), "field initLevel");
        check(hasField(c, "javaLangInvokeInited"), "field javaLangInvokeInited");
        check(hasField(c, "lock"), "field lock");
        check(hasField(c, "pageAlignDirectMemory"), "field pageAlignDirectMemory");
        check(hasField(c, "peakFinalRefCount"), "field peakFinalRefCount");
        check(hasField(c, "savedProps"), "field savedProps");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
