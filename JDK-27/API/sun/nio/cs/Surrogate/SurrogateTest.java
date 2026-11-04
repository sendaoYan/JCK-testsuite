/*
 * @test
 * @summary sun.nio.cs.Surrogate conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main SurrogateTest
 */
public class SurrogateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.Surrogate", false, SurrogateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "high"), "method high");
        check(hasMethod(c, "is"), "method is");
        check(hasMethod(c, "isHigh"), "method isHigh");
        check(hasMethod(c, "isLow"), "method isLow");
        check(hasMethod(c, "low"), "method low");
        check(hasMethod(c, "neededFor"), "method neededFor");
        check(hasMethod(c, "toUCS4"), "method toUCS4");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "MAX"), "field MAX");
        check(hasField(c, "MAX_HIGH"), "field MAX_HIGH");
        check(hasField(c, "MAX_LOW"), "field MAX_LOW");
        check(hasField(c, "MIN"), "field MIN");
        check(hasField(c, "MIN_HIGH"), "field MIN_HIGH");
        check(hasField(c, "MIN_LOW"), "field MIN_LOW");
        check(hasField(c, "UCS4_MAX"), "field UCS4_MAX");
        check(hasField(c, "UCS4_MIN"), "field UCS4_MIN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
