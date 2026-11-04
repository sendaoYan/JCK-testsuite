/*
 * @test
 * @summary sun.font.FileFont conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main FileFontTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FileFontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.FileFont", false, FileFontTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.FileFont".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.font.PhysicalFont".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#float#getGlyphAdvance(long,int);#java.awt.geom.GeneralPath#getGlyphOutline(long,int,float,float);#java.awt.geom.GeneralPath#getGlyphVectorOutline(long,[I,int,float,float);#java.awt.geom.Rectangle2D$Float#getGlyphOutlineBounds(long,int);#long#getGlyphImage(long,int);#sun.font.FontStrike#createStrike(sun.font.FontStrikeDesc);#sun.font.StrikeMetrics#getFontMetrics(long);#void#getGlyphMetrics(long,int,java.awt.geom.Point2D$Float);abstract#java.nio.ByteBuffer#readBlock(int,int);protected abstract#sun.font.FontScaler#getScaler();protected abstract#void#close();protected#java.lang.String#getPublicFileName();protected#long#getUnitsPerEm();public#boolean#canDoStyle(int);static#void#setFileToRemove(java.util.List,java.io.File,int);synchronized#void#deregisterFontAndClearStrikeCache()".equals(methods(c)), "methods");
        check("protected#[C#glyphToCharMap;protected#[Lsun.font.NativeFont;#nativeFonts;protected#boolean#useJavaRasterizer;protected#int#fileSize;protected#sun.font.FontScaler#scaler".equals(fields(c)), "fields");
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
