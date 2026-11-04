/*
 * @test
 * @summary sun.font.StrikeCache conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main StrikeCacheTest
 */

import java.util.*;
import java.lang.reflect.*;

public class StrikeCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.StrikeCache", false, StrikeCacheTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.StrikeCache".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static native#void#freeIntMemory([I,long);private static native#void#freeLongMemory([J,long);private static#boolean#longAddresses();private static#java.lang.invoke.VarHandle#getVarHandle(java.lang.foreign.StructLayout,java.lang.String);private static#void#doDispose(sun.font.FontStrikeDisposer);private static#void#freeCachedIntMemory([I,long);private static#void#freeCachedLongMemory([J,long);private static#void#initStatic();private static#void#notifyDisposeListeners(java.util.ArrayList);public static#java.lang.ref.Reference#getStrikeRef(sun.font.FontStrike);public static#java.lang.ref.Reference#getStrikeRef(sun.font.FontStrike,boolean);public static#void#addGlyphDisposedListener(sun.font.GlyphDisposedListener);static final#[B#getGlyphPixelBytes(long);static final#byte#getGlyphManaged(long);static final#byte#getPixelByte(java.lang.foreign.MemorySegment,long);static final#char#getGlyphHeight(long);static final#char#getGlyphRowBytes(long);static final#char#getGlyphWidth(long);static final#float#getGlyphTopLeftX(long);static final#float#getGlyphTopLeftY(long);static final#float#getGlyphXAdvance(long);static final#float#getGlyphYAdvance(long);static final#java.lang.foreign.MemorySegment#getGlyphPixelData(long);static final#long#getGlyphCellInfo(long);static final#long#getGlyphImagePtr(long);static final#void#setGlyphCellInfo(long,long);static final#void#setGlyphXAdvance(long,float);static native#long#getInvisibleGlyphPtr();static native#void#freeIntPointer(int);static native#void#freeLongPointer(long);static#void#disposeStrike(sun.font.FontStrikeDisposer);static#void#refStrike(sun.font.FontStrike)".equals(methods(c)), "methods");
        check("private static final#java.lang.invoke.VarHandle#cellInfoHandle;private static final#java.lang.invoke.VarHandle#heightHandle;private static final#java.lang.invoke.VarHandle#imageHandle;private static final#java.lang.invoke.VarHandle#managedHandle;private static final#java.lang.invoke.VarHandle#rowBytesHandle;private static final#java.lang.invoke.VarHandle#topLeftXHandle;private static final#java.lang.invoke.VarHandle#topLeftYHandle;private static final#java.lang.invoke.VarHandle#widthHandle;private static final#java.lang.invoke.VarHandle#xAdvanceHandle;private static final#java.lang.invoke.VarHandle#yAdvanceHandle;private static final#long#GLYPHIMAGESIZE;public static final#java.lang.foreign.StructLayout#GlyphImageLayout;static final#int#nativeAddressSize;static final#long#invisibleGlyphPtr;static#[Lsun.font.FontStrike;#recentStrikes;static#boolean#cacheRefTypeWeak;static#int#MINSTRIKES;static#int#recentStrikeIndex;static#java.lang.ref.ReferenceQueue#refQueue;static#java.util.ArrayList#disposeListeners".equals(fields(c)), "fields");
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
