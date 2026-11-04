/*
 * @test
 * @summary sun.awt.motif.MFontConfiguration conformance: reflection (no init, native/display-heavy).
 * @run main MFontConfigurationTest
 */

import java.util.*;
import java.lang.reflect.*;

public class MFontConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.motif.MFontConfiguration", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.motif.MFontConfiguration".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.FontConfiguration".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#java.lang.String#getVersionString(java.io.File);private#void#initReorderMapForLinux();private#void#initReorderMapForSolaris();private#void#initTables();protected#java.lang.String#getEncoding(java.lang.String,java.lang.String);protected#java.lang.String#getFaceNameFromComponentFontName(java.lang.String);protected#java.lang.String#getFileNameFromComponentFontName(java.lang.String);protected#java.lang.String#mapFileName(java.lang.String);protected#java.nio.charset.Charset#getDefaultFontCharset(java.lang.String);protected#void#initReorderMap();protected#void#setOsNameAndVersion();public#java.lang.String#getFallbackFamilyName(java.lang.String,java.lang.String);public#java.util.HashSet#getAWTFontPathSet()".equals(methods(c)), "methods");
        check("private static final#java.lang.String#fontsDirPrefix;private static#java.util.HashMap#encodingMap;private static#sun.awt.FontConfiguration#fontConfig;private static#sun.util.logging.PlatformLogger#logger".equals(fields(c)), "fields");
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
