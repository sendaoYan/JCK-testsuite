/*
 * @test
 * @summary javax.management.MBeanConstructorInfo conformance: white-box structure + black-box behavior.
 * @run main MBeanConstructorInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanConstructorInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == MBeanFeatureInfo.class, "extends MBeanFeatureInfo");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getSignature"), "has getSignature");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanParameterInfo p1 = new MBeanParameterInfo("p1", "int", "p1 desc");
        MBeanParameterInfo p2 = new MBeanParameterInfo("p2", "java.lang.String", "p2 desc");
        MBeanConstructorInfo c = new MBeanConstructorInfo("Ctor", "ctor desc", new MBeanParameterInfo[]{p1, p2});
        check("Ctor".equals(c.getName()), "getName");
        check("ctor desc".equals(c.getDescription()), "getDescription");
        MBeanParameterInfo[] sig = c.getSignature();
        check(sig.length == 2, "signature length");
        check("p1".equals(sig[0].getName()) && "int".equals(sig[0].getType()), "signature[0]");
        check("p2".equals(sig[1].getName()) && "java.lang.String".equals(sig[1].getType()), "signature[1]");

        // Constructor-based introspection (first arg is description; name is the class name)
        MBeanConstructorInfo viaCtor = new MBeanConstructorInfo("ctor desc", Sample.class.getConstructor(int.class));
        check(Sample.class.getName().equals(viaCtor.getName()), "ctor reflection name");
        check(viaCtor.getSignature().length == 1, "ctor reflection signature length");
        check("int".equals(viaCtor.getSignature()[0].getType()), "ctor reflection param type");

        // descriptor constructor
        ImmutableDescriptor d = new ImmutableDescriptor("name=Ctor", "description=ctor desc");
        MBeanConstructorInfo cd = new MBeanConstructorInfo("Ctor", "ctor desc", new MBeanParameterInfo[0], d);
        check(cd.getDescriptor() != null && "Ctor".equals(cd.getDescriptor().getFieldValue("name")), "descriptor ctor");

        // equals
        check(c.equals(new MBeanConstructorInfo("Ctor", "ctor desc", new MBeanParameterInfo[]{p1, p2})), "equals identical");
        check(!c.equals(new MBeanConstructorInfo("Other", "ctor desc", new MBeanParameterInfo[]{p1, p2})), "different name not equal");
        check(!c.equals(null), "not equal null");
        check(c.hashCode() == new MBeanConstructorInfo("Ctor", "ctor desc", new MBeanParameterInfo[]{p1, p2}).hashCode(), "equal hashCode");

        // clone
        MBeanConstructorInfo clone = (MBeanConstructorInfo) c.clone();
        check(clone != c && clone.equals(c), "clone equals original");
        check(c.toString() != null, "toString non-null");
    }

    public static class Sample {
        public Sample(int x) { }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
