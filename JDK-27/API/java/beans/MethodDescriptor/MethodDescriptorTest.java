/*
 * @test
 * @summary java.beans.MethodDescriptor conformance: white-box structure + black-box behavior.
 *          Both constructors, getMethod identity, getParameterDescriptors (null for the
 *          1-arg constructor, defensive clone for the 2-arg one), static/Object.toString/
 *          multi-arg methods, setName independence, no public setters, inherited
 *          FeatureDescriptor behavior, identity equals, not serializable.
 * @run main MethodDescriptorTest
 */

import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class MyBean { public String getName() { return null; } }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.MethodDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
        check(c.getConstructors().length == 2, "exactly two public constructors");
        boolean oneArg = false, twoArg = false;
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 1 && p[0] == Method.class) oneArg = true;
            if (p.length == 2 && p[0] == Method.class
                    && p[1] == ParameterDescriptor[].class) twoArg = true;
        }
        check(oneArg, "constructor (Method) present");
        check(twoArg, "constructor (Method, ParameterDescriptor[]) present");
        // no public setters for method / parameter descriptors
        String[] forbidden = {"setMethod", "setParameterDescriptors", "setParamDescriptors"};
        for (String name : forbidden) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(name)) found = true;
            check(!found, "no public " + name);
        }
    }

    static void testBlackBox() throws Exception {
        Method m = String.class.getMethod("substring", int.class);
        MethodDescriptor md = new MethodDescriptor(m);
        check(m == md.getMethod(), "method");

        Method m2 = String.class.getMethod("substring", int.class, int.class);
        ParameterDescriptor[] pd = new ParameterDescriptor[]{ new ParameterDescriptor(), new ParameterDescriptor() };
        MethodDescriptor md2 = new MethodDescriptor(m2, pd);
        check(m2 == md2.getMethod(), "method2");
        check(md2.getParameterDescriptors() != null && md2.getParameterDescriptors().length == 2, "parameterDescriptors");
    }

    static void testParameterDescriptorsContract() throws Exception {
        Method m = String.class.getMethod("substring", int.class, int.class);
        MethodDescriptor one = new MethodDescriptor(m);
        check(one.getParameterDescriptors() == null,
                "getParameterDescriptors null when not supplied");

        ParameterDescriptor p0 = new ParameterDescriptor();
        p0.setName("begin");
        ParameterDescriptor p1 = new ParameterDescriptor();
        p1.setName("end");
        MethodDescriptor md = new MethodDescriptor(m, new ParameterDescriptor[]{ p0, p1 });
        ParameterDescriptor[] got = md.getParameterDescriptors();
        check(got != null && got.length == 2, "length preserved");
        check(got[0] == p0 && got[1] == p1, "element identity preserved");
        check("begin".equals(got[0].getName()) && "end".equals(got[1].getName()),
                "parameter descriptor names visible");
        // defensive copy: mutating the returned array must not affect the descriptor
        got[0] = null;
        check(md.getParameterDescriptors()[0] == p0, "returned array is a defensive copy");
        check(md.getParameterDescriptors() != md.getParameterDescriptors(),
                "each call returns a fresh copy");
        // empty array form
        MethodDescriptor empty = new MethodDescriptor(m, new ParameterDescriptor[0]);
        check(empty.getParameterDescriptors() != null
                && empty.getParameterDescriptors().length == 0, "empty array preserved");
    }

    static void testMethodKinds() throws Exception {
        Method staticM = java.util.Collections.class.getMethod("emptyList");
        MethodDescriptor smd = new MethodDescriptor(staticM);
        check(Modifier.isStatic(smd.getMethod().getModifiers()), "static method accepted");
        check("emptyList".equals(smd.getName()), "static method name");

        Method ts = Object.class.getMethod("toString");
        MethodDescriptor tmd = new MethodDescriptor(ts);
        check("toString".equals(tmd.getName()), "Object.toString accepted");
        check(Object.class == tmd.getMethod().getDeclaringClass(), "declaring class preserved");

        Method noArg = MyBean.class.getMethod("getName");
        MethodDescriptor nmd = new MethodDescriptor(noArg);
        check("getName".equals(nmd.getName()), "name defaults to method name");
        check("getName".equals(nmd.getDisplayName()), "displayName defaults to method name");
        check(nmd.getShortDescription().equals("getName"),
                "shortDescription falls back to method name");
    }

    static void testSetNameIndependence() throws Exception {
        Method m = MyBean.class.getMethod("getName");
        MethodDescriptor md = new MethodDescriptor(m);
        md.setName("alias");
        check("alias".equals(md.getName()), "setName takes effect");
        check("getName".equals(md.getMethod().getName()), "getMethod unaffected by rename");
        check("getName".equals(md.getDisplayName()) == false,
                "displayName stays at original method name after rename");
    }

    static void testInheritedFeatureBehavior() throws Exception {
        Method m = MyBean.class.getMethod("getName");
        MethodDescriptor md = new MethodDescriptor(m);
        check(!md.isExpert() && !md.isHidden() && !md.isPreferred(), "flags default false");
        md.setExpert(true); check(md.isExpert(), "expert toggle");
        md.setHidden(true); check(md.isHidden(), "hidden toggle");
        md.setPreferred(true); check(md.isPreferred(), "preferred toggle");
        md.setDisplayName("Display");
        check("Display".equals(md.getDisplayName()), "displayName set/get");
        md.setShortDescription("Short");
        check("Short".equals(md.getShortDescription()), "shortDescription set/get");
        md.setValue("k", "v");
        check("v".equals(md.getValue("k")), "setValue/getValue inherited");
        check(md.getValue("unknown") == null, "unknown value null");
    }

    static void testEqualsIdentity() throws Exception {
        Method m = MyBean.class.getMethod("getName");
        MethodDescriptor a = new MethodDescriptor(m);
        MethodDescriptor b = new MethodDescriptor(m);
        check(a.equals(a), "equals self");
        check(!a.equals(b), "identity equals (no equals override)");
    }

    static void testToString() throws Exception {
        Method m = MyBean.class.getMethod("getName");
        String s = new MethodDescriptor(m).toString();
        check(s != null, "toString non-null");
        check(s.contains("getName"), "toString contains method name");
    }

    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(MethodDescriptor.class),
                "MethodDescriptor is not Serializable");
        final Method m = MyBean.class.getMethod("getName");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        final java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        try {
            oos.writeObject(new MethodDescriptor(m));
            check(false, "expected NotSerializableException");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testParameterDescriptorsContract();
        testMethodKinds();
        testSetNameIndependence();
        testInheritedFeatureBehavior();
        testEqualsIdentity();
        testToString();
        testNotSerializable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
