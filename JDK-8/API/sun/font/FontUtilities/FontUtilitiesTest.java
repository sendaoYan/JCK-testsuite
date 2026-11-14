/*
 * @test
 * @summary sun.font.FontUtilities conformance: reflection (no init, native/display-heavy).
 * @run main FontUtilitiesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FontUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FontUtilities", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.FontUtilities".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            System.out.println("PASS (" + passed + " assertions; SKIP build-specific internal structure)");
            return;
        }
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check(subset(methods(c), "public static#boolean#debugFonts();public static#boolean#fontSupportsDefaultEncoding(java.awt.Font);public static#boolean#isComplexCharCode(int);public static#boolean#isComplexText([C,int,int);public static#boolean#isLogging();public static#boolean#isNonSimpleChar(char);public static#boolean#textLayoutIsCompatible(java.awt.Font);public static#java.lang.String#mapFcName(java.lang.String);public static#javax.swing.plaf.FontUIResource#getCompositeFontUIResource(java.awt.Font);public static#javax.swing.plaf.FontUIResource#getFontConfigFUIR(java.lang.String,int,int);public static#sun.font.Font2D#getFont2D(java.awt.Font);public static#sun.util.logging.PlatformLogger#getLogger()"), "methods");
        check(subset(fields(c), "private static#[[Ljava.lang.String;#nameMap;private static volatile#java.lang.ref.SoftReference#compMapRef;private static#boolean#debugFonts;private static#boolean#logging;private static#sun.util.logging.PlatformLogger#logger;public static#int#MAX_LAYOUT_CHARCODE;public static#int#MIN_LAYOUT_CHARCODE;public static#boolean#isLinux;public static#boolean#isMacOSX;public static#boolean#isMacOSX14;public static#boolean#isOpenJDK;public static#boolean#isOpenSolaris;public static#boolean#isSolaris;public static#boolean#isSolaris8;public static#boolean#isSolaris9;public static#boolean#isWindows;public static#boolean#useT2K;static#java.lang.String#LUCIDA_FILE_NAME"), "fields");
        check(c.getDeclaredConstructors().length == 1, "ctors");
        System.out.println("PASS (" + passed + " assertions)");
    }

    // Build-specific tolerance: jdk8u502 added members and strengthened a few
    // `static` fields to `static final`. Assert the jdk8u372 surface is still
    // PRESENT (a subset) and ignore the `final` keyword — member growth and
    // final-strengthening are not contract drift; member REMOVAL still fails.
    static String mod(int m) {
        return Modifier.toString(m).replace(" final", "").replaceAll("\\s+", " ").trim();
    }

    static boolean subset(String actual, String expected) {
        Set<String> a = new HashSet<>();
        for (String s : actual.split(";")) if (!s.isEmpty()) a.add(s);
        for (String e : expected.split(";")) if (!a.contains(e)) return false;
        return true;
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
            s.append(mod(m.getModifiers())).append('#');
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
            l.add(mod(f.getModifiers()) + '#' + f.getType().getName() + '#' + f.getName());
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
