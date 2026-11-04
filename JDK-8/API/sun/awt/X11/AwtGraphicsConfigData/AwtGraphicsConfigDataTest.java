/*
 * @test
 * @summary sun.awt.X11.AwtGraphicsConfigData conformance: reflection (no init, native/display-heavy).
 * @run main AwtGraphicsConfigDataTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AwtGraphicsConfigDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.AwtGraphicsConfigData", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.AwtGraphicsConfigData".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.XWrapperBase".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#java.lang.String#getFieldsAsString();#java.lang.String#getName();public static#int#getSize();public#int#getDataSize();public#int#get_awt_depth();public#int#get_awt_num_colors();public#int#get_isTranslucencySupported();public#int#get_monoPixmapHeight();public#int#get_monoPixmapWidth();public#int#get_pixelStride();public#long#getPData();public#long#get_AwtColorMatch();public#long#get_AwtColorMatch(int);public#long#get_awtImage();public#long#get_awt_cmap();public#long#get_color_data();public#long#get_glxInfo();public#long#get_glxInfo(int);public#long#get_monoImage();public#long#get_monoImage(int);public#long#get_monoPixmap();public#long#get_monoPixmapGC();public#sun.awt.X11.ColorData#get_color_data(int);public#sun.awt.X11.XRenderPictFormat#get_renderPictFormat();public#sun.awt.X11.XVisualInfo#get_awt_visInfo();public#sun.awt.X11.awtImageData#get_awtImage(int);public#void#dispose();public#void#set_AwtColorMatch(long);public#void#set_awtImage(long);public#void#set_awt_cmap(long);public#void#set_awt_depth(int);public#void#set_awt_num_colors(int);public#void#set_color_data(long);public#void#set_glxInfo(long);public#void#set_isTranslucencySupported(int);public#void#set_monoImage(long);public#void#set_monoPixmap(long);public#void#set_monoPixmapGC(long);public#void#set_monoPixmapHeight(int);public#void#set_monoPixmapWidth(int);public#void#set_pixelStride(int)".equals(methods(c)), "methods");
        check("#long#pData;private final#boolean#should_free_memory;private#sun.misc.Unsafe#unsafe".equals(fields(c)), "fields");
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
