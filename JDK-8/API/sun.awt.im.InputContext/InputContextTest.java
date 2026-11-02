/*
 * @test
 * @summary sun.awt.im.InputContext conformance: reflection (no init, native/display-heavy).
 * @run main InputContextTest
 */

import java.util.*;
import java.lang.reflect.*;

public class InputContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.im.InputContext", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.im.InputContext".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.awt.im.InputContext".equals(c.getSuperclass().getName()), "superclass");
        check("java.awt.event.ComponentListener;java.awt.event.WindowListener".equals(interfaces(c)), "interfaces");
        check("#java.awt.Component#getClientComponent();#sun.awt.im.InputMethodLocator#getInputMethodLocator();private synchronized#java.awt.im.spi.InputMethod#getInputMethod();private synchronized#void#addClientWindowListeners();private synchronized#void#notifyClientWindowChange(java.awt.Window);private synchronized#void#removeClientWindowListeners();private#boolean#addedClientWindowListeners();private#boolean#checkInputMethodSelectionKey(java.awt.event.KeyEvent);private#java.awt.AWTKeyStroke#getInputMethodSelectionKeyStroke(java.util.prefs.Preferences);private#java.awt.im.spi.InputMethod#getInputMethodInstance();private#void#activateInputMethod(boolean);private#void#deactivateInputMethod(boolean);private#void#focusGained(java.awt.Component);private#void#focusLost(java.awt.Component,boolean);private#void#initializeInputMethodSelectionKey();private#void#logCreationFailed(java.lang.Throwable);public synchronized#boolean#selectInputMethod(java.util.Locale);public synchronized#java.lang.Object#getInputMethodControlObject();public synchronized#void#dispose();public synchronized#void#endComposition();public synchronized#void#reconvert();public synchronized#void#removeNotify(java.awt.Component);public#boolean#isCompositionEnabled();public#java.lang.String#getInputMethodInfo();public#java.util.Locale#getLocale();public#void#componentHidden(java.awt.event.ComponentEvent);public#void#componentMoved(java.awt.event.ComponentEvent);public#void#componentResized(java.awt.event.ComponentEvent);public#void#componentShown(java.awt.event.ComponentEvent);public#void#disableNativeIM();public#void#dispatchEvent(java.awt.AWTEvent);public#void#setCharacterSubsets([Ljava.lang.Character$Subset;);public#void#setCompositionEnabled(boolean);public#void#windowActivated(java.awt.event.WindowEvent);public#void#windowClosed(java.awt.event.WindowEvent);public#void#windowClosing(java.awt.event.WindowEvent);public#void#windowDeactivated(java.awt.event.WindowEvent);public#void#windowDeiconified(java.awt.event.WindowEvent);public#void#windowIconified(java.awt.event.WindowEvent);public#void#windowOpened(java.awt.event.WindowEvent);static#java.awt.Window#getComponentWindow(java.awt.Component);synchronized#void#changeInputMethod(sun.awt.im.InputMethodLocator);synchronized#void#enableClientWindowNotification(java.awt.im.spi.InputMethod,boolean)".equals(methods(c)), "methods");
        check("private static final#java.lang.String#inputMethodSelectionKeyCodeName;private static final#java.lang.String#inputMethodSelectionKeyModifiersName;private static final#java.lang.String#inputMethodSelectionKeyPath;private static final#sun.util.logging.PlatformLogger#log;private static#boolean#inputMethodSelectionKeyInitialized;private static#java.awt.AWTKeyStroke#inputMethodSelectionKey;private static#java.awt.im.spi.InputMethod#previousInputMethod;private static#sun.awt.im.InputContext#inputMethodWindowContext;private#[Ljava.lang.Character$Subset;#characterSubsets;private#boolean#clientWindowNotificationEnabled;private#boolean#compositionAreaHidden;private#boolean#inputMethodCreationFailed;private#boolean#isInputMethodActive;private#java.awt.Component#awtFocussedComponent;private#java.awt.Component#currentClientComponent;private#java.awt.Rectangle#clientWindowLocation;private#java.awt.Window#clientWindowListened;private#java.awt.im.spi.InputMethod#inputMethod;private#java.util.HashMap#perInputMethodState;private#java.util.HashMap#usedInputMethods;private#sun.awt.im.InputMethodLocator#inputMethodLocator".equals(fields(c)), "fields");
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
