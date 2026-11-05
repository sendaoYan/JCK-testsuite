/*
 * @test
 * @summary javax.xml.bind.annotation.XmlList conformance: annotation interface and members.
 * @run main XmlListTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlListTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlList");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        testMeta();
        testReadBack();
        testMarshalList();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlList");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME,
                "@Retention(RUNTIME) - required for JAXB runtime processing");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 3, "@Target has exactly 3 elements (FIELD, METHOD, PARAMETER)");
        java.util.Set<java.lang.annotation.ElementType> ts =
                new java.util.HashSet<java.lang.annotation.ElementType>(java.util.Arrays.asList(et));
        check(ts.contains(java.lang.annotation.ElementType.FIELD), "@Target contains FIELD");
        check(ts.contains(java.lang.annotation.ElementType.METHOD), "@Target contains METHOD");
        check(ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target contains PARAMETER");
        check(!ts.contains(java.lang.annotation.ElementType.TYPE), "@Target does NOT contain TYPE");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no declared members");
        check(c == javax.xml.bind.annotation.XmlList.class, "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class ListRoot {
        @javax.xml.bind.annotation.XmlElement
        @javax.xml.bind.annotation.XmlList
        java.util.List<String> words;
        String untouched;
    }

    static class ScalarList {
        @javax.xml.bind.annotation.XmlList
        String single;
    }

    @javax.xml.bind.annotation.XmlList
    static java.util.List<String> annotatedMethod() { return null; }

    static void takesParam(@javax.xml.bind.annotation.XmlList java.util.List<String> p) { }

    static void testReadBack() throws Exception {
        java.lang.reflect.Field f = ListRoot.class.getDeclaredField("words");
        javax.xml.bind.annotation.XmlList a = f.getAnnotation(javax.xml.bind.annotation.XmlList.class);
        check(a != null, "@XmlList retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlList.class, "annotationType() identity");
        java.lang.reflect.Field s = ScalarList.class.getDeclaredField("single");
        check(s.getAnnotation(javax.xml.bind.annotation.XmlList.class) != null, "@XmlList also applicable to scalar String field syntactically");
        java.lang.reflect.Field u = ListRoot.class.getDeclaredField("untouched");
        check(u.getAnnotation(javax.xml.bind.annotation.XmlList.class) == null, "unannotated field has no @XmlList");
        javax.xml.bind.annotation.XmlList m = XmlListTest.class.getDeclaredMethod("annotatedMethod")
                .getAnnotation(javax.xml.bind.annotation.XmlList.class);
        check(m != null, "@XmlList retrievable from METHOD target");
        check(m.equals(a), "marker instances from different targets are equals()");
        check(m.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        check(a.toString().contains("XmlList"), "toString mentions XmlList");
        boolean paramFound = false;
        for (java.lang.reflect.Method mm : XmlListTest.class.getDeclaredMethods()) {
            if (mm.getName().equals("takesParam")) {
                for (java.lang.annotation.Annotation pa : mm.getParameterAnnotations()[0]) {
                    if (pa instanceof javax.xml.bind.annotation.XmlList) paramFound = true;
                }
            }
        }
        check(paramFound, "@XmlList observable on PARAMETER target");
        check(ListRoot.class.getAnnotation(javax.xml.bind.annotation.XmlList.class) == null,
                "@XmlList cannot be applied to TYPE (read-back of unannotated class is null)");
    }

    static void testMarshalList() throws Exception {
        ListRoot r = new ListRoot();
        r.words = new java.util.ArrayList<String>(java.util.Arrays.asList("alpha", "beta", "gamma"));
        r.untouched = "plain";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(ListRoot.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(r, w);
        String xml = w.toString();
        check(xml.contains("<listRoot>"), "root element derived from class name");
        check(xml.contains("<words>alpha beta gamma</words>"),
                "@XmlList List<String> marshalled as space-separated single element");
        check(!xml.contains("<words>alpha</words>"), "list is NOT one element per item");
        check(xml.contains("<untouched>plain</untouched>"), "plain (non-@XmlList) field marshals normally");
        ListRoot back = (ListRoot) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.words != null && back.words.size() == 3, "unmarshal restores 3 list items");
        check("alpha".equals(back.words.get(0)) && "beta".equals(back.words.get(1)) && "gamma".equals(back.words.get(2)),
                "round-trip list contents exact and order preserved");
        ListRoot tab = (ListRoot) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(
                "<?xml version=\"1.0\"?><listRoot><words>one\ttwo  three</words></listRoot>"));
        check(tab.words.size() == 3 && "one".equals(tab.words.get(0)) && "two".equals(tab.words.get(1))
                && "three".equals(tab.words.get(2)), "lexing splits on any whitespace (tab and double space)");
        try {
            javax.xml.bind.JAXBContext.newInstance(ScalarList.class);
            check(false, "@XmlList on single-value property must be rejected at context creation");
        } catch (javax.xml.bind.JAXBException e) {
            check(e.getMessage() != null && e.getMessage().contains("IllegalAnnotationExceptions"),
                    "scalar placement rejected with IllegalAnnotationExceptions summary message");
        }
    }
}
