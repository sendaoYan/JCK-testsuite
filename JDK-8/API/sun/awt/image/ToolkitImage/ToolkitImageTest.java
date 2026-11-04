/*
 * @test
 * @summary sun.awt.image.ToolkitImage conformance: reflection (no init, native/display-heavy).
 * @run main ToolkitImageTest
 */

import java.util.*;
import java.lang.reflect.*;

public class ToolkitImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.image.ToolkitImage", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.image.ToolkitImage".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.Image".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#void#setDimensions(int,int);#void#setProperties(java.util.Hashtable);private synchronized#void#addWatcher(java.awt.image.ImageObserver,boolean);private synchronized#void#reconstruct(int);protected#sun.awt.image.ImageRepresentation#makeImageRep();public synchronized#int#getHeight(java.awt.image.ImageObserver);public synchronized#int#getWidth(java.awt.image.ImageObserver);public synchronized#sun.awt.image.ImageRepresentation#getImageRep();public#boolean#hasError();public#int#check(java.awt.image.ImageObserver);public#int#getHeight();public#int#getWidth();public#java.awt.Graphics#getGraphics();public#java.awt.image.BufferedImage#getBufferedImage();public#java.awt.image.ColorModel#getColorModel();public#java.awt.image.ImageProducer#getSource();public#java.lang.Object#getProperty(java.lang.String,java.awt.image.ImageObserver);public#void#flush();public#void#preload(java.awt.image.ImageObserver);public#void#setAccelerationPriority(float);synchronized#void#addInfo(int);synchronized#void#infoDone(int)".equals(methods(c)), "methods");
        check("#java.awt.image.ImageProducer#source;#sun.awt.image.ImageRepresentation#imagerep;#sun.awt.image.InputStreamImageSource#src;private#int#availinfo;private#int#height;private#int#width;private#java.util.Hashtable#properties".equals(fields(c)), "fields");
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
