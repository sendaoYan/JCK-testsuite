/*
 * @test
 * @summary com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolManagerImpl conformance: type shape via reflection (headless-safe).
 * @run main ThreadPoolManagerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ThreadPoolManagerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolManagerImpl", false, ThreadPoolManagerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager"), "interface com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("close"), "method close");
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
