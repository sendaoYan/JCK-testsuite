/*
 * @test
 * @summary com.sun.corba.se.spi.orbutil.threadpool.ThreadPool conformance: type shape via reflection (headless-safe).
 * @run main ThreadPoolTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ThreadPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orbutil.threadpool.ThreadPool", false, ThreadPoolTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Closeable"), "interface java.io.Closeable");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 12, "public declared method count");
        check(mn.contains("averageWorkCompletionTime"), "method averageWorkCompletionTime");
        check(mn.contains("currentNumberOfThreads"), "method currentNumberOfThreads");
        check(mn.contains("currentProcessedCount"), "method currentProcessedCount");
        check(mn.contains("getAnyWorkQueue"), "method getAnyWorkQueue");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getWorkQueue"), "method getWorkQueue");
        check(mn.contains("idleTimeoutForThreads"), "method idleTimeoutForThreads");
        check(mn.contains("maximumNumberOfThreads"), "method maximumNumberOfThreads");
        check(mn.contains("minimumNumberOfThreads"), "method minimumNumberOfThreads");
        check(mn.contains("numberOfAvailableThreads"), "method numberOfAvailableThreads");
        check(mn.contains("numberOfBusyThreads"), "method numberOfBusyThreads");
        check(mn.contains("numberOfWorkQueues"), "method numberOfWorkQueues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
