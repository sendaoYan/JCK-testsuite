/*
 * @test
 * @summary sun.awt.X11InputMethod conformance: reflection (no init, native/display-heavy).
 * @run main X11InputMethodTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11InputMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11InputMethod", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11InputMethod".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("sun.awt.im.InputMethodAdapter".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#void#dispatchCommittedText(java.lang.String,long);#void#dispatchComposedText(java.lang.String,[I,int,int,int,long);#void#flushText();private native#boolean#isCompositionEnabledNative();private native#boolean#setCompositionEnabledNative(boolean);private native#void#disposeXIC();private native#void#turnoffStatusWindow();private static native#void#initIDs();private synchronized#boolean#initXIM();private#boolean#getCompositionState();private#java.awt.Window#getClientComponentWindow();private#java.awt.im.InputMethodHighlight#convertVisualFeedbackToHighlight(int);private#void#dispatchCommittedText(java.lang.String);private#void#postInputMethodEvent(int,java.text.AttributedCharacterIterator,int,java.awt.font.TextHitInfo,java.awt.font.TextHitInfo);private#void#postInputMethodEvent(int,java.text.AttributedCharacterIterator,int,java.awt.font.TextHitInfo,java.awt.font.TextHitInfo,long);private#void#resetCompositionState();protected abstract#boolean#createXIC();protected abstract#boolean#openXIM();protected abstract#java.awt.Container#getParent(java.awt.Component);protected abstract#java.awt.peer.ComponentPeer#getPeer(java.awt.Component);protected abstract#void#awtLock();protected abstract#void#awtUnlock();protected abstract#void#setXICFocus(java.awt.peer.ComponentPeer,boolean,boolean);protected final#void#resetXICifneeded();protected native#java.lang.String#resetXIC();protected synchronized#void#disposeImpl();protected#boolean#isDisposed();protected#void#finalize();protected#void#setAWTFocussedComponent(java.awt.Component);protected#void#stopListening();public final#void#dispose();public static#java.util.Map#mapInputMethodHighlight(java.awt.im.InputMethodHighlight);public synchronized#void#activate();public synchronized#void#deactivate(boolean);public synchronized#void#removeNotify();public#boolean#isCompositionEnabled();public#boolean#setLocale(java.util.Locale);public#java.lang.Object#getControlObject();public#java.lang.String#getNativeInputMethodInfo();public#java.util.Locale#getLocale();public#void#disableInputMethod();public#void#dispatchEvent(java.awt.AWTEvent);public#void#endComposition();public#void#hideWindows();public#void#setCharacterSubsets([Ljava.lang.Character$Subset;);public#void#setCompositionEnabled(boolean);public#void#setInputMethodContext(java.awt.im.spi.InputMethodContext)".equals(methods(c)), "methods");
        check("private static final#int#INITIAL_SIZE;private static final#int#XIMHighlight;private static final#int#XIMPrimary;private static final#int#XIMReverse;private static final#int#XIMSecondary;private static final#int#XIMTertiary;private static final#int#XIMUnderline;private static final#int#XIMVisibleCenter;private static final#int#XIMVisibleMask;private static final#int#XIMVisibleToBackward;private static final#int#XIMVisibleToForward;private static final#sun.util.logging.PlatformLogger#log;private static#[Ljava.util.Map;#highlightStyles;private static#boolean#isXIMOpened;private transient#long#pData;private#boolean#compositionEnableSupported;private#boolean#disposed;private#boolean#isActive;private#boolean#isActiveClient;private#boolean#isLastTemporary;private#boolean#isLastXICActive;private#boolean#needResetXIC;private#boolean#savedCompositionState;private#java.awt.Component#awtFocussedComponent;private#java.awt.Component#lastXICFocussedComponent;private#java.lang.String#committedText;private#java.lang.StringBuffer#composedText;private#java.lang.ref.WeakReference#needResetXICClient;private#java.util.Locale#locale;private#sun.awt.X11InputMethod$IntBuffer#rawFeedbacks;protected#java.awt.Container#clientComponentWindow".equals(fields(c)), "fields");
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
