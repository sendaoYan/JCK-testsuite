/*
 * @test
 * @summary jdk.jfr.internal.PlatformRecording conformance: internal package reflection.
 * @modules jdk.jfr/jdk.jfr.internal:+open
 * @run main PlatformRecordingTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlatformRecordingTest {
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
        Class<?> c = Class.forName("jdk.jfr.internal.PlatformRecording", false, PlatformRecordingTest.class.getClassLoader());
        check(c.getName().equals("jdk.jfr.internal.PlatformRecording"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{"java.lang.AutoCloseable"}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"addReport","checkSetDestination","close","dump","dumpStopped","filter","getChunks","getDestination","getDumpOnExit","getDuration","getFinalChunkStartNanos","getFlushInterval","getId","getMaxAge","getMaxSize","getName","getRecording","getReports","getSettingsCopy","getSize","getStartNanos","getStartTime","getState","getStopTime","getStreamIntervalMillis","isToDisk","makeDumpPath","newCopy","newSnapshotClone","open","removeBefore","removePath","scheduleStart","setConfiguration","setDestination","setDumpDirectory","setDumpOnExit","setDuration","setFinalStartnanos","setFlushInterval","setMaxAge","setMaxSize","setName","setRecording","setSetting","setSettings","setState","setToDisk","start","stop","toString"}), "methods");
        check(Arrays.equals(fields(c), new String[]{}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
