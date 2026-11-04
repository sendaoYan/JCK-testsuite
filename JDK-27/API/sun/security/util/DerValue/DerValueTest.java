/*
 * @test
 * @summary sun.security.util.DerValue conformance: internal package reflection.
 * @modules java.base/sun.security.util:+open
 * @run main DerValueTest
 */
import java.lang.reflect.*;
import java.util.*;

public class DerValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.DerValue", false, DerValueTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.util.DerValue".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<String>()), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("clear", "createTag", "data", "encode", "equals", "getAsString", "getBMPString", "getBigInteger", "getBitString", "getBoolean", "getData", "getDataBytes", "getEnumerated", "getGeneralString", "getGeneralizedTime", "getIA5String", "getInteger", "getNull", "getOID", "getOctetString", "getPositiveBigInteger", "getPrintableString", "getT61String", "getTag", "getTime", "getUTCTime", "getUTF8String", "getUnalignedBitString", "getUniversalString", "hashCode", "isApplication", "isConstructed", "isContextSpecific", "isPrintableStringChar", "isUniversal", "length", "resetTag", "subs", "toByteArray", "toDerInputStream", "toString", "validateBMPString", "withTag", "wrap"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("TAG_APPLICATION", "TAG_CONSTRUCT", "TAG_CONTEXT", "TAG_PRIVATE", "TAG_UNIVERSAL", "data", "tag", "tag_BMPString", "tag_BitString", "tag_Boolean", "tag_Enumerated", "tag_GeneralString", "tag_GeneralizedTime", "tag_IA5String", "tag_Integer", "tag_Null", "tag_ObjectId", "tag_OctetString", "tag_PrintableString", "tag_Sequence", "tag_SequenceOf", "tag_Set", "tag_SetOf", "tag_T61String", "tag_UTF8String", "tag_UniversalString", "tag_UtcTime"))), "fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) continue;
            int fm = fld.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(fld.getName());
        }
        return s;
    }
    static Set<String> enumConstantNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) s.add(fld.getName());
        }
        return s;
    }
}
