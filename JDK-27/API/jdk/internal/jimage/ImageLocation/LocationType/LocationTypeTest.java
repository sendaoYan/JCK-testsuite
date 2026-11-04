/*
 * @test
 * @summary jdk.internal.jimage.ImageLocation.LocationType conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main LocationTypeTest
 */
public class LocationTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ImageLocation$LocationType", false, LocationTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "MODULES_DIR"), "field MODULES_DIR");
        check(hasField(c, "MODULES_ROOT"), "field MODULES_ROOT");
        check(hasField(c, "PACKAGES_DIR"), "field PACKAGES_DIR");
        check(hasField(c, "PACKAGES_ROOT"), "field PACKAGES_ROOT");
        check(hasField(c, "RESOURCE"), "field RESOURCE");
        check(hasEnumConstant(c, "MODULES_DIR"), "enum MODULES_DIR");
        check(hasEnumConstant(c, "MODULES_ROOT"), "enum MODULES_ROOT");
        check(hasEnumConstant(c, "PACKAGES_DIR"), "enum PACKAGES_DIR");
        check(hasEnumConstant(c, "PACKAGES_ROOT"), "enum PACKAGES_ROOT");
        check(hasEnumConstant(c, "RESOURCE"), "enum RESOURCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
