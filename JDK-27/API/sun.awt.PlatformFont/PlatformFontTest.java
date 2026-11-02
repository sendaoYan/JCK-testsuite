/*
 * @test
 * @summary sun.awt.PlatformFont conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main PlatformFontTest
 */

import java.util.*;
import java.lang.reflect.*;

public class PlatformFontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.PlatformFont", false, PlatformFontTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.PlatformFont".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.peer.FontPeer".equals(interfaces(c)), "interfaces");
        check("private static native#void#initIDs();protected abstract#char#getMissingGlyphCharacter();protected final#[Ljava.lang.Object;#getFontCache();public#[Ljava.lang.Object;#makeConvertedMultiFontChars([C,int,int);public#[Ljava.lang.Object;#makeConvertedMultiFontString(java.lang.String);public#[Lsun.awt.CharsetString;#makeMultiCharsetString([C,int,int);public#[Lsun.awt.CharsetString;#makeMultiCharsetString([C,int,int,boolean);public#[Lsun.awt.CharsetString;#makeMultiCharsetString(java.lang.String);public#[Lsun.awt.CharsetString;#makeMultiCharsetString(java.lang.String,boolean);public#boolean#mightHaveMultiFontMetrics()".equals(methods(c)), "methods");
        check("private#[Ljava.lang.Object;#fontCache;protected static#int#FONTCACHEMASK;protected static#int#FONTCACHESIZE;protected static#java.lang.String#osVersion;protected#[Lsun.awt.FontDescriptor;#componentFonts;protected#char#defaultChar;protected#java.lang.String#familyName;protected#sun.awt.FontConfiguration#fontConfig;protected#sun.awt.FontDescriptor#defaultFont".equals(fields(c)), "fields");
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
