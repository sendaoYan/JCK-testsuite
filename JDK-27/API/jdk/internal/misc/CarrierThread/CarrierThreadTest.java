/*
 * @test
 * @summary jdk.internal.misc.CarrierThread conformance: internal package reflection.
 * @modules java.base/jdk.internal.misc:+open
 * @run main CarrierThreadTest
 */
public class CarrierThreadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.misc.CarrierThread", false, CarrierThreadTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.concurrent.ForkJoinWorkerThread"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "beginBlocking"), "method beginBlocking");
        check(hasMethod(c, "endBlocking"), "method endBlocking");
        check(hasMethod(c, "setContextClassLoader"), "method setContextClassLoader");
        check(hasMethod(c, "setUncaughtExceptionHandler"), "method setUncaughtExceptionHandler");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "CARRIER_THREADGROUP"), "field CARRIER_THREADGROUP");
        check(hasField(c, "COMPENSATE_IN_PROGRESS"), "field COMPENSATE_IN_PROGRESS");
        check(hasField(c, "COMPENSATING"), "field COMPENSATING");
        check(hasField(c, "CONTEXTCLASSLOADER"), "field CONTEXTCLASSLOADER");
        check(hasField(c, "INHERITABLETHREADLOCALS"), "field INHERITABLETHREADLOCALS");
        check(hasField(c, "JLA"), "field JLA");
        check(hasField(c, "NOT_COMPENSATING"), "field NOT_COMPENSATING");
        check(hasField(c, "U"), "field U");
        check(hasField(c, "compensateValue"), "field compensateValue");
        check(hasField(c, "compensating"), "field compensating");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
