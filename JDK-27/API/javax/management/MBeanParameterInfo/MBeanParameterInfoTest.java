/*
 * @test
 * @summary javax.management.MBeanParameterInfo conformance: white-box structure + black-box behavior.
 *          Deepened: equals participation matrix, hashCode = name+type only,
 *          null-descriptor default, toString format, serialization.
 * @run main MBeanParameterInfoTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanParameterInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == MBeanFeatureInfo.class, "extends MBeanFeatureInfo");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getType"), "has getType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanParameterInfo p = new MBeanParameterInfo("name", "java.lang.String", "desc");
        check("name".equals(p.getName()), "getName");
        check("java.lang.String".equals(p.getType()), "getType");
        check("desc".equals(p.getDescription()), "getDescription");

        // descriptor constructor
        ImmutableDescriptor d = new ImmutableDescriptor("name=name", "description=desc");
        MBeanParameterInfo pd = new MBeanParameterInfo("name", "java.lang.String", "desc", d);
        check(pd.getDescriptor() != null && "name".equals(pd.getDescriptor().getFieldValue("name")), "descriptor ctor");

        // equals compares name + type
        check(p.equals(new MBeanParameterInfo("name", "java.lang.String", "desc")), "equals identical");
        check(!p.equals(new MBeanParameterInfo("name", "int", "desc")), "different type not equal");
        check(!p.equals(new MBeanParameterInfo("other", "java.lang.String", "desc")), "different name not equal");
        check(!p.equals(null), "not equal null");
        check(p.hashCode() == new MBeanParameterInfo("name", "java.lang.String", "desc").hashCode(), "equal hashCode");

        // clone
        MBeanParameterInfo clone = (MBeanParameterInfo) p.clone();
        check(clone != p && clone.equals(p), "clone equals original");
        check(p.toString() != null, "toString non-null");
    }

    // ---- deepened: equals matrix, hashCode scope, toString, serialization ---

    static void testEqualsHashCodeDeep() {
        MBeanParameterInfo base = new MBeanParameterInfo("name", "java.lang.String", "desc");
        check(!base.equals(new MBeanParameterInfo("name", "int", "desc")), "type participates in equals");
        check(!base.equals(new MBeanParameterInfo("name", "java.lang.String", "OTHER")),
                "description participates in equals");
        check(!base.equals(new MBeanParameterInfo("name", "java.lang.String", "desc",
                new ImmutableDescriptor("k=v"))), "descriptor participates in equals");
        check(base.hashCode() == new MBeanParameterInfo("name", "java.lang.String", "OTHER").hashCode(),
                "hashCode uses only name and type");
    }

    static void testNullDescriptorToStringSerialization() throws Exception {
        MBeanParameterInfo plain = new MBeanParameterInfo("name", "java.lang.String", "desc");
        check(plain.getDescriptor().getFieldNames().length == 0, "3-arg ctor yields empty descriptor");
        check(plain.getDescriptor() instanceof ImmutableDescriptor, "default descriptor is ImmutableDescriptor");
        String s = plain.toString();
        check(s.contains("name=name") && s.contains("type=java.lang.String") && s.contains("description=desc"),
                "toString carries name/type/description");

        MBeanParameterInfo original = new MBeanParameterInfo("name", "int", "desc", new ImmutableDescriptor("k=v"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanParameterInfo read = (MBeanParameterInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check("v".equals(read.getDescriptor().getFieldValue("k")), "descriptor survives serialization");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEqualsHashCodeDeep();
        testNullDescriptorToStringSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
