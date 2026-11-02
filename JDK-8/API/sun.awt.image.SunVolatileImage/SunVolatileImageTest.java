/*
 * @test
 * @summary sun.awt.image.SunVolatileImage conformance: reflection (no init, native/display-heavy).
 * @run main SunVolatileImageTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunVolatileImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.SunVolatileImage", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.image.SunVolatileImage".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.image.VolatileImage".equals(c.getSuperclass().getName()), "superclass");
        check("sun.java2d.DestSurfaceProvider".equals(interfaces(c)), "interfaces");
        check("private#java.awt.Color#getBackground();private#java.awt.Color#getForeground();private#java.awt.Font#getFont();protected#sun.awt.image.VolatileSurfaceManager#createSurfaceManager(java.lang.Object,java.awt.ImageCapabilities);public#boolean#contentsLost();public#int#getForcedAccelSurfaceType();public#int#getHeight();public#int#getHeight(java.awt.image.ImageObserver);public#int#getWidth();public#int#getWidth(java.awt.image.ImageObserver);public#int#validate(java.awt.GraphicsConfiguration);public#java.awt.Component#getComponent();public#java.awt.Graphics2D#createGraphics();public#java.awt.GraphicsConfiguration#getGraphicsConfig();public#java.awt.ImageCapabilities#getCapabilities();public#java.awt.image.BufferedImage#getBackupImage();public#java.awt.image.BufferedImage#getSnapshot();public#java.lang.Object#getProperty(java.lang.String,java.awt.image.ImageObserver);public#sun.java2d.Surface#getDestSurface();public#void#updateGraphicsConfig()".equals(methods(c)), "methods");
        check("private#int#forcedAccelSurfaceType;private#int#height;private#int#width;private#java.awt.Font#defaultFont;private#java.awt.GraphicsConfiguration#graphicsConfig;protected#java.awt.Component#comp;protected#sun.awt.image.VolatileSurfaceManager#volSurfaceManager".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 5, "ctors");
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
