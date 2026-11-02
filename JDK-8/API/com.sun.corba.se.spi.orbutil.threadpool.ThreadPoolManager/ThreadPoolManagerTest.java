/*
 * @test
 * @summary com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager conformance: type shape via reflection (headless-safe).
 * @run main ThreadPoolManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ThreadPoolManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager", false, ThreadPoolManagerTest.class.getClassLoader());
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
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("getDefaultThreadPool"), "method getDefaultThreadPool");
        check(mn.contains("getThreadPool"), "method getThreadPool");
        check(mn.contains("getThreadPoolChooser"), "method getThreadPoolChooser");
        check(mn.contains("getThreadPoolChooserNumericId"), "method getThreadPoolChooserNumericId");
        check(mn.contains("getThreadPoolNumericId"), "method getThreadPoolNumericId");
        check(mn.contains("getThreadPoolStringId"), "method getThreadPoolStringId");
        check(mn.contains("setThreadPoolChooser"), "method setThreadPoolChooser");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
