/*
 * @test
 * @summary sun.jvm.hotspot.ui.Annotation conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui:+open
 * @run main AnnotationTest
 */
import java.lang.reflect.*;

public class AnnotationTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.Annotation", false, AnnotationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "setColor"), "method setColor");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getX"), "method getX");
        check(hasMethod(c, "getY"), "method getY");
        check(hasMethod(c, "getColor"), "method getColor");
        check(hasMethod(c, "getInterval"), "method getInterval");
        check(hasMethod(c, "draw"), "method draw");
        check(hasMethod(c, "setY"), "method setY");
        check(hasMethod(c, "getLowAddress"), "method getLowAddress");
        check(hasMethod(c, "computeWidthAndHeight"), "method computeWidthAndHeight");
        check(hasMethod(c, "setX"), "method setX");
        check(hasMethod(c, "getHighAddress"), "method getHighAddress");
        check(hasMethod(c, "setXAndY"), "method setXAndY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
