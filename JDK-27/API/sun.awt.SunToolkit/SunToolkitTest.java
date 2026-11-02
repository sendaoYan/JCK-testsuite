/*
 * @test
 * @summary sun.awt.SunToolkit conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt:+open
 * @run main SunToolkitTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunToolkitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.SunToolkit", false, SunToolkitTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.SunToolkit".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.awt.Toolkit".equals(c.getSuperclass().getName()), "superclass");
        check("sun.awt.ComponentFactory;sun.awt.InputMethodSupport;sun.awt.KeyboardFocusManagerPeerProvider".equals(interfaces(c)), "interfaces");
        check("final#void#notifyModalityChange(int,java.awt.Dialog);private final#boolean#waitForIdle(long);private static synchronized#void#initEQ();private static#boolean#isInstanceOf(java.lang.Class,java.lang.String);private static#boolean#useSystemAAFontSettings();private static#int#getRVSize(int);private static#java.awt.RenderingHints#getDesktopAAHintsByName(java.lang.String);private static#sun.awt.image.ToolkitImage#getResolutionVariant(java.awt.Image);private static#void#initStatic();private#boolean#isEQEmpty();private#boolean#prepareResolutionVariant(java.awt.Image,int,int,java.awt.image.ImageObserver);private#int#checkResolutionVariant(java.awt.Image,int,int,java.awt.image.ImageObserver);private#void#disableBackgroundEraseImpl(java.awt.Component);private#void#fireDesktopFontPropertyChanges();protected abstract#boolean#syncNativeQueue(long);protected final#boolean#isXEmbedServerRequested();protected static#boolean#imageCached(java.lang.String);protected static#boolean#imageCached(java.net.URL);protected static#boolean#imageExists(java.lang.String);protected static#boolean#imageExists(java.net.URL);protected static#java.lang.Object#targetToPeer(java.lang.Object);protected static#sun.awt.LightweightFrame#getLightweightFrame(java.awt.Component);protected static#void#dumpPeers(sun.util.logging.PlatformLogger);protected static#void#targetCreatedPeer(java.lang.Object,java.lang.Object);protected static#void#targetDisposedPeer(java.lang.Object,java.lang.Object);protected#boolean#isModalExcludedSupportedImpl();protected#boolean#needsXEmbedImpl();protected#java.awt.EventQueue#getSystemEventQueueImpl();protected#java.awt.Image#getImageWithResolutionVariant(java.lang.String,java.lang.String);protected#java.awt.Image#getImageWithResolutionVariant(java.net.URL,java.net.URL);protected#java.awt.RenderingHints#getDesktopAAHints();protected#long#timeout(long);public abstract#boolean#isDesktopSupported();public abstract#boolean#isTaskbarSupported();public abstract#boolean#isTraySupported();public abstract#java.awt.peer.FramePeer#createLightweightFrame(sun.awt.LightweightFrame);public abstract#java.awt.peer.KeyboardFocusManagerPeer#getKeyboardFocusManagerPeer();public abstract#java.awt.peer.SystemTrayPeer#createSystemTray(java.awt.SystemTray);public abstract#java.awt.peer.TrayIconPeer#createTrayIcon(java.awt.TrayIcon);public abstract#void#grab(java.awt.Window);public abstract#void#ungrab(java.awt.Window);public static final#boolean#awtTryLock();public static final#boolean#isAWTLockHeldByCurrentThread();public static final#void#awtLock();public static final#void#awtLockNotify();public static final#void#awtLockNotifyAll();public static final#void#awtLockWait();public static final#void#awtLockWait(long);public static final#void#awtUnlock();public static native#void#closeSplashScreen();public static synchronized#boolean#getSunAwtDisableMixing();public static synchronized#void#consumeNextKeyTyped(java.awt.event.KeyEvent);public static#boolean#getSunAwtErasebackgroundonresize();public static#boolean#getSunAwtNoerasebackground();public static#boolean#isContainingTopLevelOpaque(java.awt.Component);public static#boolean#isContainingTopLevelTranslucent(java.awt.Component);public static#boolean#isInstanceOf(java.lang.Object,java.lang.String);public static#boolean#isLightweightOrUnknown(java.awt.Component);public static#boolean#isModalExcluded(java.awt.Window);public static#boolean#isModalExcludedSupported();public static#boolean#isSystemGenerated(java.awt.AWTEvent);public static#boolean#isTouchKeyboardAutoShowEnabled();public static#boolean#needsXEmbed();public static#java.awt.Component#getHeavyweightComponent(java.awt.Component);public static#java.awt.Container#getNativeContainer(java.awt.Component);public static#java.awt.EventQueue#getSystemEventQueueImplPP();public static#java.awt.Image#createImageWithResolutionVariant(java.awt.Image,java.awt.Image);public static#java.awt.RenderingHints#getDesktopFontHints();public static#java.awt.Window#getContainingWindow(java.awt.Component);public static#java.awt.image.BufferedImage#getScaledIconImage(java.util.List,int,int);public static#java.awt.image.DataBufferInt#getScaledIconData(java.util.List,int,int);public static#java.util.Locale#getStartupLocale();public static#void#checkAndSetPolicy(java.awt.Container);public static#void#executeOnEDTAndWait(java.lang.Object,java.lang.Runnable);public static#void#executeOnEventHandlerThread(java.lang.Object,java.lang.Runnable);public static#void#executeOnEventHandlerThread(java.lang.Object,java.lang.Runnable,long);public static#void#executeOnEventHandlerThread(sun.awt.PeerEvent);public static#void#flushPendingEvents();public static#void#invokeLater(java.lang.Runnable);public static#void#postEvent(java.awt.AWTEvent);public static#void#postPriorityEvent(java.awt.AWTEvent);public static#void#setAAFontSettingsCondition(boolean);public static#void#setLWRequestStatus(java.awt.Window,boolean);public static#void#setModalExcluded(java.awt.Window);public static#void#setSystemGenerated(java.awt.AWTEvent);public synchronized#long#getWindowDeactivationTime(java.awt.Window);public synchronized#void#setWindowDeactivationTime(java.awt.Window,long);public#[Ljava.lang.String;#getFontList();public#boolean#canPopupOverlapTaskBar();public#boolean#enableInputMethodsForTextComponent();public#boolean#isModalExclusionTypeSupported(java.awt.Dialog$ModalExclusionType);public#boolean#isModalityTypeSupported(java.awt.Dialog$ModalityType);public#boolean#isNativeGTKAvailable();public#boolean#isPrintableCharacterModifiersMask(int);public#boolean#isRunningOnWayland();public#boolean#isSwingBackbufferTranslucencySupported();public#boolean#isTranslucencyCapable(java.awt.GraphicsConfiguration);public#boolean#isWindowOpacitySupported();public#boolean#isWindowShapingSupported();public#boolean#isWindowTranslucencySupported();public#boolean#needUpdateWindow();public#boolean#prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#boolean#useBufferPerWindow();public#int#checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver);public#int#getFocusAcceleratorKeyMask();public#int#getNumberOfButtons();public#java.awt.Dimension#getScreenSize();public#java.awt.FontMetrics#getFontMetrics(java.awt.Font);public#java.awt.Image#createImage([B,int,int);public#java.awt.Image#createImage(java.awt.image.ImageProducer);public#java.awt.Image#createImage(java.lang.String);public#java.awt.Image#createImage(java.net.URL);public#java.awt.Image#getImage(java.lang.String);public#java.awt.Image#getImage(java.net.URL);public#java.awt.Window#createInputMethodWindow(java.lang.String,sun.awt.im.InputContext);public#java.awt.image.ColorModel#getColorModel();public#java.lang.String#getDesktop();public#java.util.Locale#getDefaultKeyboardLocale();public#void#addModalityListener(sun.awt.ModalityListener);public#void#disableBackgroundErase(java.awt.Canvas);public#void#disableBackgroundErase(java.awt.Component);public#void#dismissPopupOnFocusLostIfNeeded(java.awt.Window);public#void#dismissPopupOnFocusLostIfNeededCleanUp(java.awt.Window);public#void#notifyModalityPopped(java.awt.Dialog);public#void#notifyModalityPushed(java.awt.Dialog);public#void#realSync();public#void#realSync(long);public#void#removeModalityListener(sun.awt.ModalityListener);public#void#showOrHideTouchKeyboard(java.awt.Component,java.awt.AWTEvent);public#void#updateScreenMenuBarUI();static#java.awt.Image#getImageFromHash(java.awt.Toolkit,java.lang.String);static#java.awt.Image#getImageFromHash(java.awt.Toolkit,java.net.URL);static#void#wakeupEventQueue(java.awt.EventQueue,boolean)".equals(methods(c)), "methods");
        check("private final#java.lang.Object#waitLock;private static final#int#MAX_ITERS;private static final#int#MINIMAL_DELAY;private static final#int#MIN_ITERS;private static final#java.util.concurrent.locks.Condition#AWT_LOCK_COND;private static final#java.util.concurrent.locks.ReentrantLock#AWT_LOCK;private static volatile#sun.awt.PostEventQueue#postEventQueue;private static#boolean#checkedSystemAAFontSettings;private static#boolean#lastExtraCondition;private static#boolean#touchKeyboardAutoShowIsEnabled;private static#boolean#useSystemAAFontSettings;private static#java.awt.Dialog$ModalExclusionType#DEFAULT_MODAL_EXCLUSION_TYPE;private static#java.awt.RenderingHints#desktopFontHints;private static#java.lang.Boolean#sunAwtDisableMixing;private static#java.util.Locale#startupLocale;private static#java.util.WeakHashMap#activationMap;private#sun.awt.SunToolkit$ModalityListenerList#modalityListeners;protected static#int#numberOfButtons;public static final#int#DEFAULT_WAIT_TIME;public static final#int#GRAB_EVENT_MASK;public static final#int#MAX_BUTTONS_SUPPORTED;public static final#java.lang.String#DESKTOPFONTHINTS;public static volatile#java.awt.EventQueue#currentEventQueue;static final#sun.awt.SoftCache#fileImgCache;static final#sun.awt.SoftCache#urlImgCache".equals(fields(c)), "fields");
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
