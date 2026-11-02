/*
 * @test
 * @summary jdk.internal.org.jline.utils.AttributedStyle conformance: internal package reflection.
 * @modules jdk.internal.le/jdk.internal.org.jline.utils:+open
 * @run main AttributedStyleTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributedStyleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static String[] methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(m.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] enumConstants(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    static String[] interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        String[] a = l.toArray(new String[0]); Arrays.sort(a); return a;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.jline.utils.AttributedStyle", false, AttributedStyleTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.org.jline.utils.AttributedStyle"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Arrays.equals(interfaces(c), new String[]{}), "interfaces");
        check(Arrays.equals(methods(c), new String[]{"background","background","backgroundDefault","backgroundOff","backgroundRgb","blink","blinkDefault","blinkOff","bold","boldDefault","boldOff","conceal","concealDefault","concealOff","crossedOut","crossedOutDefault","crossedOutOff","equals","faint","faintDefault","faintOff","foreground","foreground","foregroundDefault","foregroundOff","foregroundRgb","getMask","getStyle","hashCode","hidden","hiddenDefault","hiddenOff","inverse","inverseDefault","inverseNeg","inverseOff","italic","italicDefault","italicOff","toAnsi","toString","underline","underlineDefault","underlineOff"}), "methods");
        check(Arrays.equals(fields(c), new String[]{"BLACK","BLUE","BOLD","BOLD_OFF","BRIGHT","CYAN","DEFAULT","GREEN","HIDDEN","HIDDEN_OFF","INVERSE","INVERSE_OFF","MAGENTA","RED","WHITE","YELLOW"}), "fields");
        check(Arrays.equals(enumConstants(c), new String[]{}), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
