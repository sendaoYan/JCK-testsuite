/*
 * @test
 * @summary sun.awt.im.CompositionArea conformance: reflection (no init, native/display-heavy).
 * @run main CompositionAreaTest
 */

import java.util.*;
import java.lang.reflect.*;

public class CompositionAreaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.im.CompositionArea", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.im.CompositionArea".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("javax.swing.JPanel".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.event.InputMethodListener".equals(interfaces(c)), "interfaces");
        check("#boolean#isCompositionAreaVisible();#java.awt.Rectangle#getTextLocation(java.awt.font.TextHitInfo);#java.awt.font.TextHitInfo#getLocationOffset(int,int);#void#setCaret(java.awt.font.TextHitInfo);#void#setCompositionAreaUndecorated(boolean);#void#setCompositionAreaVisible(boolean);#void#setText(java.text.AttributedCharacterIterator,java.awt.font.TextHitInfo);#void#updateWindowLocation();private#java.awt.Rectangle#getCaretRectangle(java.awt.font.TextHitInfo);public#java.awt.im.InputMethodRequests#getInputMethodRequests();public#void#caretPositionChanged(java.awt.event.InputMethodEvent);public#void#inputMethodTextChanged(java.awt.event.InputMethodEvent);public#void#paint(java.awt.Graphics);synchronized#void#setHandlerInfo(sun.awt.im.CompositionAreaHandler,sun.awt.im.InputContext)".equals(methods(c)), "methods");
        check("private static final#int#HEIGHT_MARGIN;private static final#int#PASSIVE_WIDTH;private static final#int#TEXT_ORIGIN_X;private static final#int#TEXT_ORIGIN_Y;private static final#int#WIDTH_MARGIN;private static final#long#serialVersionUID;private#java.awt.font.TextHitInfo#caret;private#java.awt.font.TextLayout#composedTextLayout;private#javax.swing.JFrame#compositionWindow;private#sun.awt.im.CompositionAreaHandler#handler".equals(fields(c)), "fields");
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
