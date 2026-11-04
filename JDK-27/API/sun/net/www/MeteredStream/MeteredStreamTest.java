/*
 * @test
 * @summary sun.net.www.MeteredStream conformance: internal package reflection.
 * @modules java.base/sun.net.www:+open
 * @run main MeteredStreamTest
 */
public class MeteredStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.MeteredStream", false, MeteredStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.FilterInputStream"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "available"), "method available");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "isLockHeldByCurrentThread"), "method isLockHeldByCurrentThread");
        check(hasMethod(c, "lock"), "method lock");
        check(hasMethod(c, "mark"), "method mark");
        check(hasMethod(c, "markSupported"), "method markSupported");
        check(hasMethod(c, "read"), "method read");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "skip"), "method skip");
        check(hasMethod(c, "unlock"), "method unlock");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "closed"), "field closed");
        check(hasField(c, "count"), "field count");
        check(hasField(c, "expected"), "field expected");
        check(hasField(c, "markLimit"), "field markLimit");
        check(hasField(c, "markedCount"), "field markedCount");
        check(hasField(c, "readLock"), "field readLock");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
