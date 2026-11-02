/*
 * @test
 * @summary sun.font.Type1Font conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.font:+open
 * @run main Type1FontTest
 */

import java.util.*;
import java.lang.reflect.*;

public class Type1FontTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.Type1Font", false, Type1FontTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.font.Type1Font".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.font.FileFont".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("#sun.font.CharToGlyphMapper#getMapper();#void#readFile(java.nio.ByteBuffer);private synchronized#java.nio.ByteBuffer#getBuffer();private#boolean#isStyleToken(java.lang.String);private#byte#skip(java.nio.ByteBuffer);private#int#nextCapitalLetter(java.lang.String,int);private#int#nextTokenType(java.nio.ByteBuffer);private#java.lang.String#expandAbbreviation(java.lang.String);private#java.lang.String#expandName(java.lang.String,boolean);private#java.lang.String#fullName2FamilyName(java.lang.String);private#java.lang.String#getSimpleToken(java.nio.ByteBuffer);private#java.lang.String#getString(java.nio.ByteBuffer);private#java.lang.String#psName2FamilyName(java.lang.String);private#java.lang.String#psName2FullName(java.lang.String);private#void#initNames(java.nio.ByteBuffer);private#void#verify();private#void#verifyPFA(java.nio.ByteBuffer);private#void#verifyPFB(java.nio.ByteBuffer);protected synchronized#sun.font.FontScaler#getScaler();protected#void#close();public synchronized#java.nio.ByteBuffer#readBlock(int,int);public#int#getFileSize();public#int#getGlyphCode(char);public#int#getMissingGlyphCode();public#int#getNumGlyphs();public#java.lang.String#getPostscriptName();public#java.lang.String#toString()".equals(methods(c)), "methods");
        check("#java.lang.ref.WeakReference#bufferRef;private static final#int#PSEOFTOKEN;private static final#int#PSNAMETOKEN;private static final#int#PSSTRINGTOKEN;private static final#java.util.HashMap#styleAbbreviationsMapping;private static final#java.util.HashSet#styleNameTokes;private#java.lang.String#psName".equals(fields(c)), "fields");
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
