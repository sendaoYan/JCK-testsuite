/*
 * @test
 * @summary javax.xml.bind.annotation.XmlElementWrapper conformance: annotation interface and members.
 * @run main XmlElementWrapperTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlElementWrapperTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementWrapper");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("name"), "has method name");
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("nillable"), "has method nillable");
        check(names.contains("required"), "has method required");
        testMeta();
        testDefaults();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementWrapper");
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
        check(declared.size() == 4, "exactly 4 declared members");
        check(declared.containsAll(java.util.Arrays.asList("name", "namespace", "nillable", "required")),
                "member name set exact");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name() returns String");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        check(c.getDeclaredMethod("nillable").getReturnType() == boolean.class, "nillable() returns boolean");
        check(c.getDeclaredMethod("required").getReturnType() == boolean.class, "required() returns boolean");
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
        }
    }

    static void testDefaults() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlElementWrapper");
        check("##default".equals(c.getDeclaredMethod("name").getDefaultValue()), "name() default \"##default\"");
        check("##default".equals(c.getDeclaredMethod("namespace").getDefaultValue()), "namespace() default \"##default\"");
        check(Boolean.FALSE.equals(c.getDeclaredMethod("nillable").getDefaultValue()), "nillable() default false");
        check(Boolean.FALSE.equals(c.getDeclaredMethod("required").getDefaultValue()), "required() default false");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "wrapHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class WrapHost {
        @javax.xml.bind.annotation.XmlElementWrapper(name = "w", namespace = "urn:w", nillable = true, required = true)
        @javax.xml.bind.annotation.XmlElement(name = "item")
        java.util.List<String> items;
        @javax.xml.bind.annotation.XmlElementWrapper
        @javax.xml.bind.annotation.XmlElement(name = "bit")
        java.util.List<String> bare;
        String note;
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "nullWrapHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class NullWrapHost {
        @javax.xml.bind.annotation.XmlElementWrapper(name = "nw", nillable = true)
        @javax.xml.bind.annotation.XmlElement(name = "item")
        java.util.List<String> items;
    }

    @javax.xml.bind.annotation.XmlElementWrapper(name = "mid")
    static java.util.List<String> annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlElementWrapper full =
                WrapHost.class.getDeclaredField("items").getAnnotation(javax.xml.bind.annotation.XmlElementWrapper.class);
        check(full != null, "@XmlElementWrapper retrievable from FIELD target");
        check("w".equals(full.name()), "explicit name read back");
        check("urn:w".equals(full.namespace()), "explicit namespace read back");
        check(full.nillable(), "explicit nillable=true read back");
        check(full.required(), "explicit required=true read back");
        check(full.annotationType() == javax.xml.bind.annotation.XmlElementWrapper.class, "annotationType() identity");
        check(WrapHost.class.getDeclaredField("items")
                .getAnnotation(javax.xml.bind.annotation.XmlElement.class) != null,
                "wrapper co-exists with @XmlElement for the item mapping");
        javax.xml.bind.annotation.XmlElementWrapper bare =
                WrapHost.class.getDeclaredField("bare").getAnnotation(javax.xml.bind.annotation.XmlElementWrapper.class);
        check("##default".equals(bare.name()), "bare name is \"##default\" (derived from property name)");
        check("##default".equals(bare.namespace()), "bare namespace is \"##default\"");
        check(!bare.nillable(), "bare nillable is false");
        check(!bare.required(), "bare required is false");
        check(!bare.equals(full), "different member values give non-equal instances");
        check(bare.hashCode() != full.hashCode(), "hashCodes differ for different member values");
        javax.xml.bind.annotation.XmlElementWrapper m =
                XmlElementWrapperTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlElementWrapper.class);
        check(m != null, "@XmlElementWrapper retrievable from METHOD target");
        check("mid".equals(m.name()), "method-level name read back");
        check(WrapHost.class.getDeclaredField("note")
                .getAnnotation(javax.xml.bind.annotation.XmlElementWrapper.class) == null,
                "unannotated field has no @XmlElementWrapper");
        check(WrapHost.class.getAnnotation(javax.xml.bind.annotation.XmlElementWrapper.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlElementWrapper again =
                WrapHost.class.getDeclaredField("items").getAnnotation(javax.xml.bind.annotation.XmlElementWrapper.class);
        check(full.equals(again) && full.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(WrapHost.class, NullWrapHost.class);
        javax.xml.bind.Marshaller ma = ctx.createMarshaller();
        WrapHost h = new WrapHost();
        h.items = new java.util.ArrayList<String>(java.util.Arrays.asList("a", "b"));
        h.bare = new java.util.ArrayList<String>(java.util.Collections.singletonList("z"));
        h.note = "n";
        java.io.StringWriter w = new java.io.StringWriter();
        ma.marshal(h, w);
        String xml = w.toString();
        check(xml.contains("<wrapHost"), "root element present");
        check(xml.contains(":w>"), "wrapper element with explicit name and namespace rendered");
        check(xml.contains("urn:w"), "wrapper namespace declared in document");
        check(xml.contains("<item>a</item>"), "first item inside wrapper");
        check(xml.contains("<item>b</item>"), "second item inside wrapper");
        check(xml.indexOf(":w>") < xml.indexOf("<item>a</item>") && xml.indexOf("<item>b</item>") < xml.indexOf("</ns"),
                "items nested inside the wrapper element (open before, close after)");
        check(xml.contains("<bare>"), "bare wrapper uses derived property name");
        check(xml.contains("<bit>z</bit>"), "bare wrapper content rendered");
        check(xml.contains("<note>n</note>"), "sibling plain field marshals normally");
        WrapHost back = (WrapHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.items != null && back.items.size() == 2 && "a".equals(back.items.get(0)) && "b".equals(back.items.get(1)),
                "wrapper items round-trip with order");
        check(back.bare != null && back.bare.size() == 1 && "z".equals(back.bare.get(0)),
                "bare wrapper items round-trip");
        java.io.StringWriter w2 = new java.io.StringWriter();
        ma.marshal(new NullWrapHost(), w2);
        String xml2 = w2.toString();
        check(xml2.contains("<nw xsi:nil=\"true\""),
                "null list under nillable wrapper renders wrapper with xsi:nil and no children");
    }
}
