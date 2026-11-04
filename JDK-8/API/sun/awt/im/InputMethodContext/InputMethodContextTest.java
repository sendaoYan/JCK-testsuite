/*
 * @test
 * @summary sun.awt.im.InputMethodContext conformance: reflection (no init, native/display-heavy).
 * @run main InputMethodContextTest
 */

import java.util.*;
import java.lang.reflect.*;

public class InputMethodContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.im.InputMethodContext", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.im.InputMethodContext".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.im.InputContext".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.im.spi.InputMethodContext".equals(interfaces(c)), "interfaces");
        check("#boolean#isCompositionAreaVisible();#boolean#useBelowTheSpotInput();#void#grabCompositionArea(boolean);#void#releaseCompositionArea();#void#setCompositionAreaUndecorated(boolean);#void#setCompositionAreaVisible(boolean);#void#setInputMethodSupportsBelowTheSpot(boolean);private#boolean#haveActiveClient();private#java.awt.im.InputMethodRequests#getReq();private#sun.awt.im.CompositionAreaHandler#getCompositionAreaHandler(boolean);public#int#getCommittedTextLength();public#int#getInsertPositionOffset();public#java.awt.Rectangle#getTextLocation(java.awt.font.TextHitInfo);public#java.awt.Window#createInputMethodWindow(java.lang.String,boolean);public#java.awt.font.TextHitInfo#getLocationOffset(int,int);public#java.text.AttributedCharacterIterator#cancelLatestCommittedText([Ljava.text.AttributedCharacterIterator$Attribute;);public#java.text.AttributedCharacterIterator#getCommittedText(int,int,[Ljava.text.AttributedCharacterIterator$Attribute;);public#java.text.AttributedCharacterIterator#getSelectedText([Ljava.text.AttributedCharacterIterator$Attribute;);public#javax.swing.JFrame#createInputMethodJFrame(java.lang.String,boolean);public#void#dispatchEvent(java.awt.AWTEvent);public#void#dispatchInputMethodEvent(int,java.text.AttributedCharacterIterator,int,java.awt.font.TextHitInfo,java.awt.font.TextHitInfo);public#void#enableClientWindowNotification(java.awt.im.spi.InputMethod,boolean);static#java.awt.Window#createInputMethodWindow(java.lang.String,sun.awt.im.InputContext,boolean);synchronized#void#dispatchCommittedText(java.awt.Component,java.text.AttributedCharacterIterator,int)".equals(methods(c)), "methods");
        check("private static#boolean#belowTheSpotInputRequested;private#boolean#dispatchingCommittedText;private#boolean#inputMethodSupportsBelowTheSpot;private#java.lang.Object#compositionAreaHandlerLock;private#sun.awt.im.CompositionAreaHandler#compositionAreaHandler".equals(fields(c)), "fields");
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
