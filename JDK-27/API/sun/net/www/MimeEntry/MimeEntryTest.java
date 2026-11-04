/*
 * @test
 * @summary sun.net.www.MimeEntry conformance: internal package reflection.
 * @modules java.base/sun.net.www:+open
 * @run main MimeEntryTest
 */
public class MimeEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.MimeEntry", false, MimeEntryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "getAction"), "method getAction");
        check(hasMethod(c, "getDescription"), "method getDescription");
        check(hasMethod(c, "getExtensions"), "method getExtensions");
        check(hasMethod(c, "getExtensionsAsList"), "method getExtensionsAsList");
        check(hasMethod(c, "getImageFileName"), "method getImageFileName");
        check(hasMethod(c, "getLaunchString"), "method getLaunchString");
        check(hasMethod(c, "getTempFileTemplate"), "method getTempFileTemplate");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "matches"), "method matches");
        check(hasMethod(c, "setAction"), "method setAction");
        check(hasMethod(c, "setCommand"), "method setCommand");
        check(hasMethod(c, "setDescription"), "method setDescription");
        check(hasMethod(c, "setExtensions"), "method setExtensions");
        check(hasMethod(c, "setImageFileName"), "method setImageFileName");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "toProperty"), "method toProperty");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "LAUNCH_APPLICATION"), "field LAUNCH_APPLICATION");
        check(hasField(c, "LOAD_INTO_BROWSER"), "field LOAD_INTO_BROWSER");
        check(hasField(c, "SAVE_TO_FILE"), "field SAVE_TO_FILE");
        check(hasField(c, "UNKNOWN"), "field UNKNOWN");
        check(hasField(c, "action"), "field action");
        check(hasField(c, "actionKeywords"), "field actionKeywords");
        check(hasField(c, "command"), "field command");
        check(hasField(c, "description"), "field description");
        check(hasField(c, "fileExtensions"), "field fileExtensions");
        check(hasField(c, "imageFileName"), "field imageFileName");
        check(hasField(c, "starred"), "field starred");
        check(hasField(c, "tempFileNameTemplate"), "field tempFileNameTemplate");
        check(hasField(c, "typeName"), "field typeName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
