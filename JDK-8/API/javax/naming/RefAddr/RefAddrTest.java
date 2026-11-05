/*
 * @test
 * @summary javax.naming.RefAddr conformance: abstract class hierarchy and
 *          members, plus behavioral equals/hashCode/toString using a concrete
 *          subclass.
 * @run main RefAddrTest
 */

import javax.naming.RefAddr;
import javax.naming.BinaryRefAddr;
import javax.naming.StringRefAddr;

public class RefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class TestAddr extends RefAddr {
        TestAddr(String type, String content) { super(type); this.content = content; }
        String content;
        public Object getContent() { return content; }
        private static final long serialVersionUID = 1L;
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testHierarchy() throws Exception {
        Class<?> c = Class.forName("javax.naming.RefAddr");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.io.Serializable");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContent"), "has method getContent");
        check(names.contains("getType"), "has method getType");
    }

    static void testBehavioral() {
        TestAddr a = new TestAddr("TYPE", "VAL");
        check("TYPE".equals(a.getType()), "getType");
        check("VAL".equals(a.getContent()), "getContent");
        TestAddr same = new TestAddr("TYPE", "VAL");
        TestAddr diffType = new TestAddr("TYPE2", "VAL");
        TestAddr diffContent = new TestAddr("TYPE", "VAL2");
        check(a.equals(same) && same.equals(a), "equals symmetric");
        check(a.hashCode() == same.hashCode(), "hashCode equal");
        check(!a.equals(diffType), "different type");
        check(!a.equals(diffContent), "different content");
        check(!a.equals(null) && !a.equals("TYPE"), "equals null/non-RefAddr");
        String s = a.toString();
        check(s.contains("Type: TYPE"), "toString type");
        check(s.contains("Content: VAL"), "toString content");
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testBehavioral();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dRaEqualityMatrix();
        dRaToStringAndHash();
        dRaSerialization();
        System.out.println("deep: RefAddr contracts OK");
    }

    static void dRaEqualityMatrix() {
        StringRefAddr a = new StringRefAddr("type", "content");
        StringRefAddr b = new StringRefAddr("type", "content");
        check(a.equals(b) && b.equals(a), "same type+content equal");
        check(a.hashCode() == b.hashCode(), "equal implies equal hashCode");
        check(!a.equals(new StringRefAddr("other", "content")), "different type not equal");
        check(!a.equals(new StringRefAddr("type", "other")), "different content not equal");
        check(!a.equals(null), "equals null false");
        check(!a.equals("type/content"), "equals non-RefAddr false");
        BinaryRefAddr bin = new BinaryRefAddr("type", new byte[] { 49 });
        check(!a.equals(bin) && !bin.equals(a), "String and Binary RefAddr never equal");
        StringRefAddr nullC1 = new StringRefAddr("t", null);
        StringRefAddr nullC2 = new StringRefAddr("t", null);
        check(nullC1.equals(nullC2), "null contents equal each other");
        check(!nullC1.equals(a), "null content != non-null content");
        check(!a.equals(nullC1), "non-null content != null content");
    }

    static void dRaToStringAndHash() {
        StringRefAddr s = new StringRefAddr("t", "c");
        String text = s.toString();
        check(text.contains("t") && text.contains("c"), "toString shows type and content");
        StringRefAddr nullC = new StringRefAddr("t", null);
        int h = nullC.hashCode();
        check(true, "hashCode with null content does not throw: " + h);
        check(new StringRefAddr("t", "x").getType().equals("t"), "getType");
        check(new StringRefAddr(null, "x").getType() == null, "null type stored as-is");
    }

    static void dRaSerialization() throws Exception {
        StringRefAddr s = new StringRefAddr("t", "c");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(s); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            RefAddr back = (RefAddr) oi.readObject();
            check(back.equals(s), "RefAddr serialization round trip equal");
            check("t".equals(back.getType()) && "c".equals(back.getContent()),
                    "type and content survive serialization");
        }
    }

}
