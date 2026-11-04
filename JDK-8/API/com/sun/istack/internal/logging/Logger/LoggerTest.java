/*
 * @test
 * @summary com.sun.istack.internal.logging.Logger conformance: hierarchy and declared members via reflection.
 * @run main LoggerTest
 */

public class LoggerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.istack.internal.logging.Logger", false, null);
        check(c.getName().equals("com.sun.istack.internal.logging.Logger"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("config"), "declares config");
        check(ms.contains("entering"), "declares entering");
        check(ms.contains("exiting"), "declares exiting");
        check(ms.contains("fine"), "declares fine");
        check(ms.contains("finer"), "declares finer");
        check(ms.contains("finest"), "declares finest");
        check(ms.contains("getCallerMethodName"), "declares getCallerMethodName");
        check(ms.contains("getLogger"), "declares getLogger");
        check(ms.contains("getStackMethodName"), "declares getStackMethodName");
        check(ms.contains("getSystemLoggerName"), "declares getSystemLoggerName");
        check(ms.contains("info"), "declares info");
        check(ms.contains("isLoggable"), "declares isLoggable");
        check(ms.contains("isMethodCallLoggable"), "declares isMethodCallLoggable");
        check(ms.contains("log"), "declares log");
        check(ms.contains("logException"), "declares logException");
        check(ms.contains("logSevereException"), "declares logSevereException");
        check(ms.contains("setLevel"), "declares setLevel");
        check(ms.contains("severe"), "declares severe");
        check(ms.contains("warning"), "declares warning");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
