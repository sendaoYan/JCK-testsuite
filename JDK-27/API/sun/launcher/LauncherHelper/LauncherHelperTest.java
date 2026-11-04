/*
 * @test
 * @summary sun.launcher.LauncherHelper conformance: internal package reflection.
 * @modules java.base/sun.launcher:+open
 * @run main LauncherHelperTest
 */
public class LauncherHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.launcher.LauncherHelper", false, LauncherHelperTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "checkAndLoadMain"), "method checkAndLoadMain");
        check(hasMethod(c, "getApplicationClass"), "method getApplicationClass");
        check(hasField(c, "ADD_EXPORTS"), "field ADD_EXPORTS");
        check(hasField(c, "ADD_OPENS"), "field ADD_OPENS");
        check(hasField(c, "ENABLE_FINAL_FIELD_MUTATION"), "field ENABLE_FINAL_FIELD_MUTATION");
        check(hasField(c, "ENABLE_NATIVE_ACCESS"), "field ENABLE_NATIVE_ACCESS");
        check(hasField(c, "INDENT"), "field INDENT");
        check(hasField(c, "JAVAFX_APPLICATION_CLASS_NAME"), "field JAVAFX_APPLICATION_CLASS_NAME");
        check(hasField(c, "JAVAFX_APPLICATION_MARKER"), "field JAVAFX_APPLICATION_MARKER");
        check(hasField(c, "JAVAFX_FXHELPER_CLASS_NAME_SUFFIX"), "field JAVAFX_FXHELPER_CLASS_NAME_SUFFIX");
        check(hasField(c, "JAVAFX_GRAPHICS_MODULE_NAME"), "field JAVAFX_GRAPHICS_MODULE_NAME");
        check(hasField(c, "LAUNCHER_AGENT_CLASS"), "field LAUNCHER_AGENT_CLASS");
        check(hasField(c, "LM_CLASS"), "field LM_CLASS");
        check(hasField(c, "LM_JAR"), "field LM_JAR");
        check(hasField(c, "LM_MODULE"), "field LM_MODULE");
        check(hasField(c, "LM_SOURCE"), "field LM_SOURCE");
        check(hasField(c, "LM_UNKNOWN"), "field LM_UNKNOWN");
        check(hasField(c, "LOCALE_SETTINGS"), "field LOCALE_SETTINGS");
        check(hasField(c, "MAIN_CLASS"), "field MAIN_CLASS");
        check(hasField(c, "PROP_SETTINGS"), "field PROP_SETTINGS");
        check(hasField(c, "VM_SETTINGS"), "field VM_SETTINGS");
        check(hasField(c, "appClass"), "field appClass");
        check(hasField(c, "defaultBundleName"), "field defaultBundleName");
        check(hasField(c, "diagprop"), "field diagprop");
        check(hasField(c, "encoding"), "field encoding");
        check(hasField(c, "encprop"), "field encprop");
        check(hasField(c, "isCharsetSupported"), "field isCharsetSupported");
        check(hasField(c, "isStaticMain"), "field isStaticMain");
        check(hasField(c, "noArgMain"), "field noArgMain");
        check(hasField(c, "ostream"), "field ostream");
        check(hasField(c, "outBuf"), "field outBuf");
        check(hasField(c, "trace"), "field trace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
