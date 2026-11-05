/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElementRefs conformance: annotation interface and members.
 * @run main XmlElementRefsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlElementRefsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementRefs");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("value"), "has method value");
        testMeta();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementRefs");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 2, "@Target has exactly 2 elements (FIELD, METHOD)");
        java.util.Set<java.lang.annotation.ElementType> ts =
                new java.util.HashSet<java.lang.annotation.ElementType>(java.util.Arrays.asList(et));
        check(ts.contains(java.lang.annotation.ElementType.FIELD), "@Target contains FIELD");
        check(ts.contains(java.lang.annotation.ElementType.METHOD), "@Target contains METHOD");
        check(!ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target does NOT contain PARAMETER");
        check(!ts.contains(java.lang.annotation.ElementType.TYPE), "@Target does NOT contain TYPE");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 1, "exactly 1 declared member (value)");
        check(c.getDeclaredMethod("value").getReturnType() == javax.xml.bind.annotation.XmlElementRef[].class,
                "value() returns XmlElementRef[]");
        check(Modifier.isAbstract(c.getDeclaredMethod("value").getModifiers()), "value() is abstract");
        check(c.getDeclaredMethod("value").getDefaultValue() == null,
                "value() has NO default (at least one @XmlElementRef must be supplied)");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "alpha")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Alpha {
        @javax.xml.bind.annotation.XmlElement
        String v;
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "beta")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Beta {
        @javax.xml.bind.annotation.XmlElement
        String w;
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "multiRefHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class MultiRefHost {
        @javax.xml.bind.annotation.XmlElementRefs({
                @javax.xml.bind.annotation.XmlElementRef(type = Alpha.class, required = false),
                @javax.xml.bind.annotation.XmlElementRef(type = Beta.class, name = "renamed")
        })
        java.util.List<Object> items;
        String note;
    }

    static class OtherRefs {
        @javax.xml.bind.annotation.XmlElementRefs(@javax.xml.bind.annotation.XmlElementRef(type = Alpha.class))
        java.util.List<Alpha> single;
        String none;
    }

    @javax.xml.bind.annotation.XmlElementRefs(@javax.xml.bind.annotation.XmlElementRef(type = Alpha.class))
    static Object annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlElementRefs a =
                MultiRefHost.class.getDeclaredField("items")
                        .getAnnotation(javax.xml.bind.annotation.XmlElementRefs.class);
        check(a != null, "@XmlElementRefs retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlElementRefs.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlElementRef[] vals = a.value();
        check(vals != null && vals.length == 2, "value() array length 2 read back");
        check(vals[0].annotationType() == javax.xml.bind.annotation.XmlElementRef.class,
                "nested entries are @XmlElementRef instances");
        check(vals[0].type() == Alpha.class, "first nested type read back");
        check(!vals[0].required(), "first nested required=false read back");
        check(vals[1].type() == Beta.class, "second nested type read back");
        check("renamed".equals(vals[1].name()), "second nested explicit name read back");
        check(vals[1].required(), "second nested required defaults true");
        javax.xml.bind.annotation.XmlElementRefs m =
                XmlElementRefsTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlElementRefs.class);
        check(m != null, "@XmlElementRefs retrievable from METHOD target");
        check(m.value().length == 1 && m.value()[0].type() == Alpha.class,
                "single-element array form read back from method target");
        check(!a.equals(OtherRefs.class.getDeclaredField("single")
                .getAnnotation(javax.xml.bind.annotation.XmlElementRefs.class)),
                "different nested arrays give non-equal container instances");
        check(MultiRefHost.class.getDeclaredField("note")
                .getAnnotation(javax.xml.bind.annotation.XmlElementRefs.class) == null,
                "unannotated field has no @XmlElementRefs");
        check(MultiRefHost.class.getAnnotation(javax.xml.bind.annotation.XmlElementRefs.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlElementRefs again =
                MultiRefHost.class.getDeclaredField("items")
                        .getAnnotation(javax.xml.bind.annotation.XmlElementRefs.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(MultiRefHost.class, Alpha.class, Beta.class);
        MultiRefHost h = new MultiRefHost();
        Alpha al = new Alpha();
        al.v = "av";
        Beta be = new Beta();
        be.w = "bv";
        h.items = new java.util.ArrayList<Object>(java.util.Arrays.asList(al, be));
        h.note = "n";
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(h, w);
        String xml = w.toString();
        check(xml.contains("<multiRefHost"), "root element present");
        check(xml.contains("<alpha><v>av</v></alpha>"),
                "heterogeneous list entry rendered by its own root-element name");
        check(xml.contains("<beta><w>bv</w></beta>"),
                "second entry also rendered by its own root-element name");
        check(xml.contains("<note>n</note>"), "sibling plain field marshals normally");
        MultiRefHost back = (MultiRefHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.items != null && back.items.size() == 2, "list size round-trip");
        check(back.items.get(0) instanceof Alpha && "av".equals(((Alpha) back.items.get(0)).v),
                "first entry typed round-trip");
        check(back.items.get(1) instanceof Beta && "bv".equals(((Beta) back.items.get(1)).w),
                "second entry typed round-trip");
        check("n".equals(back.note), "note round-trip");
    }
}
