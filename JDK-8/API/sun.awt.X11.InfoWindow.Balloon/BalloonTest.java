/*
 * @test
 * @summary sun.awt.X11.InfoWindow.Balloon conformance: reflection (no init, native/display-heavy).
 * @run main BalloonTest
 */

import java.util.*;
import java.lang.reflect.*;

public class BalloonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.InfoWindow$Balloon", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.InfoWindow$Balloon".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.X11.InfoWindow".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#void#_display(java.lang.String,java.lang.String,java.lang.String);private#void#loadGtkImages();public#void#display(java.lang.String,java.lang.String,java.lang.String);public#void#dispose()".equals(methods(c)), "methods");
        check("private final#java.lang.Object#target;private final#sun.awt.X11.InfoWindow$Balloon$LiveArguments#liveArguments;private static final#int#BALLOON_ICON_HEIGHT;private static final#int#BALLOON_ICON_WIDTH;private static final#int#BALLOON_SHOW_TIME;private static final#int#BALLOON_TEXT_MAX_LENGTH;private static final#int#BALLOON_TRAY_ICON_INDENT;private static final#int#BALLOON_WORD_LINE_MAX_COUNT;private static final#int#BALLOON_WORD_LINE_MAX_LENGTH;private static final#java.awt.Color#BALLOON_CAPTION_BACKGROUND_COLOR;private static final#java.awt.Font#BALLOON_CAPTION_FONT;private#[Ljava.awt.Label;#lineLabels;private#boolean#gtkImagesLoaded;private#java.awt.Button#closeButton;private#java.awt.Image#errorImage;private#java.awt.Image#iconImage;private#java.awt.Image#infoImage;private#java.awt.Image#warnImage;private#java.awt.Label#captionLabel;private#java.awt.Panel#captionPanel;private#java.awt.Panel#mainPanel;private#java.awt.Panel#textPanel;private#sun.awt.X11.InfoWindow$Balloon$ActionPerformer#ap;private#sun.awt.X11.InfoWindow$Balloon$Displayer#displayer;private#sun.awt.X11.XTrayIconPeer$IconCanvas#iconCanvas".equals(fields(c)), "fields");
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
