/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.awt.X11InputMethodBase conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main X11InputMethodBaseTest
 */

import java.util.*;
import java.lang.reflect.*;

public class X11InputMethodBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11InputMethodBase", false, X11InputMethodBaseTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11InputMethodBase".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.awt.im.InputMethodAdapter".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#void#dispatchCommittedText(java.lang.String,long);#void#flushText();abstract#void#dispatchComposedText(java.lang.String,[I,int,int,int,long);private native#boolean#isCompositionEnabledNative();private native#java.lang.String#resetXIC();private static native#void#initIDs();private synchronized#boolean#initXIM();private#void#dispatchCommittedText(java.lang.String);private#void#postInputMethodEvent(int,java.text.AttributedCharacterIterator,int,java.awt.font.TextHitInfo,java.awt.font.TextHitInfo);protected abstract#boolean#createXIC();protected abstract#boolean#openXIM();protected abstract#java.awt.Container#getParent(java.awt.Component);protected abstract#java.awt.peer.ComponentPeer#getPeer(java.awt.Component);protected abstract#void#awtLock();protected abstract#void#awtUnlock();protected abstract#void#disposeImpl();protected abstract#void#resetCompositionState();protected abstract#void#setXICFocus(java.awt.peer.ComponentPeer,boolean,boolean);protected final#void#resetXICifneeded();protected native#boolean#setCompositionEnabledNative(boolean);protected native#void#disposeXIC();protected native#void#turnoffStatusWindow();protected#boolean#getCompositionState();protected#boolean#isDisposed();protected#java.awt.Window#getClientComponentWindow();protected#java.awt.im.InputMethodHighlight#convertVisualFeedbackToHighlight(int);protected#void#postInputMethodEvent(int,java.text.AttributedCharacterIterator,int,java.awt.font.TextHitInfo,java.awt.font.TextHitInfo,long);protected#void#setAWTFocussedComponent(java.awt.Component);protected#void#stopListening();public abstract#void#activate();public abstract#void#deactivate(boolean);public abstract#void#hideWindows();public abstract#void#setCompositionEnabled(boolean);public final#void#dispose();public static#java.util.Map#mapInputMethodHighlight(java.awt.im.InputMethodHighlight);public synchronized#void#removeNotify();public#boolean#isCompositionEnabled();public#boolean#setLocale(java.util.Locale);public#java.lang.Object#getControlObject();public#java.lang.String#getNativeInputMethodInfo();public#java.util.Locale#getLocale();public#void#disableInputMethod();public#void#dispatchEvent(java.awt.AWTEvent);public#void#endComposition();public#void#setCharacterSubsets([Ljava.lang.Character$Subset;);public#void#setInputMethodContext(java.awt.im.spi.InputMethodContext)".equals(methods(c)), "methods");
        check("private static final#int#XIMHighlight;private static final#int#XIMPrimary;private static final#int#XIMReverse;private static final#int#XIMSecondary;private static final#int#XIMTertiary;private static final#int#XIMUnderline;private static#[Ljava.util.Map;#highlightStyles;private static#boolean#isXIMOpened;private#java.lang.ref.WeakReference#needResetXICClient;private#java.util.Locale#locale;protected static final#int#INITIAL_SIZE;protected static final#int#XIMVisibleCenter;protected static final#int#XIMVisibleMask;protected static final#int#XIMVisibleToBackward;protected static final#int#XIMVisibleToForward;protected static final#sun.util.logging.PlatformLogger#log;protected transient#long#pData;protected#boolean#compositionEnableSupported;protected#boolean#disposed;protected#boolean#isActive;protected#boolean#isLastTemporary;protected#boolean#isLastXICActive;protected#boolean#needResetXIC;protected#boolean#savedCompositionState;protected#java.awt.Component#awtFocussedComponent;protected#java.awt.Component#lastXICFocussedComponent;protected#java.awt.Container#clientComponentWindow;protected#java.lang.String#committedText;protected#java.lang.StringBuffer#composedText;protected#sun.awt.X11InputMethodBase$IntBuffer#rawFeedbacks".equals(fields(c)), "fields");
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
