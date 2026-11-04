/*
 * @test
 * @summary sun.java2d.pipe.RenderBuffer conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.pipe:+open
 * @run main RenderBufferTest
 */

import java.util.*;
import java.lang.reflect.*;

public class RenderBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.pipe.RenderBuffer", false, RenderBufferTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.pipe.RenderBuffer".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public final#int#capacity();public final#int#position();public final#int#remaining();public final#long#getAddress();public final#sun.java2d.pipe.RenderBuffer#putByte(byte);public final#sun.java2d.pipe.RenderBuffer#putDouble(double);public final#sun.java2d.pipe.RenderBuffer#putFloat(float);public final#sun.java2d.pipe.RenderBuffer#putInt(int);public final#sun.java2d.pipe.RenderBuffer#putInt(int,int);public final#sun.java2d.pipe.RenderBuffer#putLong(long);public final#sun.java2d.pipe.RenderBuffer#putShort(short);public final#sun.java2d.pipe.RenderBuffer#skip(int);public final#void#clear();public final#void#position(int);public static#sun.java2d.pipe.RenderBuffer#allocate(int);public#sun.java2d.pipe.RenderBuffer#put([B);public#sun.java2d.pipe.RenderBuffer#put([B,int,int);public#sun.java2d.pipe.RenderBuffer#put([F);public#sun.java2d.pipe.RenderBuffer#put([F,int,int);public#sun.java2d.pipe.RenderBuffer#put([I);public#sun.java2d.pipe.RenderBuffer#put([I,int,int);public#sun.java2d.pipe.RenderBuffer#put([J);public#sun.java2d.pipe.RenderBuffer#put([J,int,int);public#sun.java2d.pipe.RenderBuffer#put([S);public#sun.java2d.pipe.RenderBuffer#put([S,int,int)".equals(methods(c)), "methods");
        check("private final#java.lang.foreign.MemorySegment#segment;private static final#int#COPY_FROM_ARRAY_THRESHOLD;private static final#int#SIZEOF_BYTE;private static final#int#SIZEOF_DOUBLE;private static final#int#SIZEOF_FLOAT;private static final#int#SIZEOF_INT;private static final#int#SIZEOF_LONG;private static final#int#SIZEOF_SHORT;private#int#curOffset".equals(fields(c)), "fields");
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
