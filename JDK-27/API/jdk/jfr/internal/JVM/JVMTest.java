/*
 * @test
 * @summary jdk.jfr.internal.JVM conformance: internal package reflection.
 * @modules jdk.jfr/jdk.jfr.internal:+open
 * @run main JVMTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JVMTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static String[] methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(m.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] enumConstants(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.JVM", false, JVMTest.class.getClassLoader());
        check(c.getName().equals("jdk.jfr.internal.JVM"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"abort","addStringConstant","beginRecording","commit","counterTime","drainStaleMethodTracerIds","emitDataLoss","emitEvent","emitOldObjectSamples","endRecording","exclude","flush","flush","getAllEventClasses","getAllowedToDoEventRetransforms","getChunkStartNanos","getClassId","getConfiguration","getDumpPath","getEventWriter","getPid","getStackTraceId","getThreadId","getTicksFrequency","getTimeConversionFactor","getTypeId","getTypeId","getUnloadedEventClassCount","hostMemoryUsage","hostTotalMemory","hostTotalSwapMemory","include","isAvailable","isContainerized","isExcluded","isExcluded","isInstrumented","isProduct","isRecording","log","logEvent","markChunkFinal","nanosNow","newEventWriter","registerStackFilter","retransformClasses","setCPUPeriod","setCPURate","setCompressedIntegers","setConfiguration","setCutoff","setDumpPath","setEnabled","setFileNotification","setForceInstrumentation","setGlobalBufferCount","setGlobalBufferSize","setMemorySize","setMethodSamplingPeriod","setMethodTraceFilters","setMiscellaneous","setOutput","setRepositoryLocation","setStackDepth","setStackTraceEnabled","setThreadBufferSize","setThreshold","setThrottle","shouldRotateDisk","storeMetadataDescriptor","subscribeLogLevel","tryUpdateEpoch","uncaughtException","unregisterStackFilter"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"CHUNK_ROTATION_MONITOR"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
