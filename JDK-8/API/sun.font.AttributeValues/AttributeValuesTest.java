/*
 * @test
 * @summary sun.font.AttributeValues conformance: reflection (no init, native/display-heavy).
 * @run main AttributeValuesTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AttributeValuesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.font.AttributeValues", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.font.AttributeValues".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("java.lang.Cloneable".equals(interfaces(c)), "interfaces");
        check("private static#boolean#equals(java.lang.Object,java.lang.Object);private static#java.awt.geom.AffineTransform#extractRotation(java.awt.geom.Point2D$Double,java.awt.geom.AffineTransform,boolean);private#boolean#i_equals(sun.font.EAttribute,sun.font.AttributeValues);private#boolean#i_validate(sun.font.EAttribute);private#java.lang.Object#i_get(sun.font.EAttribute);private#void#i_set(sun.font.EAttribute,java.lang.Object);private#void#i_set(sun.font.EAttribute,sun.font.AttributeValues);private#void#update(sun.font.EAttribute);public static transient#int#getMask([Lsun.font.EAttribute;);public static#boolean#is16Hashtable(java.util.Hashtable);public static#float#getJustification(java.util.Map);public static#int#getMask(sun.font.EAttribute);public static#java.awt.font.NumericShaper#getNumericShaping(java.util.Map);public static#java.awt.geom.AffineTransform#extractXRotation(java.awt.geom.AffineTransform,boolean);public static#java.awt.geom.AffineTransform#extractYRotation(java.awt.geom.AffineTransform,boolean);public static#java.awt.geom.AffineTransform#getBaselineTransform(java.util.Map);public static#java.awt.geom.AffineTransform#getCharTransform(java.util.Map);public static#sun.font.AttributeValues#fromMap(java.util.Map);public static#sun.font.AttributeValues#fromMap(java.util.Map,int);public static#sun.font.AttributeValues#fromSerializableHashtable(java.util.Hashtable);public#boolean#allDefined(int);public#boolean#anyDefined(int);public#boolean#anyNonDefault(int);public#boolean#equals(java.lang.Object);public#boolean#equals(sun.font.AttributeValues);public#boolean#getStrikethrough();public#boolean#getSwapColors();public#boolean#isDefined(sun.font.EAttribute);public#boolean#isNonDefault(sun.font.EAttribute);public#float#getJustification();public#float#getPosture();public#float#getSize();public#float#getTracking();public#float#getWeight();public#float#getWidth();public#int#getBidiEmbedding();public#int#getInputMethodUnderline();public#int#getKerning();public#int#getLigatures();public#int#getRunDirection();public#int#getSuperscript();public#int#getUnderline();public#int#hashCode();public#java.awt.Font#getFont();public#java.awt.Paint#getBackground();public#java.awt.Paint#getForeground();public#java.awt.font.GraphicAttribute#getCharReplacement();public#java.awt.font.NumericShaper#getNumericShaping();public#java.awt.geom.AffineTransform#getBaselineTransform();public#java.awt.geom.AffineTransform#getCharTransform();public#java.awt.geom.AffineTransform#getTransform();public#java.lang.Object#get(sun.font.EAttribute);public#java.lang.Object#getInputMethodHighlight();public#java.lang.String#getFamily();public#java.lang.String#toString();public#java.util.Hashtable#toSerializableHashtable();public#java.util.Map#toMap(java.util.Map);public#sun.font.AttributeValues#applyIMHighlight();public#sun.font.AttributeValues#clone();public#sun.font.AttributeValues#merge(java.util.Map);public#sun.font.AttributeValues#merge(java.util.Map,int);public#sun.font.AttributeValues#merge(sun.font.AttributeValues);public#sun.font.AttributeValues#merge(sun.font.AttributeValues,int);public#void#defineAll(int);public#void#set(sun.font.EAttribute,java.lang.Object);public#void#set(sun.font.EAttribute,sun.font.AttributeValues);public#void#setBackground(java.awt.Paint);public#void#setBidiEmbedding(int);public#void#setCharReplacement(java.awt.font.GraphicAttribute);public#void#setDefault(sun.font.EAttribute);public#void#setFamily(java.lang.String);public#void#setFont(java.awt.Font);public#void#setForeground(java.awt.Paint);public#void#setInputMethodHighlight(java.awt.im.InputMethodHighlight);public#void#setInputMethodHighlight(java.text.Annotation);public#void#setInputMethodUnderline(int);public#void#setJustification(float);public#void#setKerning(int);public#void#setLigatures(int);public#void#setNumericShaping(java.awt.font.NumericShaper);public#void#setPosture(float);public#void#setRunDirection(int);public#void#setSize(float);public#void#setStrikethrough(boolean);public#void#setSuperscript(int);public#void#setSwapColors(boolean);public#void#setTracking(float);public#void#setTransform(java.awt.font.TransformAttribute);public#void#setTransform(java.awt.geom.AffineTransform);public#void#setUnderline(int);public#void#setWeight(float);public#void#setWidth(float);public#void#unset(sun.font.EAttribute);public#void#unsetDefault();public#void#updateDerivedTransforms()".equals(methods(c)), "methods");
        check("private static final#java.lang.String#DEFINED_KEY;private static final#sun.font.AttributeValues#DEFAULT;private#boolean#strikethrough;private#boolean#swapColors;private#byte#bidiEmbedding;private#byte#imUnderline;private#byte#kerning;private#byte#ligatures;private#byte#runDirection;private#byte#superscript;private#byte#underline;private#float#justification;private#float#posture;private#float#size;private#float#tracking;private#float#weight;private#float#width;private#int#defined;private#int#nondefault;private#java.awt.Font#font;private#java.awt.Paint#background;private#java.awt.Paint#foreground;private#java.awt.font.GraphicAttribute#charReplacement;private#java.awt.font.NumericShaper#numericShaping;private#java.awt.geom.AffineTransform#baselineTransform;private#java.awt.geom.AffineTransform#charTransform;private#java.awt.geom.AffineTransform#transform;private#java.lang.Object#imHighlight;private#java.lang.String#family;public static final#int#MASK_ALL".equals(fields(c)), "fields");
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
