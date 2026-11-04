/*
 * @test
 * @summary sun.awt.X11.XProtocolConstants conformance: reflection (no init, native/display-heavy).
 * @run main XProtocolConstantsTest
 */

import java.util.*;
import java.lang.reflect.*;

public class XProtocolConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.X11.XProtocolConstants", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.X11.XProtocolConstants".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("".equals(methods(c)), "methods");
        check("public static final#int#X_AllocColor;public static final#int#X_AllocColorCells;public static final#int#X_AllocColorPlanes;public static final#int#X_AllocNamedColor;public static final#int#X_AllowEvents;public static final#int#X_Bell;public static final#int#X_ChangeActivePointerGrab;public static final#int#X_ChangeGC;public static final#int#X_ChangeHosts;public static final#int#X_ChangeKeyboardControl;public static final#int#X_ChangeKeyboardMapping;public static final#int#X_ChangePointerControl;public static final#int#X_ChangeProperty;public static final#int#X_ChangeSaveSet;public static final#int#X_ChangeWindowAttributes;public static final#int#X_CirculateWindow;public static final#int#X_ClearArea;public static final#int#X_CloseFont;public static final#int#X_ConfigureWindow;public static final#int#X_ConvertSelection;public static final#int#X_CopyArea;public static final#int#X_CopyColormapAndFree;public static final#int#X_CopyGC;public static final#int#X_CopyPlane;public static final#int#X_CreateColormap;public static final#int#X_CreateCursor;public static final#int#X_CreateGC;public static final#int#X_CreateGlyphCursor;public static final#int#X_CreatePixmap;public static final#int#X_CreateWindow;public static final#int#X_DeleteProperty;public static final#int#X_DestroySubwindows;public static final#int#X_DestroyWindow;public static final#int#X_Error;public static final#int#X_FillPoly;public static final#int#X_ForceScreenSaver;public static final#int#X_FreeColormap;public static final#int#X_FreeColors;public static final#int#X_FreeCursor;public static final#int#X_FreeGC;public static final#int#X_FreePixmap;public static final#int#X_GetAtomName;public static final#int#X_GetFontPath;public static final#int#X_GetGeometry;public static final#int#X_GetImage;public static final#int#X_GetInputFocus;public static final#int#X_GetKeyboardControl;public static final#int#X_GetKeyboardMapping;public static final#int#X_GetModifierMapping;public static final#int#X_GetMotionEvents;public static final#int#X_GetPointerControl;public static final#int#X_GetPointerMapping;public static final#int#X_GetProperty;public static final#int#X_GetScreenSaver;public static final#int#X_GetSelectionOwner;public static final#int#X_GetWindowAttributes;public static final#int#X_GrabButton;public static final#int#X_GrabKey;public static final#int#X_GrabKeyboard;public static final#int#X_GrabPointer;public static final#int#X_GrabServer;public static final#int#X_ImageText16;public static final#int#X_ImageText8;public static final#int#X_InstallColormap;public static final#int#X_InternAtom;public static final#int#X_KillClient;public static final#int#X_ListExtensions;public static final#int#X_ListFonts;public static final#int#X_ListFontsWithInfo;public static final#int#X_ListHosts;public static final#int#X_ListInstalledColormaps;public static final#int#X_ListProperties;public static final#int#X_LookupColor;public static final#int#X_MapSubwindows;public static final#int#X_MapWindow;public static final#int#X_NoOperation;public static final#int#X_OpenFont;public static final#int#X_PolyArc;public static final#int#X_PolyFillArc;public static final#int#X_PolyFillRectangle;public static final#int#X_PolyLine;public static final#int#X_PolyPoint;public static final#int#X_PolyRectangle;public static final#int#X_PolySegment;public static final#int#X_PolyText16;public static final#int#X_PolyText8;public static final#int#X_PutImage;public static final#int#X_QueryBestSize;public static final#int#X_QueryColors;public static final#int#X_QueryExtension;public static final#int#X_QueryFont;public static final#int#X_QueryKeymap;public static final#int#X_QueryPointer;public static final#int#X_QueryTextExtents;public static final#int#X_QueryTree;public static final#int#X_RecolorCursor;public static final#int#X_ReparentWindow;public static final#int#X_Reply;public static final#int#X_RotateProperties;public static final#int#X_SendEvent;public static final#int#X_SetAccessControl;public static final#int#X_SetClipRectangles;public static final#int#X_SetCloseDownMode;public static final#int#X_SetDashes;public static final#int#X_SetFontPath;public static final#int#X_SetInputFocus;public static final#int#X_SetModifierMapping;public static final#int#X_SetPointerMapping;public static final#int#X_SetScreenSaver;public static final#int#X_SetSelectionOwner;public static final#int#X_StoreColors;public static final#int#X_StoreNamedColor;public static final#int#X_TranslateCoords;public static final#int#X_UngrabButton;public static final#int#X_UngrabKey;public static final#int#X_UngrabKeyboard;public static final#int#X_UngrabPointer;public static final#int#X_UngrabServer;public static final#int#X_UninstallColormap;public static final#int#X_UnmapSubwindows;public static final#int#X_UnmapWindow;public static final#int#X_WarpPointer".equals(fields(c)), "fields");
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
