/*
 * @test
 * @summary sun.jvm.hotspot.oops.CellTypeState conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main CellTypeStateTest
 */
import java.lang.reflect.*;

public class CellTypeStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.CellTypeState", false, CellTypeStateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "copy"), "method copy");
        check(hasMethod(c, "merge"), "method merge");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "equal"), "method equal");
        check(hasMethod(c, "isLive"), "method isLive");
        check(hasMethod(c, "isReference"), "method isReference");
        check(hasMethod(c, "isAddress"), "method isAddress");
        check(hasMethod(c, "isValue"), "method isValue");
        check(hasMethod(c, "makeBottom"), "method makeBottom");
        check(hasMethod(c, "makeAny"), "method makeAny");
        check(hasMethod(c, "isValidState"), "method isValidState");
        check(hasMethod(c, "canBeUninit"), "method canBeUninit");
        check(hasMethod(c, "canBeValue"), "method canBeValue");
        check(hasMethod(c, "isInfoTop"), "method isInfoTop");
        check(hasMethod(c, "isInfoBottom"), "method isInfoBottom");
        check(hasMethod(c, "isLockReference"), "method isLockReference");
        check(hasMethod(c, "getInfo"), "method getInfo");
        check(hasMethod(c, "canBeReference"), "method canBeReference");
        check(hasMethod(c, "canBeAddress"), "method canBeAddress");
        check(hasMethod(c, "isBottom"), "method isBottom");
        check(hasMethod(c, "makeSlotRef"), "method makeSlotRef");
        check(hasMethod(c, "makeTop"), "method makeTop");
        check(hasMethod(c, "makeAddr"), "method makeAddr");
        check(hasMethod(c, "makeLineRef"), "method makeLineRef");
        check(hasMethod(c, "makeLockRef"), "method makeLockRef");
        check(hasMethod(c, "isUninit"), "method isUninit");
        check(hasMethod(c, "getMonitorSource"), "method getMonitorSource");
        check(hasMethod(c, "isGoodAddress"), "method isGoodAddress");
        check(hasMethod(c, "isNonlockReference"), "method isNonlockReference");
        check(hasMethod(c, "equalKind"), "method equalKind");
        check(hasMethod(c, "toChar"), "method toChar");
        check(hasField(c, "bottom"), "field bottom");
        check(hasField(c, "uninit"), "field uninit");
        check(hasField(c, "ref"), "field ref");
        check(hasField(c, "value"), "field value");
        check(hasField(c, "refUninit"), "field refUninit");
        check(hasField(c, "top"), "field top");
        check(hasField(c, "addr"), "field addr");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
