/*
 * @test
 * @summary sun.awt.FontConfiguration conformance: reflection (no init, native/display-heavy).
 * @run main FontConfigurationTest
 */

import java.util.*;
import java.lang.reflect.*;

public class FontConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.awt.FontConfiguration", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.awt.FontConfiguration".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private static#[I#getExclusionRanges(short);private static#[S#getFallbackScripts();private static#[S#getShortArray(short);private static#[S#readShortTable(java.io.DataInputStream,int);private static#[S#toList(java.util.HashMap);private static#boolean#contains([S,short,int);private static#boolean#isEmpty([S);private static#boolean#isLogicalFontStyleName(java.lang.String);private static#int#getArrayIndex([Ljava.lang.String;,java.lang.String);private static#java.lang.Object#getReorderSequence();private static#java.lang.String#getComponentFileName(short);private static#java.lang.String#getScriptName(short);private static#java.util.Vector#splitSequence(java.lang.String);private static#short#getComponentFileID(short);private static#short#getShortArrayID([S);private static#short#getStringID(java.lang.String);private static#void#dump();private static#void#printTable([S,int);private static#void#sanityCheck();private static#void#writeShortTable(java.io.DataOutputStream,[S);private#[Lsun.awt.FontDescriptor;#getFontDescriptors(int,int);private#java.io.File#findFontConfigFile(java.lang.String);private#java.io.File#findImpl(java.lang.String);private#java.nio.charset.CharsetEncoder#getFontCharsetEncoder(java.lang.String,java.lang.String);private#short#getInitELC();private#short#remapLocaleMap(int,int,short,short);private#short#remapProportional(int,short);private#void#findFontConfigFile();private#void#initAllComponentFonts();private#void#initFontConfig();private#void#readFontConfigFile(java.io.File);private#void#reorderSequenceForLocale([Ljava.lang.String;);private#void#setEncoding();private#void#shuffle([Ljava.lang.String;,int,int);protected abstract#java.lang.String#getEncoding(java.lang.String,java.lang.String);protected abstract#java.lang.String#getFaceNameFromComponentFontName(java.lang.String);protected abstract#java.lang.String#getFileNameFromComponentFontName(java.lang.String);protected abstract#java.nio.charset.Charset#getDefaultFontCharset(java.lang.String);protected abstract#void#initReorderMap();protected static#int#getFontIndex(java.lang.String);protected static#int#getStyleIndex(int);protected static#int#getStyleIndex(java.lang.String);protected static#java.lang.String#getComponentFontName(short);protected static#java.lang.String#getFontName(int);protected static#java.lang.String#getString(short);protected static#java.lang.String#getStyleName(int);protected static#short#getComponentFontID(short,int,int);protected static#short#getComponentFontIDMotif(short,int,int);protected static#sun.awt.FontConfiguration#getFontConfiguration();protected#[Ljava.lang.String;#split(java.lang.String);protected#[Lsun.awt.FontDescriptor;#buildFontDescriptors(int,int);protected#[S#getCoreScripts(int);protected#java.lang.String#getCompatibilityFamilyName(java.lang.String);protected#java.lang.String#makeAWTFontName(java.lang.String,java.lang.String);protected#java.lang.String#mapFileName(java.lang.String);protected#void#getInstalledFallbackFonts(java.lang.String);protected#void#setFontConfiguration();protected#void#setOsNameAndVersion();public abstract#java.lang.String#getFallbackFamilyName(java.lang.String,java.lang.String);public static#boolean#hasMonoToPropMap();public static#boolean#isLogicalFontFaceName(java.lang.String);public static#boolean#isLogicalFontFaceNameLC(java.lang.String);public static#boolean#isLogicalFontFamilyName(java.lang.String);public static#boolean#isLogicalFontFamilyNameLC(java.lang.String);public static#boolean#willReorderForStartupLocale();public static#java.lang.String#getLogicalFontFaceName(java.lang.String,int);public static#java.lang.String#getStyleString(int);public static#void#loadBinary(java.io.InputStream);public static#void#loadProperties(java.io.InputStream);public static#void#saveBinary(java.io.OutputStream);public synchronized#boolean#init();public#[Ljava.lang.String;#getPlatformFontNames();public#[Lsun.awt.FontDescriptor;#getFontDescriptors(java.lang.String,int);public#[Lsun.font.CompositeFontDescriptor;#get2DCompositeFontInfo();public#boolean#fontFilesArePresent();public#boolean#foundOsSpecificFile();public#boolean#needToSearchForFile(java.lang.String);public#int#getNumberCoreFonts();public#java.lang.String#getExtraFontPath();public#java.lang.String#getFileNameFromPlatformName(java.lang.String);public#java.lang.String#getVersion();public#java.util.HashSet#getAWTFontPathSet()".equals(methods(c)), "methods");
        check("#java.util.HashMap#existsMap;#java.util.HashMap#filenamesMap;#java.util.HashSet#coreFontFileNames;private static final#[I#EMPTY_INT_ARRAY;private static final#[Ljava.lang.String;#EMPTY_STRING_ARRAY;private static final#[S#EMPTY_SHORT_ARRAY;private static final#int#HEAD_LENGTH;private static final#int#INDEX_TABLEEND;private static final#int#INDEX_alphabeticSuffix;private static final#int#INDEX_appendedfontpath;private static final#int#INDEX_awtfontpaths;private static final#int#INDEX_componentFontNameIDs;private static final#int#INDEX_elcIDs;private static final#int#INDEX_exclusions;private static final#int#INDEX_fallbackScripts;private static final#int#INDEX_filenames;private static final#int#INDEX_fontfileNameIDs;private static final#int#INDEX_proportionals;private static final#int#INDEX_scriptFonts;private static final#int#INDEX_scriptFontsMotif;private static final#int#INDEX_scriptIDs;private static final#int#INDEX_sequences;private static final#int#INDEX_stringIDs;private static final#int#INDEX_stringTable;private static final#int#INDEX_version;private static final#java.lang.String#UNDEFINED_COMPONENT_FONT;private static#[C#table_stringTable;private static#[Ljava.lang.String;#stringCache;private static#[S#head;private static#[S#stringIDs;private static#[S#table_alphabeticSuffix;private static#[S#table_componentFontNameIDs;private static#[S#table_elcIDs;private static#[S#table_exclusions;private static#[S#table_filenames;private static#[S#table_fontfileNameIDs;private static#[S#table_proportionals;private static#[S#table_scriptFonts;private static#[S#table_scriptFontsMotif;private static#[S#table_scriptIDs;private static#[S#table_sequences;private static#[S#table_stringIDs;private static#java.lang.StringBuilder#stringTable;private static#short#stringIDNum;private static#sun.awt.FontConfiguration#fontConfig;private static#sun.util.logging.PlatformLogger#logger;private#[I#compCoreNum;private#[Ljava.lang.String;#componentFonts;private#[[[I#compExclusions;private#[[[Lsun.awt.FontDescriptor;#fontDescriptors;private#[[[S#compFontNameIDs;private#boolean#foundOsSpecificFile;private#boolean#inited;private#int#numCoreFonts;private#java.io.File#fontConfigFile;private#java.lang.String#alphabeticSuffix;private#java.lang.String#initEncoding;private#java.lang.String#javaLib;private#java.util.HashMap#reorderScripts;private#java.util.Hashtable#charsetRegistry;private#java.util.Locale#initLocale;private#java.util.Set#coreFontNameIDs;private#java.util.Set#fallbackFontNameIDs;private#short#initELC;protected static final#[Ljava.lang.String;#fontNames;protected static final#[Ljava.lang.String;#publicFontNames;protected static final#[Ljava.lang.String;#styleNames;protected static final#int#NUM_FONTS;protected static final#int#NUM_STYLES;protected static#[Ljava.lang.String;#installedFallbackFontFiles;protected static#[S#table_awtfontpaths;protected static#boolean#isProperties;protected static#java.lang.String#encoding;protected static#java.lang.String#osName;protected static#java.lang.String#osVersion;protected static#java.util.Hashtable#localeMap;protected static#java.util.Locale#startupLocale;protected#boolean#preferLocaleFonts;protected#boolean#preferPropFonts;protected#java.util.HashMap#reorderMap;protected#sun.font.SunFontManager#fontManager;public static#boolean#verbose".equals(fields(c)), "fields");
        check(c.getDeclaredConstructors().length == 2, "ctors");
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
