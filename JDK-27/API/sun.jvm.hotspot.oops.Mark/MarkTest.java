/*
 * @test
 * @summary sun.jvm.hotspot.oops.Mark conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main MarkTest
 */
import java.lang.reflect.*;

public class MarkTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.Mark", false, MarkTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "value"), "method value");
        check(hasMethod(c, "hash"), "method hash");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "isLocked"), "method isLocked");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "getKlass"), "method getKlass");
        check(hasMethod(c, "hasMonitor"), "method hasMonitor");
        check(hasMethod(c, "monitor"), "method monitor");
        check(hasMethod(c, "isUnlocked"), "method isUnlocked");
        check(hasMethod(c, "hasNoHash"), "method hasNoHash");
        check(hasMethod(c, "isMarked"), "method isMarked");
        check(hasMethod(c, "getKlassShift"), "method getKlassShift");
        check(hasMethod(c, "valueAsAddress"), "method valueAsAddress");
        check(hasMethod(c, "hasDisplacedMarkHelper"), "method hasDisplacedMarkHelper");
        check(hasMethod(c, "displacedMarkHelper"), "method displacedMarkHelper");
        check(hasMethod(c, "age"), "method age");
        check(hasMethod(c, "isBeingInflated"), "method isBeingInflated");
        check(hasMethod(c, "mustBePreserved"), "method mustBePreserved");
        check(hasMethod(c, "hasLocker"), "method hasLocker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
