/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.print.AttributeClass conformance: reflection (no init, native/display-heavy).
 * @run main AttributeClassTest
 */

import java.util.*;
import java.lang.reflect.*;

public class AttributeClassTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.print.AttributeClass", false, ClassLoader.getSystemClassLoader());
        check(c != null, "load");
        check("sun.print.AttributeClass".equals(c.getName()), "name");
        check(!c.isAnonymousClass(), "not anonymous");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check("".equals(interfaces(c)), "interfaces");
        check("private#int#convertToInt([B);private#int#unsignedByteToInt(byte);public#[C#getLenChars();public#[I#getArrayOfIntValues();public#[I#getIntRangeValue();public#[Ljava.lang.String;#getArrayOfStringValues();public#boolean#equals(java.lang.Object);public#byte#getByteValue();public#byte#getType();public#int#getIntValue();public#int#hashCode();public#java.lang.Object#getObjectValue();public#java.lang.String#getName();public#java.lang.String#getStringValue();public#java.lang.String#toString()".equals(methods(c)), "methods");
        check("private#int#myType;private#int#nameLen;private#java.lang.Object#myValue;private#java.lang.String#myName;public static final#int#TAG_BOOL;public static final#int#TAG_CHARSET;public static final#int#TAG_DATE;public static final#int#TAG_ENUM;public static final#int#TAG_INT;public static final#int#TAG_KEYWORD;public static final#int#TAG_MEMBER_ATTRNAME;public static final#int#TAG_MIME_MEDIATYPE;public static final#int#TAG_NAME_LANGUAGE;public static final#int#TAG_NAME_WO_LANGUAGE;public static final#int#TAG_NATURALLANGUAGE;public static final#int#TAG_OCTET;public static final#int#TAG_RANGE_INTEGER;public static final#int#TAG_RESOLUTION;public static final#int#TAG_TEXT_LANGUAGE;public static final#int#TAG_TEXT_WO_LANGUAGE;public static final#int#TAG_UNSUPPORTED_VALUE;public static final#int#TAG_URI;public static final#sun.print.AttributeClass#ATTRIBUTES_CHARSET;public static final#sun.print.AttributeClass#ATTRIBUTES_NATURAL_LANGUAGE".equals(fields(c)), "fields");
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
