/*
 * @test
 * @summary sun.text.IntHashtable conformance: internal package reflection.
 * @modules java.base/sun.text:+open
 * @run main IntHashtableTest
 */
public class IntHashtableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.IntHashtable", false, IntHashtableTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getDefaultValue"), "method getDefaultValue");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "put"), "method put");
        check(hasMethod(c, "putInternal"), "method putInternal");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "setDefaultValue"), "method setDefaultValue");
        check(hasMethod(c, "size"), "method size");
        check(hasField(c, "DELETED"), "field DELETED");
        check(hasField(c, "EMPTY"), "field EMPTY");
        check(hasField(c, "HIGH_WATER_FACTOR"), "field HIGH_WATER_FACTOR");
        check(hasField(c, "LOW_WATER_FACTOR"), "field LOW_WATER_FACTOR");
        check(hasField(c, "MAX_UNUSED"), "field MAX_UNUSED");
        check(hasField(c, "PRIMES"), "field PRIMES");
        check(hasField(c, "count"), "field count");
        check(hasField(c, "defaultValue"), "field defaultValue");
        check(hasField(c, "highWaterMark"), "field highWaterMark");
        check(hasField(c, "keyList"), "field keyList");
        check(hasField(c, "lowWaterMark"), "field lowWaterMark");
        check(hasField(c, "primeIndex"), "field primeIndex");
        check(hasField(c, "values"), "field values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
