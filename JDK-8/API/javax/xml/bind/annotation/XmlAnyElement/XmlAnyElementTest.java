/*
 * @test
 * @summary javax.xml.bind.annotation.XmlAnyElement conformance: annotation interface and members.
 * @run main XmlAnyElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlAnyElementTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAnyElement");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("lax"), "has method lax");
        check(names.contains("value"), "has method value");
        testMeta();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAnyElement");
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
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 2, "exactly 2 declared members (lax, value)");
        check(declared.containsAll(java.util.Arrays.asList("lax", "value")), "member name set exact");
        check(c.getDeclaredMethod("lax").getReturnType() == boolean.class, "lax() returns boolean");
        check(c.getDeclaredMethod("value").getReturnType() == Class.class, "value() returns Class");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
        }
        check(javax.xml.bind.annotation.DomHandler.class.isAssignableFrom(
                javax.xml.bind.annotation.W3CDomHandler.class), "default value() W3CDomHandler implements DomHandler");
        check("false".equals(c.getDeclaredMethod("lax").getDefaultValue() == Boolean.FALSE ? "false" : "true"),
                "lax() default is false");
        check(c.getDeclaredMethod("value").getDefaultValue() == javax.xml.bind.annotation.W3CDomHandler.class,
                "value() default is W3CDomHandler.class");
    }

    static class MyDomHandler extends javax.xml.bind.annotation.W3CDomHandler { }

    @javax.xml.bind.annotation.XmlRootElement(name = "laxRoot")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class LaxRoot {
        @javax.xml.bind.annotation.XmlAnyElement(lax = true)
        Object any;
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "strictRoot")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class StrictRoot {
        @javax.xml.bind.annotation.XmlAnyElement
        org.w3c.dom.Element el;
        @javax.xml.bind.annotation.XmlAnyElement(lax = false, value = MyDomHandler.class)
        org.w3c.dom.Element custom;
        String plain;
    }

    @javax.xml.bind.annotation.XmlAnyElement
    static org.w3c.dom.Element annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlAnyElement bare =
                StrictRoot.class.getDeclaredField("el").getAnnotation(javax.xml.bind.annotation.XmlAnyElement.class);
        check(bare != null, "bare @XmlAnyElement retrievable from field");
        check(!bare.lax(), "bare lax() defaults to false");
        check(bare.value() == javax.xml.bind.annotation.W3CDomHandler.class, "bare value() defaults to W3CDomHandler");
        check(bare.annotationType() == javax.xml.bind.annotation.XmlAnyElement.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlAnyElement full =
                StrictRoot.class.getDeclaredField("custom").getAnnotation(javax.xml.bind.annotation.XmlAnyElement.class);
        check(full != null, "explicit @XmlAnyElement(lax=false, value=MyDomHandler.class) retrievable");
        check(!full.lax(), "explicit lax=false read back");
        check(full.value() == MyDomHandler.class, "custom DomHandler subclass read back as value()");
        javax.xml.bind.annotation.XmlAnyElement lax =
                LaxRoot.class.getDeclaredField("any").getAnnotation(javax.xml.bind.annotation.XmlAnyElement.class);
        check(lax != null && lax.lax(), "lax=true read back");
        check(lax.value() == javax.xml.bind.annotation.W3CDomHandler.class, "lax variant still has default value()");
        check(lax != bare, "different member values give non-equal annotation instances");
        check(StrictRoot.class.getDeclaredField("plain").getAnnotation(javax.xml.bind.annotation.XmlAnyElement.class) == null,
                "unannotated field has no @XmlAnyElement");
        javax.xml.bind.annotation.XmlAnyElement m =
                XmlAnyElementTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlAnyElement.class);
        check(m != null, "@XmlAnyElement retrievable from METHOD target");
        check(m.equals(bare), "method-level bare instance equals field-level bare instance");
        check(m.hashCode() == bare.hashCode(), "hashCode consistent for equal instances");
        check(StrictRoot.class.getAnnotation(javax.xml.bind.annotation.XmlAnyElement.class) == null,
                "class-level retrieval is null (TYPE not allowed)");
        javax.xml.bind.annotation.XmlAnyElement again =
                StrictRoot.class.getDeclaredField("el").getAnnotation(javax.xml.bind.annotation.XmlAnyElement.class);
        check(bare.equals(again) && bare.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "known")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Known {
        @javax.xml.bind.annotation.XmlElement
        String k;
    }

    static void testMarshalLoop() throws Exception {
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new org.xml.sax.InputSource(new java.io.StringReader("<sub k=\"1\">t</sub>")));
        StrictRoot sr = new StrictRoot();
        sr.el = d.getDocumentElement();
        sr.custom = d.getDocumentElement();
        sr.plain = "p";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(StrictRoot.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(sr, w);
        String xml = w.toString();
        check(xml.contains("<strictRoot>"), "root element present");
        check(xml.contains("<sub k=\"1\">t</sub>"), "wildcard DOM element copied verbatim (attrs + text)");
        check(xml.indexOf("<sub") != xml.lastIndexOf("<sub"), "two wildcard fields each render their copy");
        check(xml.contains("<plain>p</plain>"), "coexisting plain field marshals normally");
        StrictRoot back = (StrictRoot) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.el != null && "sub".equals(back.el.getLocalName()), "unmarshal restores DOM element");
        check("1".equals(back.el.getAttribute("k")), "attribute restored on wildcard element");
        check("t".equals(back.el.getTextContent()), "text restored on wildcard element");
        // lax=true: known @XmlRootElement instance marshals as its own element and unmarshals to the class
        LaxRoot lr = new LaxRoot();
        Known k = new Known();
        k.k = "kv";
        lr.any = k;
        javax.xml.bind.JAXBContext ctx2 = javax.xml.bind.JAXBContext.newInstance(LaxRoot.class, Known.class);
        java.io.StringWriter w2 = new java.io.StringWriter();
        ctx2.createMarshaller().marshal(lr, w2);
        String xml2 = w2.toString();
        check(xml2.contains("<known><k>kv</k></known>"), "lax wildcard renders known instance as its root element");
        LaxRoot back2 = (LaxRoot) ctx2.createUnmarshaller().unmarshal(new java.io.StringReader(xml2));
        check(back2.any instanceof Known, "lax unmarshal binds to the known class (not DOM)");
        check(back2.any instanceof org.w3c.dom.Element == false, "lax result is not a DOM Element");
        check("kv".equals(((Known) back2.any).k), "lax round-trip field value exact");
    }
}
