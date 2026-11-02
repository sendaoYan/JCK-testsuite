/*
 * @test
 * @summary sun.text.SupplementaryCharacterData conformance: internal package reflection.
 * @modules java.base/sun.text:+open
 * @run main SupplementaryCharacterDataTest
 */
public class SupplementaryCharacterDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.SupplementaryCharacterData", false, SupplementaryCharacterDataTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "getArray"), "method getArray");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "IGNORE"), "field IGNORE");
        check(hasField(c, "dataTable"), "field dataTable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
