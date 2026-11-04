/*
 * @test
 * @summary sun.net.www.http.KeepAliveStream conformance: internal package reflection.
 * @modules java.base/sun.net.www.http:+open
 * @run main KeepAliveStreamTest
 */
public class KeepAliveStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.KeepAliveStream", false, KeepAliveStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.MeteredStream"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.net.www.http.Hurryable"), "interface sun.net.www.http.Hurryable");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "hurry"), "method hurry");
        check(hasMethod(c, "mark"), "method mark");
        check(hasMethod(c, "markSupported"), "method markSupported");
        check(hasMethod(c, "remainingToRead"), "method remainingToRead");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setClosed"), "method setClosed");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "cleanerThread"), "field cleanerThread");
        check(hasField(c, "hc"), "field hc");
        check(hasField(c, "hurried"), "field hurried");
        check(hasField(c, "queue"), "field queue");
        check(hasField(c, "queuedForCleanup"), "field queuedForCleanup");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
