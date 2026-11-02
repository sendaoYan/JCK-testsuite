/*
 * @test
 * @summary sun.font.SunFontManager conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main SunFontManagerTest
 */

import java.util.*;
import java.lang.reflect.*;

public class SunFontManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.SunFontManager", false, SunFontManagerTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.SunFontManager".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("sun.font.FontManagerForSGE;sun.java2d.FontSupport".equals(interfaces(c)), "interfaces");
        check("#sun.font.Font2D#findFontFromPlatformMap(java.lang.String,int);#sun.font.PhysicalFont#findJREDeferredFont(java.lang.String,int);#void#addToPool(sun.font.FileFont);private static native#void#initIDs();private static#boolean#fontSupportsEncoding(java.awt.Font,java.lang.String);private static#java.lang.String#dotStyleStr(int);private static#java.util.HashSet#getInstalledNames();private static#java.util.Locale#getSystemStartupLocale();private static#void#initStatic();private synchronized#java.util.HashMap#getFullNameToFileMap();private synchronized#void#checkForUnreferencedFontFiles();private synchronized#void#loadLocaleNames();private#[Ljava.lang.String;#getFontFilesFromPath(boolean);private#boolean#isNameForRegisteredFile(java.lang.String);private#java.lang.String#getPathName(java.lang.String);private#sun.font.Font2D#findFont2DAllLocales(java.lang.String,int);private#sun.font.Font2D#findFontFromPlatform(java.lang.String,int);private#sun.font.PhysicalFont#findDeferredFont(java.lang.String,int);private#sun.font.PhysicalFont#findOtherDeferredFont(java.lang.String,int);private#sun.font.PhysicalFont#registerFontFile(java.lang.String);private#void#addCompositeToFontList(sun.font.CompositeFont,int);private#void#addDirFonts(java.lang.String,java.io.File,java.io.FilenameFilter,int,boolean,int,boolean,boolean);private#void#initCompositeFonts(sun.awt.FontConfiguration,java.util.concurrent.ConcurrentHashMap);private#void#logPlatformFontInfo();private#void#registerFontsOnPath(java.lang.String,boolean,int,boolean,boolean);private#void#removeFromCache(sun.font.Font2D);private#void#resolveFontFiles(java.util.HashSet,java.util.ArrayList);private#void#resolveWindowsFonts();protected abstract#[Ljava.lang.String;#getDefaultPlatformFont();protected abstract#java.lang.String#getFontPath(boolean);protected abstract#sun.awt.FontConfiguration#createFontConfiguration();protected static#void#registerCompositeFont(java.lang.String,[Ljava.lang.String;,[Ljava.lang.String;,int,[I,[I,boolean,java.util.concurrent.ConcurrentHashMap);protected synchronized#void#initialiseDeferredFonts();protected synchronized#void#registerDeferredJREFonts(java.lang.String);protected#[Ljava.lang.String;#getFontNamesFromPlatform();protected#[Ljava.lang.String;#getNativeNames(java.lang.String,java.lang.String);protected#[Ljava.lang.String;#getPlatformFontDirs(boolean);protected#[Lsun.font.PhysicalFont;#getPhysicalFonts();protected#boolean#useAbsoluteFontFileNames();protected#java.lang.String#getFileNameFromPlatformName(java.lang.String);protected#javax.swing.plaf.FontUIResource#getFontConfigFUIR(java.lang.String,int,int);protected#sun.font.PhysicalFont#addToFontList(sun.font.PhysicalFont,int);protected#void#addFontToPlatformFontPath(java.lang.String);protected#void#addNativeFontFamilyNames(java.util.TreeMap,java.util.Locale);protected#void#addToMissingFontFileList(java.lang.String);protected#void#getJREFontFamilyNames(java.util.TreeMap,java.util.Locale);protected#void#loadFonts();protected#void#populateFontFileNameMap(java.util.HashMap,java.util.HashMap,java.util.HashMap,java.util.Locale);protected#void#registerCompositeFont(java.lang.String,[Ljava.lang.String;,[Ljava.lang.String;,int,[I,[I,boolean);protected#void#registerFontDir(java.lang.String);protected#void#registerFontDirs(java.lang.String);protected#void#registerFontFile(java.lang.String,[Ljava.lang.String;,int,boolean);protected#void#registerFontsInDir(java.lang.String,boolean,int,boolean,boolean);protected#void#registerOtherFontFiles(java.util.HashSet);protected#void#registerPlatformFontsUsedByFontConfiguration();public abstract#sun.awt.FontConfiguration#createFontConfiguration(boolean,boolean);public static final#java.lang.String#getJDKFontDir();public static#sun.font.SunFontManager#getInstance();public synchronized#java.lang.String#getDefaultFontFaceName();public synchronized#java.lang.String#getDefaultFontFile();public synchronized#java.lang.String#getFullNameByFileName(java.lang.String);public synchronized#sun.font.PhysicalFont#initialiseDeferredFont(java.lang.String);public synchronized#void#deRegisterBadFont(sun.font.Font2D);public synchronized#void#preferLocaleFonts();public synchronized#void#preferProportionalFonts();public synchronized#void#replaceFont(sun.font.PhysicalFont,sun.font.PhysicalFont);public synchronized#void#useAlternateFontforJALocales();public#[Ljava.awt.Font;#getAllInstalledFonts();public#[Ljava.awt.Font;#getCreatedFonts();public#[Ljava.lang.String;#getInstalledFontFamilyNames(java.util.Locale);public#[Lsun.font.Font2D;#createFont2D(java.io.File,int,boolean,boolean);public#[Lsun.font.Font2D;#getRegisteredFonts();public#boolean#getFamilyNamesFromPlatform(java.util.TreeMap,java.util.Locale);public#boolean#gotFontsFromPlatform();public#boolean#isDeferredFont(java.lang.String);public#boolean#isRegisteredFontFile(java.lang.String);public#boolean#registerFont(java.awt.Font);public#boolean#usingAlternateCompositeFonts();public#boolean#usingAlternateFontforJALocales();public#int#getNumFonts();public#java.io.FilenameFilter#getTrueTypeFilter();public#java.io.FilenameFilter#getType1Filter();public#java.lang.String#getFileNameForFontName(java.lang.String);public#java.lang.String#getPlatformFontPath(boolean);public#java.util.HashMap#populateHardcodedFileNameMap();public#java.util.TreeMap#getCreatedFontFamilyNames();public#sun.awt.FontConfiguration#getFontConfiguration();public#sun.font.Font2D#findFont2D(java.lang.String,int,int);public#sun.font.Font2D#getDefaultLogicalFont(int);public#sun.font.Font2DHandle#getNewComposite(java.lang.String,int,sun.font.Font2DHandle);public#sun.font.PhysicalFont#getDefaultPhysicalFont();public#sun.font.PhysicalFont#getRegisteredFontFile(java.lang.String);public#sun.font.PhysicalFont#registerFontFile(java.lang.String,[Ljava.lang.String;,int,boolean,int);public#sun.font.TrueTypeFont#getEUDCFont();public#void#createCompositeFonts(java.util.concurrent.ConcurrentHashMap,boolean,boolean);public#void#loadFontFiles();public#void#register1dot0Fonts();public#void#registerDeferredFont(java.lang.String,java.lang.String,[Ljava.lang.String;,int,boolean,int);public#void#registerFonts([Ljava.lang.String;,[[Ljava.lang.String;,int,int,boolean,int,boolean);public#void#registerFontsInDir(java.lang.String)".equals(methods(c)), "methods");
        check("#[Ljava.lang.String;#jreOtherFontFiles;#boolean#loadedAllFontFiles;#boolean#loadedAllFonts;#boolean#noOtherJREFontFiles;#java.lang.Thread#fileCloser;#java.util.Vector#tmpFontFiles;private final#java.util.concurrent.ConcurrentHashMap#deferredFontFiles;private final#java.util.concurrent.ConcurrentHashMap#initialisedFonts;private static final#java.io.FilenameFilter#t1Filter;private static final#java.io.FilenameFilter#ttFilter;private static final#java.lang.Object#regFamilyLock;private static#[Ljava.lang.String;#STR_ARRAY;private static#boolean#gAltJAFont;private static#int#maxSoftRefCnt;private static#java.util.HashSet#installedNames;private static#java.util.HashSet#missingFontFiles;private static#java.util.Locale#systemLocale;private static#sun.font.Font2DHandle#FONT_HANDLE_NULL;private#[Ljava.awt.Font;#allFonts;private#[Ljava.lang.String;#allFamilies;private#[Ljava.lang.String;#pathDirs;private#[Lsun.font.CompositeFont;#compFonts;private#boolean#_usingAlternateComposites;private#boolean#discoveredAllFonts;private#boolean#fontsAreRegistered;private#boolean#gLocalePref;private#boolean#gPropPref;private#boolean#haveCheckedUnreferencedFontFiles;private#boolean#loaded1dot0Fonts;private#int#createdFontCount;private#int#lastPoolIndex;private#int#maxCompFont;private#java.lang.String#defaultFontFileName;private#java.lang.String#defaultFontName;private#java.util.ArrayList#badFonts;private#java.util.HashMap#familyToFontListMap;private#java.util.HashMap#fontToFamilyNameMap;private#java.util.HashMap#fontToFileMap;private#java.util.HashMap#localeFullNamesToFont;private#java.util.Hashtable#createdByFamilyName;private#java.util.Hashtable#createdByFullName;private#java.util.Locale#lastDefaultLocale;private#java.util.concurrent.ConcurrentHashMap#compositeFonts;private#java.util.concurrent.ConcurrentHashMap#fontNameCache;private#java.util.concurrent.ConcurrentHashMap#physicalFonts;private#java.util.concurrent.ConcurrentHashMap#registeredFonts;private#sun.awt.FontConfiguration#fontConfig;private#sun.font.PhysicalFont#defaultPhysicalFont;protected static final#int#CHANNELPOOLSIZE;protected#[Lsun.font.FileFont;#fontFileCache;protected#java.lang.String#fontPath;protected#java.util.HashSet#registeredFontFiles;protected#java.util.concurrent.ConcurrentHashMap#fullNameToFont;public static final#int#FONTFORMAT_COMPOSITE;public static final#int#FONTFORMAT_NATIVE;public static final#int#FONTFORMAT_NONE;public static final#int#FONTFORMAT_TRUETYPE;public static final#int#FONTFORMAT_TTC;public static final#int#FONTFORMAT_TYPE1;public static#boolean#noType1Font;public static#java.lang.String#jreFontDirName;public static#java.lang.String#jreLibDirName;static volatile#java.util.HashMap#platformFontMap;static#boolean#longAddresses".equals(fields(c)), "fields");
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
