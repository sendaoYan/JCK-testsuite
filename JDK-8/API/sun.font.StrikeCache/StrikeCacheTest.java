/*
 * @test
 * @summary sun.font.StrikeCache conformance: reflection (no init, native/display-heavy).
 * @run main StrikeCacheTest
 */

import java.util.*;
import java.lang.reflect.*;

public class StrikeCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.StrikeCache", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.StrikeCache".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static final#void#doDispose(sun.font.FontStrikeDisposer);private static native#void#freeIntMemory([I,long);private static native#void#freeLongMemory([J,long);private static#boolean#longAddresses();private static#void#freeCachedIntMemory([I,long);private static#void#freeCachedLongMemory([J,long);private static#void#notifyDisposeListeners(java.util.ArrayList);public static#java.lang.ref.Reference#getStrikeRef(sun.font.FontStrike);public static#java.lang.ref.Reference#getStrikeRef(sun.font.FontStrike,boolean);public static#void#addGlyphDisposedListener(sun.font.GlyphDisposedListener);static native#void#freeIntPointer(int);static native#void#freeLongPointer(long);static native#void#getGlyphCacheDescription([J);static#void#disposeStrike(sun.font.FontStrikeDisposer);static#void#refStrike(sun.font.FontStrike)".equals(methods(c)), "methods");
        check("static final#sun.misc.Unsafe#unsafe;static#[Lsun.font.FontStrike;#recentStrikes;static#boolean#cacheRefTypeWeak;static#int#MINSTRIKES;static#int#boundsOffset;static#int#cacheCellOffset;static#int#glyphInfoSize;static#int#heightOffset;static#int#managedOffset;static#int#nativeAddressSize;static#int#pixelDataOffset;static#int#recentStrikeIndex;static#int#rowBytesOffset;static#int#topLeftXOffset;static#int#topLeftYOffset;static#int#widthOffset;static#int#xAdvanceOffset;static#int#yAdvanceOffset;static#java.lang.ref.ReferenceQueue#refQueue;static#java.util.ArrayList#disposeListeners;static#long#invisibleGlyphPtr".equals(fields(c)), "fields");
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
