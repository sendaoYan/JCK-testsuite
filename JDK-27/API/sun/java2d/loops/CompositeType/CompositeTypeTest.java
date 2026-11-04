/*
 * @test
 * @summary sun.java2d.loops.CompositeType conformance: internal package reflection (no init, AWT/2D/font/print/swing native-heavy).
 * @modules java.desktop/sun.java2d.loops:+open
 * @run main CompositeTypeTest
 */

import java.util.*;
import java.lang.reflect.*;

public class CompositeTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.java2d.loops.CompositeType", false, CompositeTypeTest.class.getClassLoader());
        check(c != null, "load");
        check("sun.java2d.loops.CompositeType".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("public static synchronized#int#makeUniqueID(java.lang.String);public static#sun.java2d.loops.CompositeType#forAlphaComposite(java.awt.AlphaComposite);public#boolean#equals(java.lang.Object);public#boolean#isDerivedFrom(sun.java2d.loops.CompositeType);public#int#getUniqueID();public#int#hashCode();public#java.lang.String#getDescriptor();public#java.lang.String#toString();public#sun.java2d.loops.CompositeType#deriveSubType(java.lang.String);public#sun.java2d.loops.CompositeType#getSuperType()".equals(methods(c)), "methods");
        check("private static final#java.util.HashMap#compositeUIDMap;private static#int#unusedUID;private#int#uniqueID;private#java.lang.String#desc;private#sun.java2d.loops.CompositeType#next;public static final#java.lang.String#DESC_ALPHA_XOR;public static final#java.lang.String#DESC_ANY;public static final#java.lang.String#DESC_ANY_ALPHA;public static final#java.lang.String#DESC_CLEAR;public static final#java.lang.String#DESC_DST;public static final#java.lang.String#DESC_DST_ATOP;public static final#java.lang.String#DESC_DST_IN;public static final#java.lang.String#DESC_DST_OUT;public static final#java.lang.String#DESC_DST_OVER;public static final#java.lang.String#DESC_SRC;public static final#java.lang.String#DESC_SRC_ATOP;public static final#java.lang.String#DESC_SRC_IN;public static final#java.lang.String#DESC_SRC_NO_EA;public static final#java.lang.String#DESC_SRC_OUT;public static final#java.lang.String#DESC_SRC_OVER;public static final#java.lang.String#DESC_SRC_OVER_NO_EA;public static final#java.lang.String#DESC_XOR;public static final#sun.java2d.loops.CompositeType#AlphaXor;public static final#sun.java2d.loops.CompositeType#Any;public static final#sun.java2d.loops.CompositeType#AnyAlpha;public static final#sun.java2d.loops.CompositeType#Clear;public static final#sun.java2d.loops.CompositeType#Dst;public static final#sun.java2d.loops.CompositeType#DstAtop;public static final#sun.java2d.loops.CompositeType#DstIn;public static final#sun.java2d.loops.CompositeType#DstOut;public static final#sun.java2d.loops.CompositeType#DstOver;public static final#sun.java2d.loops.CompositeType#General;public static final#sun.java2d.loops.CompositeType#OpaqueSrcOverNoEa;public static final#sun.java2d.loops.CompositeType#Src;public static final#sun.java2d.loops.CompositeType#SrcAtop;public static final#sun.java2d.loops.CompositeType#SrcIn;public static final#sun.java2d.loops.CompositeType#SrcNoEa;public static final#sun.java2d.loops.CompositeType#SrcOut;public static final#sun.java2d.loops.CompositeType#SrcOver;public static final#sun.java2d.loops.CompositeType#SrcOverNoEa;public static final#sun.java2d.loops.CompositeType#Xor".equals(fields(c)), "fields");
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
