/*
 * @test
 * @summary sun.nio.ch.SelectionKeyImpl conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main SelectionKeyImplTest
 */
public class SelectionKeyImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SelectionKeyImpl", false, SelectionKeyImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.AbstractSelectionKey"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "channel"), "method channel");
        check(hasMethod(c, "interestOps"), "method interestOps");
        check(hasMethod(c, "interestOpsAnd"), "method interestOpsAnd");
        check(hasMethod(c, "interestOpsOr"), "method interestOpsOr");
        check(hasMethod(c, "nioInterestOps"), "method nioInterestOps");
        check(hasMethod(c, "nioReadyOps"), "method nioReadyOps");
        check(hasMethod(c, "readyOps"), "method readyOps");
        check(hasMethod(c, "selector"), "method selector");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "INTERESTOPS"), "field INTERESTOPS");
        check(hasField(c, "channel"), "field channel");
        check(hasField(c, "index"), "field index");
        check(hasField(c, "interestOps"), "field interestOps");
        check(hasField(c, "lastPolled"), "field lastPolled");
        check(hasField(c, "readyOps"), "field readyOps");
        check(hasField(c, "registeredEvents"), "field registeredEvents");
        check(hasField(c, "reset"), "field reset");
        check(hasField(c, "selector"), "field selector");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
