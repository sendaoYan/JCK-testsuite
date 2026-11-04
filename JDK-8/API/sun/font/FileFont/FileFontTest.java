/*
 * @test
 * @summary sun.font.FileFont conformance: reflection (no init, native/display-heavy).
 * @run main FileFontTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FileFontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FileFont", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.FileFont".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.font.PhysicalFont".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#float#getGlyphAdvance(long,int);#java.awt.geom.GeneralPath#getGlyphOutline(long,int,float,float);#java.awt.geom.GeneralPath#getGlyphVectorOutline(long,[I,int,float,float);#java.awt.geom.Rectangle2D$Float#getGlyphOutlineBounds(long,int);#long#getGlyphImage(long,int);#sun.font.FontStrike#createStrike(sun.font.FontStrikeDesc);#sun.font.StrikeMetrics#getFontMetrics(long);#void#getGlyphMetrics(long,int,java.awt.geom.Point2D$Float);#void#setFileToRemove(java.io.File,sun.font.CreatedFontTracker);abstract#java.nio.ByteBuffer#readBlock(int,int);protected abstract#sun.font.FontScaler#getScaler();protected abstract#void#close();protected#boolean#checkUseNatives();protected#java.lang.String#getPublicFileName();protected#long#getUnitsPerEm();public#boolean#canDoStyle(int);static#void#setFileToRemove(java.lang.Object,java.io.File,sun.font.CreatedFontTracker);synchronized#void#deregisterFontAndClearStrikeCache()".equals(methods(c)), "methods");
        check("protected#[C#glyphToCharMap;protected#[Lsun.font.NativeFont;#nativeFonts;protected#boolean#checkedNatives;protected#boolean#useJavaRasterizer;protected#boolean#useNatives;protected#int#fileSize;protected#sun.font.FontScaler#scaler".equals(fields(c)), "fields");
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
