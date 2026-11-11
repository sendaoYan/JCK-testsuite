/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.font.FcFontConfiguration conformance: reflection (no init, native/display-heavy).
 * @run main FcFontConfigurationTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FcFontConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FcFontConfiguration", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.FcFontConfiguration".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.FontConfiguration".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#void#warning(java.lang.String);private#[Lsun.font.FontConfigManager$FontConfigFont;#getFcFontList([Lsun.font.FontConfigManager$FcCompFont;,java.lang.String,int);private#java.io.File#getFcInfoFile();private#java.lang.String#getVersionString(java.io.File);private#void#readFcInfo();private#void#writeFcInfo();protected#[Lsun.awt.FontDescriptor;#buildFontDescriptors(int,int);protected#java.lang.String#getEncoding(java.lang.String,java.lang.String);protected#java.lang.String#getFaceNameFromComponentFontName(java.lang.String);protected#java.lang.String#getFileNameFromComponentFontName(java.lang.String);protected#java.nio.charset.Charset#getDefaultFontCharset(java.lang.String);protected#void#initReorderMap();protected#void#setOsNameAndVersion();public synchronized#boolean#init();public#[Ljava.lang.String;#getPlatformFontNames();public#[Lsun.font.CompositeFontDescriptor;#get2DCompositeFontInfo();public#boolean#needToSearchForFile(java.lang.String);public#int#getNumberCoreFonts();public#java.lang.String#getExtraFontPath();public#java.lang.String#getFallbackFamilyName(java.lang.String,java.lang.String);public#java.lang.String#getFileNameFromPlatformName(java.lang.String)".equals(methods(c)), "methods");
        check("private static final#java.lang.String#fileVersion;private#[Lsun.font.FontConfigManager$FcCompFont;#fcCompFonts;private#java.lang.String#fcInfoFileName".equals(fields(c)), "fields");
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
