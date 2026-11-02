/*
 * @test
 * @summary sun.awt.X11.XToolkit conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.awt.X11:+open
 * @run main XToolkitTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XToolkitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XToolkit", false, XToolkitTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XToolkit".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.awt.UNIXToolkit".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Runnable".equals(interfaces(c)), "interfaces");
        check("#boolean#getLastCursorPos(java.awt.Point);#sun.awt.X11.XEmbedChildProxyPeer#createEmbedProxy(sun.awt.X11.XEmbedChildProxy);#void#freeXKB();#void#init();#void#parseXSettings(int,java.util.Map);private native#int#getNumberOfButtonsImpl();private native#void#nativeLoadSystemColors([I);private static native#void#initIDs();private static#java.awt.Rectangle#getWorkArea(long,int);private static#void#callTimeoutTasks();private static#void#initScreenSize();private static#void#setBackingStoreType();private#boolean#initXSettingsIfNeeded(java.lang.String);private#java.awt.Insets#getScreenInsetsImpl(java.awt.GraphicsConfiguration);private#long#getNextTaskTime();private#void#dispatchEvent(sun.awt.X11.XEvent);private#void#loadXSettings();private#void#notifyListeners(sun.awt.X11.XEvent);private#void#processGlobalMotionEvent(sun.awt.X11.XEvent,sun.awt.X11.XBaseWindow);private#void#processXkbChanges(sun.awt.X11.XEvent);private#void#resetScreenInsetsCache();protected static#java.lang.Object#targetToPeer(java.lang.Object);protected static#void#targetDisposedPeer(java.lang.Object,java.lang.Object);protected#boolean#isDynamicLayoutSet();protected#boolean#isDynamicLayoutSupported();protected#boolean#needsXEmbedImpl();protected#boolean#syncNativeQueue(long);protected#java.lang.Object#lazilyLoadDesktopProperty(java.lang.String);protected#void#initializeDesktopProperties();public static synchronized#boolean#getSunAwtDisableGtkFileDialogs();public static#boolean#getSunAwtDisableGrab();public static#long#getDefaultRootWindow();public static#long#getDisplay();public static#long#getEventNumber();public synchronized#java.awt.peer.MouseInfoPeer#getMouseInfoPeer();public synchronized#void#addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener);public#boolean#areExtraMouseButtonsEnabled();public#boolean#enableInputMethodsForTextComponent();public#boolean#getLockingKeyState(int);public#boolean#isAlwaysOnTopSupported();public#boolean#isDesktopSupported();public#boolean#isDynamicLayoutActive();public#boolean#isFrameStateSupported(int);public#boolean#isModalExclusionTypeSupported(java.awt.Dialog$ModalExclusionType);public#boolean#isModalityTypeSupported(java.awt.Dialog$ModalityType);public#boolean#isTaskbarSupported();public#boolean#isTranslucencyCapable(java.awt.GraphicsConfiguration);public#boolean#isTraySupported();public#boolean#isWindowOpacitySupported();public#boolean#isWindowShapingSupported();public#boolean#isWindowTranslucencySupported();public#boolean#useBufferPerWindow();public#int#getMaximumCursorColors();public#int#getNumberOfButtons();public#int#getScreenResolution();public#java.awt.Cursor#createCustomCursor(java.awt.Image,java.awt.Point,java.lang.String);public#java.awt.Dimension#getBestCursorSize(int,int);public#java.awt.Insets#getScreenInsets(java.awt.GraphicsConfiguration);public#java.awt.PrintJob#getPrintJob(java.awt.Frame,java.lang.String,java.awt.JobAttributes,java.awt.PageAttributes);public#java.awt.PrintJob#getPrintJob(java.awt.Frame,java.lang.String,java.util.Properties);public#java.awt.datatransfer.Clipboard#getSystemClipboard();public#java.awt.datatransfer.Clipboard#getSystemSelection();public#java.awt.dnd.DragGestureRecognizer#createDragGestureRecognizer(java.lang.Class,java.awt.dnd.DragSource,java.awt.Component,int,java.awt.dnd.DragGestureListener);public#java.awt.dnd.peer.DragSourceContextPeer#createDragSourceContextPeer(java.awt.dnd.DragGestureEvent);public#java.awt.im.spi.InputMethodDescriptor#getInputMethodAdapterDescriptor();public#java.awt.peer.ButtonPeer#createButton(java.awt.Button);public#java.awt.peer.CanvasPeer#createCanvas(java.awt.Canvas);public#java.awt.peer.CheckboxMenuItemPeer#createCheckboxMenuItem(java.awt.CheckboxMenuItem);public#java.awt.peer.CheckboxPeer#createCheckbox(java.awt.Checkbox);public#java.awt.peer.ChoicePeer#createChoice(java.awt.Choice);public#java.awt.peer.DesktopPeer#createDesktopPeer(java.awt.Desktop);public#java.awt.peer.DialogPeer#createDialog(java.awt.Dialog);public#java.awt.peer.FileDialogPeer#createFileDialog(java.awt.FileDialog);public#java.awt.peer.FontPeer#getFontPeer(java.lang.String,int);public#java.awt.peer.FramePeer#createFrame(java.awt.Frame);public#java.awt.peer.FramePeer#createLightweightFrame(sun.awt.LightweightFrame);public#java.awt.peer.KeyboardFocusManagerPeer#getKeyboardFocusManagerPeer();public#java.awt.peer.LabelPeer#createLabel(java.awt.Label);public#java.awt.peer.ListPeer#createList(java.awt.List);public#java.awt.peer.MenuBarPeer#createMenuBar(java.awt.MenuBar);public#java.awt.peer.MenuItemPeer#createMenuItem(java.awt.MenuItem);public#java.awt.peer.MenuPeer#createMenu(java.awt.Menu);public#java.awt.peer.PanelPeer#createPanel(java.awt.Panel);public#java.awt.peer.PopupMenuPeer#createPopupMenu(java.awt.PopupMenu);public#java.awt.peer.RobotPeer#createRobot(java.awt.GraphicsDevice);public#java.awt.peer.ScrollPanePeer#createScrollPane(java.awt.ScrollPane);public#java.awt.peer.ScrollbarPeer#createScrollbar(java.awt.Scrollbar);public#java.awt.peer.SystemTrayPeer#createSystemTray(java.awt.SystemTray);public#java.awt.peer.TaskbarPeer#createTaskbarPeer(java.awt.Taskbar);public#java.awt.peer.TextAreaPeer#createTextArea(java.awt.TextArea);public#java.awt.peer.TextFieldPeer#createTextField(java.awt.TextField);public#java.awt.peer.TrayIconPeer#createTrayIcon(java.awt.TrayIcon);public#java.awt.peer.WindowPeer#createWindow(java.awt.Window);public#java.util.Map#mapInputMethodHighlight(java.awt.im.InputMethodHighlight);public#sun.awt.X11.XEmbeddedFramePeer#createEmbeddedFrame(sun.awt.X11.XEmbeddedFrame);public#sun.awt.datatransfer.DataTransferer#getDataTransferer();public#void#addXEventListener(sun.awt.X11.XToolkit$XEventListener);public#void#beep();public#void#grab(java.awt.Window);public#void#loadSystemColors([I);public#void#run();public#void#run(boolean);public#void#setDynamicLayout(boolean);public#void#ungrab(java.awt.Window);static native#java.lang.String#getEnv(java.lang.String);static native#long#getDefaultXColormap();static native#long#getTrayIconDisplayTimeout();static native#void#awt_output_flush();static native#void#awt_toolkit_init();static native#void#waitForEvents(long);static native#void#wakeup_poll();static#boolean#canUseXKBCalls();static#boolean#getModifierState(int);static#boolean#isKanaKeyboard();static#boolean#isLeftMouseButton(java.awt.event.MouseEvent);static#boolean#isRightMouseButton(java.awt.event.MouseEvent);static#boolean#isSunKeyboard();static#boolean#isToolkitThread();static#boolean#isXKBenabled();static#boolean#isXsunKPBehavior();static#boolean#tryXKB();static#int#getBackingStoreType();static#int#getMaxWindowHeightInPixels();static#int#getMaxWindowWidthInPixels();static#int#getMultiClickTime();static#int#getNumberOfButtonsForMask();static#int#getXKBBaseEventCode();static#int#getXKBEffectiveGroup();static#int#keysymToPrimaryKeycode(long);static#java.lang.String#getAWTAppClassName();static#java.lang.String#getCorrectXIDString(java.lang.String);static#javax.swing.UIDefaults#getUIDefaults();static#long#getCurrentServerTime();static#long#getXKBKbdDesc();static#sun.awt.X11.XBaseWindow#windowToXWindow(long);static#void#XSync();static#void#addEventDispatcher(long,sun.awt.X11.XEventDispatcher);static#void#addToWinMap(long,sun.awt.X11.XBaseWindow);static#void#awtFUnlock();static#void#dumpPeers();static#void#initUIDefaults();static#void#initializeMultiClickTime();static#void#processException(java.lang.Throwable);static#void#remove(java.lang.Runnable);static#void#removeEventDispatcher(long,sun.awt.X11.XEventDispatcher);static#void#removeFromWinMap(long,sun.awt.X11.XBaseWindow);static#void#removeSourceEvents(java.awt.EventQueue,java.lang.Object,boolean);static#void#resetKeyboardSniffer();static#void#schedule(java.lang.Runnable,long);static#void#setupModifierMap()".equals(methods(c)), "methods");
        check("#sun.awt.X11.XClipboard#clipboard;#sun.awt.X11.XClipboard#selection;private static final#int#AWT_MULTICLICK_DEFAULT_TIME;private static final#java.lang.String#dndPrefix;private static final#java.lang.String#postfix;private static final#java.lang.String#prefix;private static final#long#display;private static final#sun.awt.DisplayChangedListener#displayChangedHandler;private static final#sun.awt.X11.XEventDispatcher#timeFetcher;private static final#sun.awt.X11GraphicsDevice#device;private static final#sun.util.logging.PlatformLogger#backingStoreLog;private static final#sun.util.logging.PlatformLogger#eventLog;private static final#sun.util.logging.PlatformLogger#keyEventLog;private static final#sun.util.logging.PlatformLogger#log;private static final#sun.util.logging.PlatformLogger#timeoutTaskLog;private static volatile#int#maxWindowHeightInPixels;private static volatile#int#maxWindowWidthInPixels;private static#boolean#areExtraMouseButtonsEnabled;private static#boolean#initialized;private static#boolean#oops_updated;private static#boolean#timeStampUpdated;private static#int#backingStoreType;private static#int#oops_position;private static#java.lang.Boolean#sunAwtDisableGtkFileDialogs;private static#java.lang.String#awtAppClassName;private static#java.util.SortedMap#timeoutTasks;private static#long#eventNumber;private static#long#timeStamp;private static#sun.awt.X11.XAtom#_XA_JAVA_TIME_PROPERTY_ATOM;private static#sun.awt.X11.XEventDispatcher#oops_waiter;private static#sun.awt.X11.XMouseInfoPeer#xPeer;private#boolean#loadedXSettings;private#java.awt.Point#lastCursorPos;private#java.util.Collection#listeners;private#sun.awt.XSettings#xs;private#sun.font.FontConfigManager#fcManager;protected static#boolean#dynamicLayoutSetting;static final#boolean#PRIMARY_LOOP;static final#boolean#SECONDARY_LOOP;static final#int#IS_KANA_KEYBOARD;static final#int#IS_NONKANA_KEYBOARD;static final#int#IS_NONSUN_KEYBOARD;static final#int#IS_SUN_KEYBOARD;static final#int#XORG_KP_BEHAVIOR;static final#int#XSUN_KP_BEHAVIOR;static final#sun.awt.X11GraphicsEnvironment#localEnv;static#boolean#awt_UseXKB;static#boolean#awt_UseXKB_Calls;static#int#altMask;static#int#arrowCursor;static#int#awt_IsXsunKPBehavior;static#int#awt_XKBBaseEventCode;static#int#awt_XKBEffectiveGroup;static#int#awt_multiclick_time;static#int#kanaOrNotKeyboard;static#int#metaMask;static#int#modLockIsShiftLock;static#int#modeSwitchMask;static#int#numLockMask;static#int#sunOrNotKeyboard;static#java.lang.Thread#toolkitThread;static#java.util.HashMap#specialPeerMap;static#java.util.HashMap#winToDispatcher;static#java.util.TreeMap#winMap;static#javax.swing.UIDefaults#uidefaults;static#long#awt_XKBDescPtr".equals(fields(c)), "fields");
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
