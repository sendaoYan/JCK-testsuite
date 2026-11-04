/*
 * @test
 * @summary sun.nio.ch.SelectorImpl conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main SelectorImplTest
 */
public class SelectorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SelectorImpl", false, SelectorImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.AbstractSelector"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "begin"), "method begin");
        check(hasMethod(c, "cancel"), "method cancel");
        check(hasMethod(c, "doSelect"), "method doSelect");
        check(hasMethod(c, "end"), "method end");
        check(hasMethod(c, "implClose"), "method implClose");
        check(hasMethod(c, "implCloseSelector"), "method implCloseSelector");
        check(hasMethod(c, "implDereg"), "method implDereg");
        check(hasMethod(c, "implRegister"), "method implRegister");
        check(hasMethod(c, "keys"), "method keys");
        check(hasMethod(c, "processDeregisterQueue"), "method processDeregisterQueue");
        check(hasMethod(c, "processReadyEvents"), "method processReadyEvents");
        check(hasMethod(c, "register"), "method register");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "selectNow"), "method selectNow");
        check(hasMethod(c, "selectedKeys"), "method selectedKeys");
        check(hasMethod(c, "setEventOps"), "method setEventOps");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "cancelledKeys"), "field cancelledKeys");
        check(hasField(c, "inSelect"), "field inSelect");
        check(hasField(c, "keys"), "field keys");
        check(hasField(c, "publicKeys"), "field publicKeys");
        check(hasField(c, "publicSelectedKeys"), "field publicSelectedKeys");
        check(hasField(c, "selectedKeys"), "field selectedKeys");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
