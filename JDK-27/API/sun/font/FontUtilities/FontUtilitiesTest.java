/*
 * @test
 * @summary sun.font.FontUtilities conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main FontUtilitiesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FontUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FontUtilities", false, FontUtilitiesTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.FontUtilities".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#void#initStatic();public static#boolean#debugFonts();public static#boolean#fontSupportsDefaultEncoding(java.awt.Font);public static#boolean#isComplexCharCode(int);public static#boolean#isComplexScript([C,int,int);public static#boolean#isComplexText([C,int,int);public static#boolean#isDefaultIgnorable(int);public static#boolean#isIgnorableWhitespace(int);public static#boolean#isLogging();public static#boolean#isNonSimpleChar(char);public static#boolean#textLayoutIsCompatible(java.awt.Font);public static#java.lang.String#mapFcName(java.lang.String);public static#javax.swing.plaf.FontUIResource#getCompositeFontUIResource(java.awt.Font);public static#javax.swing.plaf.FontUIResource#getFontConfigFUIR(java.lang.String,int,int);public static#sun.font.Font2D#getFont2D(java.awt.Font);public static#sun.util.logging.PlatformLogger#getLogger();public static#void#logInfo(java.lang.String);public static#void#logSevere(java.lang.String);public static#void#logWarning(java.lang.String)".equals(methods(c)), "methods");
        check("private static final#[[Ljava.lang.String;#nameMap;private static volatile#java.lang.ref.SoftReference#compMapRef;private static#boolean#debugFonts;private static#boolean#logging;private static#sun.util.logging.PlatformLogger#logger;public static final#int#MAX_LAYOUT_CHARCODE;public static final#int#MIN_LAYOUT_CHARCODE;public static#boolean#isLinux;public static#boolean#isMacOSX;public static#boolean#isMacOSX14;public static#boolean#isWindows;public static#boolean#useJDKScaler".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static String interfaces(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (Class<?> i : c.getInterfaces()) l.add(i.getName());
        Collections.sort(l);
        return String.join(";", l);
    }

    static String methods(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.isSynthetic()) continue;
            StringBuilder s = new StringBuilder();
            s.append(java.lang.reflect.Modifier.toString(m.getModifiers())).append('#');
            s.append(m.getReturnType().getName()).append('#');
            s.append(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) { if (i > 0) s.append(','); s.append(ps[i].getName()); }
            s.append(')');
            l.add(s.toString());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String fields(Class<?> c) {
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            l.add(java.lang.reflect.Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        if (!c.isEnum()) return "";
        List<String> l = new ArrayList<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) l.add(f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }
}
