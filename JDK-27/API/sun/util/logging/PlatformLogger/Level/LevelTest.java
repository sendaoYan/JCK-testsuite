/*
 * @test
 * @summary sun.util.logging.PlatformLogger.Level conformance: internal package reflection.
 * @modules java.base/sun.util.logging:+open
 * @run main LevelTest
 */
public class LevelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.logging.PlatformLogger$Level", false, LevelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "intValue"), "method intValue");
        check(hasMethod(c, "systemLevel"), "method systemLevel");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "ALL"), "field ALL");
        check(hasField(c, "CONFIG"), "field CONFIG");
        check(hasField(c, "FINE"), "field FINE");
        check(hasField(c, "FINER"), "field FINER");
        check(hasField(c, "FINEST"), "field FINEST");
        check(hasField(c, "INFO"), "field INFO");
        check(hasField(c, "LEVEL_VALUES"), "field LEVEL_VALUES");
        check(hasField(c, "OFF"), "field OFF");
        check(hasField(c, "SEVERE"), "field SEVERE");
        check(hasField(c, "SEVERITY_ALL"), "field SEVERITY_ALL");
        check(hasField(c, "SEVERITY_CONFIG"), "field SEVERITY_CONFIG");
        check(hasField(c, "SEVERITY_FINE"), "field SEVERITY_FINE");
        check(hasField(c, "SEVERITY_FINER"), "field SEVERITY_FINER");
        check(hasField(c, "SEVERITY_FINEST"), "field SEVERITY_FINEST");
        check(hasField(c, "SEVERITY_INFO"), "field SEVERITY_INFO");
        check(hasField(c, "SEVERITY_OFF"), "field SEVERITY_OFF");
        check(hasField(c, "SEVERITY_SEVERE"), "field SEVERITY_SEVERE");
        check(hasField(c, "SEVERITY_WARNING"), "field SEVERITY_WARNING");
        check(hasField(c, "WARNING"), "field WARNING");
        check(hasField(c, "systemLevel"), "field systemLevel");
        check(hasEnumConstant(c, "ALL"), "enum ALL");
        check(hasEnumConstant(c, "CONFIG"), "enum CONFIG");
        check(hasEnumConstant(c, "FINE"), "enum FINE");
        check(hasEnumConstant(c, "FINER"), "enum FINER");
        check(hasEnumConstant(c, "FINEST"), "enum FINEST");
        check(hasEnumConstant(c, "INFO"), "enum INFO");
        check(hasEnumConstant(c, "OFF"), "enum OFF");
        check(hasEnumConstant(c, "SEVERE"), "enum SEVERE");
        check(hasEnumConstant(c, "WARNING"), "enum WARNING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
