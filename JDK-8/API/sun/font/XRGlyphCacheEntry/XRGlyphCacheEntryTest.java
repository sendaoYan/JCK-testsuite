/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.font.XRGlyphCacheEntry conformance: reflection (no init, native/display-heavy).
 * @run main XRGlyphCacheEntryTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XRGlyphCacheEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.XRGlyphCacheEntry", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.XRGlyphCacheEntry".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static#int#getGlyphID(long);public static#void#setGlyphID(long,int);public#boolean#isGrayscale(boolean);public#boolean#isPinned();public#float#getTopLeftXOffset();public#float#getTopLeftYOffset();public#float#getXAdvance();public#float#getYAdvance();public#int#getDestinationRowBytes(boolean);public#int#getGlyphDataLenth(boolean);public#int#getGlyphID();public#int#getGlyphSet();public#int#getHeight();public#int#getLastUsed();public#int#getPaddedWidth(boolean);public#int#getPixelCnt();public#int#getSourceRowBytes();public#int#getWidth();public#int#getXOff();public#int#getYOff();public#long#getGlyphInfoPtr();public#void#setGlyphID(int);public#void#setGlyphSet(int);public#void#setLastUsed(int);public#void#setPinned();public#void#setUnpinned();public#void#writePixelData(java.io.ByteArrayOutputStream,boolean)".equals(methods(c)), "methods");
        check("#boolean#pinned;#int#glyphSet;#int#lastUsed;#int#xOff;#int#yOff;#long#glyphInfoPtr".equals(fields(c)), "fields");
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
