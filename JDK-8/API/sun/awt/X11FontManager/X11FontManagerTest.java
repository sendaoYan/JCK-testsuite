/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11FontManager conformance: reflection (no init, native/display-heavy).
 * @run main X11FontManagerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11FontManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11FontManager", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11FontManager".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.FcFontManager".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#java.lang.String#getX11FontName(java.lang.String);private#boolean#isHeadless();private#java.lang.String#getObliqueLucidaFontID(java.lang.String);private#java.lang.String#specificFontIDForName(java.lang.String);private#java.lang.String#switchFontIDForName(java.lang.String);private#void#getPlatformFontPathFromFontConfig();private#void#initObliqueLucidaFontMap();protected synchronized#java.lang.String#getFontPath(boolean);protected#[Ljava.lang.String;#getNativeNames(java.lang.String,java.lang.String);protected#javax.swing.plaf.FontUIResource#getFontConfigFUIR(java.lang.String,int,int);protected#sun.awt.FontConfiguration#createFontConfiguration();protected#void#addFontToPlatformFontPath(java.lang.String);protected#void#registerFontDir(java.lang.String);protected#void#registerFontDirs(java.lang.String);protected#void#registerPlatformFontsUsedByFontConfiguration();public static#sun.awt.X11FontManager#getInstance();public#java.lang.String#getFileNameFromPlatformName(java.lang.String);public#java.lang.String#getFileNameFromXLFD(java.lang.String);public#sun.awt.FontConfiguration#createFontConfiguration(boolean,boolean);public#void#loadFonts()".equals(methods(c)), "methods");
        check("#java.util.HashMap#oblmap;private static final#int#ADD_STYLE_NAME_FIELD;private static final#int#AVERAGE_WIDTH_FIELD;private static final#int#CHARSET_ENCODING_FIELD;private static final#int#CHARSET_REGISTRY_FIELD;private static final#int#FAMILY_NAME_FIELD;private static final#int#FOUNDRY_FIELD;private static final#int#PIXEL_SIZE_FIELD;private static final#int#POINT_SIZE_FIELD;private static final#int#RESOLUTION_X_FIELD;private static final#int#RESOLUTION_Y_FIELD;private static final#int#SETWIDTH_NAME_FIELD;private static final#int#SLANT_FIELD;private static final#int#SPACING_FIELD;private static final#int#WEIGHT_NAME_FIELD;private static#[Ljava.lang.String;#fontdirs;private static#java.util.HashMap#registeredDirs;private static#java.util.HashSet#fontConfigDirs;private static#java.util.Map#fontNameMap;private static#java.util.Map#xFontDirsMap;private static#java.util.Map#xlfdMap".equals(fields(c)), "fields");
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
            s.append(Modifier.toString(m.getModifiers())).append('#');
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
            l.add(Modifier.toString(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
        }
        Collections.sort(l);
        return String.join(";", l);
    }

    static String enumConstants(Class<?> c) {
        try {
            Object[] ec = c.getEnumConstants();
            if (ec == null) return "";
            List<String> l = new ArrayList<>();
            for (Object o : ec) l.add(((Enum<?>) o).name());
            Collections.sort(l);
            return String.join(";", l);
        } catch (Throwable t) {
            return "";
        }
    }
}
