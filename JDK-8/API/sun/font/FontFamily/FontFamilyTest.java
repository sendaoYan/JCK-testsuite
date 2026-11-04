/*
 * @test
 * @summary sun.font.FontFamily conformance: reflection (no init, native/display-heavy).
 * @run main FontFamilyTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FontFamilyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FontFamily", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.FontFamily".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#sun.font.Font2D#getClosestStyle(int);private#boolean#closerWeight(sun.font.Font2D,sun.font.Font2D,int);private#boolean#isFromSameSource(sun.font.Font2D);private#boolean#preferredWidth(sun.font.Font2D);public static synchronized#sun.font.FontFamily#getLocaleFamily(java.lang.String);public static#[Ljava.lang.String;#getAllFamilyNames();public static#[Lsun.font.FontFamily;#getAllFontFamilies();public static#sun.font.FontFamily#getFamily(java.lang.String);public#int#getRank();public#java.lang.String#getFamilyName();public#java.lang.String#toString();public#sun.font.Font2D#getFont(int);public#sun.font.Font2D#getFontWithExactStyleMatch(int);public#void#setFont(sun.font.Font2D,int);static synchronized#void#addLocaleNames(sun.font.FontFamily,[Ljava.lang.String;);static#void#remove(sun.font.Font2D)".equals(methods(c)), "methods");
        check("private static#java.util.HashMap#allLocaleNames;private static#java.util.concurrent.ConcurrentHashMap#familyNameMap;private#int#familyWidth;protected#boolean#logicalFont;protected#int#familyRank;protected#java.lang.String#familyName;protected#sun.font.Font2D#bold;protected#sun.font.Font2D#bolditalic;protected#sun.font.Font2D#italic;protected#sun.font.Font2D#plain".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
