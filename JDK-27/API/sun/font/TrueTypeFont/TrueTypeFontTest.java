/*
 * @test
 * @summary sun.font.TrueTypeFont conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main TrueTypeFontTest
 */

import java.util.*;
import java.lang.reflect.*;

public class TrueTypeFontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.TrueTypeFont", false, TrueTypeFontTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.TrueTypeFont".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.font.FileFont".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#[B#readBytes(int,int);#[Ljava.lang.String;#getAllFamilyNames();#[Ljava.lang.String;#getAllFullNames();#boolean#supportsEncoding(java.lang.String);#boolean#supportsJA();#boolean#useEmbeddedBitmapsForSize(int);#int#getTableOffset(int);#int#getTableSize(int);#int#readBlock(java.nio.ByteBuffer,int,int);#java.awt.geom.Point2D$Float#getGlyphPoint(long,int,int);#java.nio.ByteBuffer#getTableBuffer(int);#java.nio.ByteBuffer#readBlock(int,int);#sun.font.TrueTypeFont$DirectoryEntry#getDirectoryEntry(int);private static synchronized#void#createLCIDMap();private static#[S#getLanguageCompatibleLCIDsFromLocale(java.util.Locale);private static#short#getLCIDFromLocale(java.util.Locale);private static#void#addLCIDMapEntry(java.util.Map,java.lang.String,short);private static#void#createLCIDLanguageCompatibilityMap();private synchronized#java.nio.channels.FileChannel#open();private synchronized#java.nio.channels.FileChannel#open(boolean);private#[C#getGaspTable();private#boolean#isLanguageCompatible(short);private#java.lang.String#makeString([B,int,short,short);private#void#setCJKSupport(java.nio.ByteBuffer);private#void#setStrikethroughMetrics(java.nio.ByteBuffer,int);private#void#setStyle(java.nio.ByteBuffer);private#void#setUnderlineMetrics(java.nio.ByteBuffer,int);private#void#verify(boolean);protected synchronized#sun.font.FontScaler#getScaler();protected synchronized#void#close();protected#[B#getTableBytes(int);protected#java.lang.String#lookupName(short,int);protected#void#init(int);protected#void#initAllNames(int,java.util.HashSet);protected#void#initNames();protected#void#setStyle();public#boolean#hasSupplementaryChars();public#boolean#useAAForPtSize(int);public#int#getFontCount();public#int#getWeight();public#int#getWidth();public#java.lang.String#getFamilyName(java.util.Locale);public#java.lang.String#getFontName(java.util.Locale);public#java.lang.String#getFullName();public#java.lang.String#getPostscriptName();public#java.lang.String#toString();public#sun.font.CharToGlyphMapper#getMapper();public#void#getStyleMetrics(float,[F,int);static#java.lang.String#getCodePage()".equals(methods(c)), "methods");
        check("#[Lsun.font.TrueTypeFont$DirectoryEntry;#tableDirectory;#int#directoryCount;#int#directoryOffset;#int#fontDataSize;#int#fontIndex;#int#numTables;#sun.font.TrueTypeFont$TTDisposerRecord#disposerRecord;private static final#[Ljava.lang.String;#codePages;private static final#[S#EMPTY_COMPATIBLE_LCIDS;private static final#[[Ljava.lang.String;#languages;private static final#int#DIRECTORYENTRYSIZE;private static final#int#DIRECTORYHEADERSIZE;private static final#int#TTCHEADERSIZE;private static final#int#fsSelectionBoldBit;private static final#int#fsSelectionItalicBit;private static final#int#fsSelectionRegularBit;private static final#short#US_LCID;private static#java.lang.String#defaultCodePage;private static#java.util.Map#lcidLanguageCompatibilityMap;private static#java.util.Map#lcidMap;private#[C#gaspTable;private#[S#languageCompatibleLCIDs;private#boolean#supportsCJK;private#boolean#supportsJA;private#float#stPos;private#float#stSize;private#float#ulPos;private#float#ulSize;private#int#fontWeight;private#int#fontWidth;private#java.lang.String#localeFamilyName;private#java.lang.String#localeFullName;private#java.util.Locale#nameLocale;public static final#int#EBLCTag;public static final#int#FAMILY_NAME_ID;public static final#int#FULL_NAME_ID;public static final#int#GDEFTag;public static final#int#GPOSTag;public static final#int#GSUBTag;public static final#int#MACROMAN_ENGLISH_LANG;public static final#int#MACROMAN_SPECIFIC_ID;public static final#int#MAC_PLATFORM_ID;public static final#int#MS_PLATFORM_ID;public static final#int#POSTSCRIPT_NAME_ID;public static final#int#cmapTag;public static final#int#fdscTag;public static final#int#featTag;public static final#int#fvarTag;public static final#int#gaspTag;public static final#int#glyfTag;public static final#int#headTag;public static final#int#hheaTag;public static final#int#hmtxTag;public static final#int#locaTag;public static final#int#maxpTag;public static final#int#mortTag;public static final#int#morxTag;public static final#int#nameTag;public static final#int#os_2Tag;public static final#int#ottoTag;public static final#int#postTag;public static final#int#reserved_bits1;public static final#int#reserved_bits2;public static final#int#trueTag;public static final#int#ttcfTag;public static final#int#v1ttTag;public static final#short#ENGLISH_LOCALE_ID;static final#[Ljava.lang.String;#encoding_mapping".equals(fields(c)), "fields");
        check("".equals(enumConstants(c)), "enumConstants");
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
