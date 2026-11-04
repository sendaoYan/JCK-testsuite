/*
 * @test
 * @summary sun.font.EAttribute conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main EAttributeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class EAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.EAttribute", false, EAttributeTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.EAttribute".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Enum".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#[Lsun.font.EAttribute;#values();public static#sun.font.EAttribute#forAttribute(java.text.AttributedCharacterIterator$Attribute);public static#sun.font.EAttribute#valueOf(java.lang.String);public#java.lang.String#toString()".equals(methods(c)), "methods");
        check("final#int#mask;final#java.awt.font.TextAttribute#att;public static final#sun.font.EAttribute#EBACKGROUND;public static final#sun.font.EAttribute#EBASELINE_TRANSFORM;public static final#sun.font.EAttribute#EBIDI_EMBEDDING;public static final#sun.font.EAttribute#ECHAR_REPLACEMENT;public static final#sun.font.EAttribute#EFAMILY;public static final#sun.font.EAttribute#EFONT;public static final#sun.font.EAttribute#EFOREGROUND;public static final#sun.font.EAttribute#EINPUT_METHOD_HIGHLIGHT;public static final#sun.font.EAttribute#EINPUT_METHOD_UNDERLINE;public static final#sun.font.EAttribute#EJUSTIFICATION;public static final#sun.font.EAttribute#EKERNING;public static final#sun.font.EAttribute#ELIGATURES;public static final#sun.font.EAttribute#ENUMERIC_SHAPING;public static final#sun.font.EAttribute#EPOSTURE;public static final#sun.font.EAttribute#ERUN_DIRECTION;public static final#sun.font.EAttribute#ESIZE;public static final#sun.font.EAttribute#ESTRIKETHROUGH;public static final#sun.font.EAttribute#ESUPERSCRIPT;public static final#sun.font.EAttribute#ESWAP_COLORS;public static final#sun.font.EAttribute#ETRACKING;public static final#sun.font.EAttribute#ETRANSFORM;public static final#sun.font.EAttribute#EUNDERLINE;public static final#sun.font.EAttribute#EWEIGHT;public static final#sun.font.EAttribute#EWIDTH;static final#[Lsun.font.EAttribute;#atts".equals(fields(c)), "fields");
        check("EBACKGROUND;EBASELINE_TRANSFORM;EBIDI_EMBEDDING;ECHAR_REPLACEMENT;EFAMILY;EFONT;EFOREGROUND;EINPUT_METHOD_HIGHLIGHT;EINPUT_METHOD_UNDERLINE;EJUSTIFICATION;EKERNING;ELIGATURES;ENUMERIC_SHAPING;EPOSTURE;ERUN_DIRECTION;ESIZE;ESTRIKETHROUGH;ESUPERSCRIPT;ESWAP_COLORS;ETRACKING;ETRANSFORM;EUNDERLINE;EWEIGHT;EWIDTH".equals(enumConstants(c)), "enumConstants");
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
