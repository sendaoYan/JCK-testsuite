/*
 * @test
 * @summary com.sun.corba.se.impl.orbutil.threadpool.WorkQueueImpl conformance: type shape via reflection (headless-safe).
 * @run main WorkQueueImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class WorkQueueImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orbutil.threadpool.WorkQueueImpl", false, WorkQueueImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.orbutil.threadpool.WorkQueue"), "interface com.sun.corba.se.spi.orbutil.threadpool.WorkQueue");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("addWork"), "method addWork");
        check(mn.contains("averageTimeInQueue"), "method averageTimeInQueue");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getThreadPool"), "method getThreadPool");
        check(mn.contains("setThreadPool"), "method setThreadPool");
        check(mn.contains("totalWorkItemsAdded"), "method totalWorkItemsAdded");
        check(mn.contains("workItemsInQueue"), "method workItemsInQueue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
