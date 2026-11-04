/*
 * @test
 * @summary sun.nio.ch.SimpleAsynchronousFileChannelImpl conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main SimpleAsynchronousFileChannelImplTest
 */
public class SimpleAsynchronousFileChannelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SimpleAsynchronousFileChannelImpl", false, SimpleAsynchronousFileChannelImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.ch.AsynchronousFileChannelImpl"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "force"), "method force");
        check(hasMethod(c, "implRelease"), "method implRelease");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "truncate"), "method truncate");
        check(hasMethod(c, "tryLock"), "method tryLock");
        check(hasField(c, "nd"), "field nd");
        check(hasField(c, "path"), "field path");
        check(hasField(c, "threads"), "field threads");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
