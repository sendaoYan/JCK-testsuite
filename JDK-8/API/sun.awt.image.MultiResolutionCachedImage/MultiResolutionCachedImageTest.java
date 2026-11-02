/*
 * @test
 * @summary sun.awt.image.MultiResolutionCachedImage conformance: reflection (no init, native/display-heavy).
 * @run main MultiResolutionCachedImageTest
 */

import java.util.*;
import java.lang.reflect.*;

public class MultiResolutionCachedImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.MultiResolutionCachedImage", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.image.MultiResolutionCachedImage".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.image.AbstractMultiResolutionImage".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#int#getInfo(java.awt.Image);private static#void#preload(java.awt.Image,int);private#void#updateInfo(java.awt.image.ImageObserver,int);protected#java.awt.Image#getBaseImage();public#int#getHeight(java.awt.image.ImageObserver);public#int#getWidth(java.awt.image.ImageObserver);public#java.awt.Image#getResolutionVariant(int,int);public#java.lang.Object#getProperty(java.lang.String,java.awt.image.ImageObserver);public#java.util.List#getResolutionVariants();public#sun.awt.image.MultiResolutionCachedImage#map(java.util.function.Function)".equals(methods(c)), "methods");
        check("private final#[Ljava.awt.geom.Dimension2D;#sizes;private final#int#baseImageHeight;private final#int#baseImageWidth;private final#java.util.function.BiFunction#mapper;private#int#availableInfo".equals(fields(c)), "fields");
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
