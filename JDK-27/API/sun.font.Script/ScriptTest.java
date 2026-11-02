/*
 * @test
 * @summary sun.font.Script conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main ScriptTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ScriptTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.Script", false, ScriptTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.Script".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("".equals(methods(c)), "methods");
        check("public static final#int#ARABIC;public static final#int#ARMENIAN;public static final#int#BENGALI;public static final#int#BOPOMOFO;public static final#int#BUHID;public static final#int#CANADIAN_ABORIGINAL;public static final#int#CHEROKEE;public static final#int#CODE_LIMIT;public static final#int#COMMON;public static final#int#COPTIC;public static final#int#CYRILLIC;public static final#int#DESERET;public static final#int#DEVANAGARI;public static final#int#ETHIOPIC;public static final#int#GEORGIAN;public static final#int#GOTHIC;public static final#int#GREEK;public static final#int#GUJARATI;public static final#int#GURMUKHI;public static final#int#HAN;public static final#int#HANGUL;public static final#int#HANUNOO;public static final#int#HEBREW;public static final#int#HIRAGANA;public static final#int#INHERITED;public static final#int#INVALID_CODE;public static final#int#KANNADA;public static final#int#KATAKANA;public static final#int#KHMER;public static final#int#LAO;public static final#int#LATIN;public static final#int#MALAYALAM;public static final#int#MONGOLIAN;public static final#int#MYANMAR;public static final#int#OGHAM;public static final#int#OLD_ITALIC;public static final#int#ORIYA;public static final#int#RUNIC;public static final#int#SINHALA;public static final#int#SYRIAC;public static final#int#TAGALOG;public static final#int#TAGBANWA;public static final#int#TAMIL;public static final#int#TELUGU;public static final#int#THAANA;public static final#int#THAI;public static final#int#TIBETAN;public static final#int#UCAS;public static final#int#YI".equals(fields(c)), "fields");
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
