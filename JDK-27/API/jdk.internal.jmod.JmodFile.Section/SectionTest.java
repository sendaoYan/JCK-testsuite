/*
 * @test
 * @summary jdk.internal.jmod.JmodFile.Section conformance: internal package reflection.
 * @modules java.base/jdk.internal.jmod:+open
 * @run main SectionTest
 */
public class SectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jmod.JmodFile$Section", false, SectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "jmodDir"), "method jmodDir");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "CLASSES"), "field CLASSES");
        check(hasField(c, "CONFIG"), "field CONFIG");
        check(hasField(c, "HEADER_FILES"), "field HEADER_FILES");
        check(hasField(c, "LEGAL_NOTICES"), "field LEGAL_NOTICES");
        check(hasField(c, "MAN_PAGES"), "field MAN_PAGES");
        check(hasField(c, "NATIVE_CMDS"), "field NATIVE_CMDS");
        check(hasField(c, "NATIVE_LIBS"), "field NATIVE_LIBS");
        check(hasField(c, "jmodDir"), "field jmodDir");
        check(hasEnumConstant(c, "CLASSES"), "enum CLASSES");
        check(hasEnumConstant(c, "CONFIG"), "enum CONFIG");
        check(hasEnumConstant(c, "HEADER_FILES"), "enum HEADER_FILES");
        check(hasEnumConstant(c, "LEGAL_NOTICES"), "enum LEGAL_NOTICES");
        check(hasEnumConstant(c, "MAN_PAGES"), "enum MAN_PAGES");
        check(hasEnumConstant(c, "NATIVE_CMDS"), "enum NATIVE_CMDS");
        check(hasEnumConstant(c, "NATIVE_LIBS"), "enum NATIVE_LIBS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
