/*
 * @test
 * @summary sun.text.UCompactIntArray conformance: internal package reflection.
 * @modules java.base/sun.text:+open
 * @run main UCompactIntArrayTest
 */
public class UCompactIntArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.UCompactIntArray", false, UCompactIntArrayTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "compact"), "method compact");
        check(hasMethod(c, "elementAt"), "method elementAt");
        check(hasMethod(c, "getKSize"), "method getKSize");
        check(hasMethod(c, "setElementAt"), "method setElementAt");
        check(hasField(c, "BLOCKCOUNT"), "field BLOCKCOUNT");
        check(hasField(c, "BLOCKMASK"), "field BLOCKMASK");
        check(hasField(c, "BLOCKSHIFT"), "field BLOCKSHIFT");
        check(hasField(c, "CODEPOINTMASK"), "field CODEPOINTMASK");
        check(hasField(c, "INDEXCOUNT"), "field INDEXCOUNT");
        check(hasField(c, "INDEXSHIFT"), "field INDEXSHIFT");
        check(hasField(c, "PLANECOUNT"), "field PLANECOUNT");
        check(hasField(c, "PLANEMASK"), "field PLANEMASK");
        check(hasField(c, "PLANESHIFT"), "field PLANESHIFT");
        check(hasField(c, "UNICODECOUNT"), "field UNICODECOUNT");
        check(hasField(c, "blockTouched"), "field blockTouched");
        check(hasField(c, "defaultValue"), "field defaultValue");
        check(hasField(c, "indices"), "field indices");
        check(hasField(c, "isCompact"), "field isCompact");
        check(hasField(c, "planeTouched"), "field planeTouched");
        check(hasField(c, "values"), "field values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
