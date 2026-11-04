/*
 * @test
 * @summary jdk.internal.random.Xoroshiro128PlusPlus conformance: internal package reflection.
 * @modules java.base/jdk.internal.random:+open
 * @run main Xoroshiro128PlusPlusTest
 */
public class Xoroshiro128PlusPlusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.random.Xoroshiro128PlusPlus", false, Xoroshiro128PlusPlusTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.util.random.RandomGenerator$LeapableGenerator"), "interface java.util.random.RandomGenerator$LeapableGenerator");
        check(hasMethod(c, "copy"), "method copy");
        check(hasMethod(c, "jump"), "method jump");
        check(hasMethod(c, "jumpDistance"), "method jumpDistance");
        check(hasMethod(c, "leap"), "method leap");
        check(hasMethod(c, "leapDistance"), "method leapDistance");
        check(hasMethod(c, "nextLong"), "method nextLong");
        check(hasField(c, "GROUP"), "field GROUP");
        check(hasField(c, "JUMP_TABLE"), "field JUMP_TABLE");
        check(hasField(c, "LEAP_TABLE"), "field LEAP_TABLE");
        check(hasField(c, "defaultGen"), "field defaultGen");
        check(hasField(c, "x0"), "field x0");
        check(hasField(c, "x1"), "field x1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
