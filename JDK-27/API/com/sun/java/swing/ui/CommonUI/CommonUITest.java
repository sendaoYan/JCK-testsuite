/*
 * @test
 * @summary com.sun.java.swing.ui.CommonUI conformance: internal package reflection.
 * @modules jdk.hotspot.agent/com.sun.java.swing.ui:+open
 * @run main CommonUITest
 */
import java.lang.reflect.*;

public class CommonUITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.java.swing.ui.CommonUI", false, CommonUITest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getButtconPrefSize"), "method getButtconPrefSize");
        check(hasMethod(c, "createLabel"), "method createLabel");
        check(hasMethod(c, "createTextField"), "method createTextField");
        check(hasMethod(c, "createRadioButton"), "method createRadioButton");
        check(hasMethod(c, "createCheckBox"), "method createCheckBox");
        check(hasMethod(c, "createComboBox"), "method createComboBox");
        check(hasMethod(c, "createButton"), "method createButton");
        check(hasMethod(c, "createSmallButton"), "method createSmallButton");
        check(hasMethod(c, "createBorder"), "method createBorder");
        check(hasMethod(c, "centerComponent"), "method centerComponent");
        check(hasMethod(c, "stringFromStringArray"), "method stringFromStringArray");
        check(hasMethod(c, "stringArrayFromString"), "method stringArrayFromString");
        check(hasMethod(c, "createListPane"), "method createListPane");
        check(hasMethod(c, "getParentFrame"), "method getParentFrame");
        check(hasMethod(c, "msToSec"), "method msToSec");
        check(hasMethod(c, "secToMs"), "method secToMs");
        check(hasMethod(c, "setWaitCursor"), "method setWaitCursor");
        check(hasMethod(c, "setDefaultCursor"), "method setDefaultCursor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
