/*
 * @test
 * @summary jdk.internal.misc.InnocuousThread conformance: internal package reflection.
 * @modules java.base/jdk.internal.misc:+open
 * @run main InnocuousThreadTest
 */
public class InnocuousThreadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.misc.InnocuousThread", false, InnocuousThreadTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Thread"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "eraseThreadLocals"), "method eraseThreadLocals");
        check(hasMethod(c, "newSystemThread"), "method newSystemThread");
        check(hasMethod(c, "newThread"), "method newThread");
        check(hasMethod(c, "run"), "method run");
        check(hasMethod(c, "setContextClassLoader"), "method setContextClassLoader");
        check(hasMethod(c, "setUncaughtExceptionHandler"), "method setUncaughtExceptionHandler");
        check(hasField(c, "CONTEXTCLASSLOADER"), "field CONTEXTCLASSLOADER");
        check(hasField(c, "INHERITABLE_THREAD_LOCALS"), "field INHERITABLE_THREAD_LOCALS");
        check(hasField(c, "INNOCUOUSTHREADGROUP"), "field INNOCUOUSTHREADGROUP");
        check(hasField(c, "THREAD_LOCALS"), "field THREAD_LOCALS");
        check(hasField(c, "UNSAFE"), "field UNSAFE");
        check(hasField(c, "hasRun"), "field hasRun");
        check(hasField(c, "threadNumber"), "field threadNumber");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
