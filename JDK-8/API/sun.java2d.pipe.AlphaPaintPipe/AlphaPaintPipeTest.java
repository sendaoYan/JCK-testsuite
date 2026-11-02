/*
 * @test
 * @summary sun.java2d.pipe.AlphaPaintPipe conformance: reflection (no init, native/display-heavy).
 * @run main AlphaPaintPipeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AlphaPaintPipeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.AlphaPaintPipe", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.AlphaPaintPipe".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.pipe.CompositePipe".equals(interfaces(c)), "interfaces");
        check("public#boolean#needTile(java.lang.Object,int,int,int,int);public#java.lang.Object#startSequence(sun.java2d.SunGraphics2D,java.awt.Shape,java.awt.Rectangle,[I);public#void#endSequence(java.lang.Object);public#void#renderPathTile(java.lang.Object,[B,int,int,int,int,int,int);public#void#skipTile(java.lang.Object,int,int)".equals(methods(c)), "methods");
        check("private static final#int#TILE_SIZE;static#java.lang.ref.WeakReference#cachedLastColorModel;static#java.lang.ref.WeakReference#cachedLastData;static#java.lang.ref.WeakReference#cachedLastRaster".equals(fields(c)), "fields");
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
