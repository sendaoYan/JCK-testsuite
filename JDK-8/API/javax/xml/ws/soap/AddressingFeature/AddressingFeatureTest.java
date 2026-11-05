/*
 * @test
 * @summary javax.xml.ws.soap.AddressingFeature conformance: constructor matrix (enabled/required/responses), ID constant, defaults and independence across four constructors.
 * @run main AddressingFeatureTest
 */

import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.AddressingFeature.Responses;

public class AddressingFeatureTest {
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
        javax.xml.ws.soap.AddressingFeature f = new javax.xml.ws.soap.AddressingFeature();
        check("http://www.w3.org/2005/08/addressing/module".equals(f.getID()), "id");
        check(f.isEnabled(), "enabled");
        check(f.getResponses() != null, "responses non-null");
        testClassFace();
        testConstructorMatrix();
        testIdAndDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 类面：final，继承 WebServiceFeature，恰 4 个公共构造器
    static void testClassFace() throws Exception {
        Class<?> c = AddressingFeature.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == WebServiceFeature.class, "extends WebServiceFeature");
        check(WebServiceFeature.class.isAssignableFrom(c), "assignable to WebServiceFeature");
        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 4, "exactly 4 public constructors: " + ctors.length);
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(boolean.class) != null, "(boolean) ctor");
        check(c.getConstructor(boolean.class, boolean.class) != null, "(boolean,boolean) ctor");
        check(c.getConstructor(boolean.class, boolean.class, Responses.class) != null,
              "(boolean,boolean,Responses) ctor");
        // 无 (Responses) 单独构造器
        try {
            c.getConstructor(Responses.class);
            check(false, "no (Responses)-only ctor");
        } catch (NoSuchMethodException e) {
            check(true, "no (Responses)-only ctor (NoSuchMethodException)");
        }
    }

    // 构造矩阵：enabled/required/responses 全组合读回
    static void testConstructorMatrix() {
        AddressingFeature d = new AddressingFeature();
        check(d.isEnabled(), "() enabled true");
        check(!d.isRequired(), "() required false");
        check(d.getResponses() == Responses.ALL, "() responses ALL");

        AddressingFeature t = new AddressingFeature(true);
        check(t.isEnabled(), "(true) enabled true");
        check(!t.isRequired(), "(true) required false");
        check(t.getResponses() == Responses.ALL, "(true) responses ALL");

        AddressingFeature fl = new AddressingFeature(false);
        check(!fl.isEnabled(), "(false) enabled false");
        check(!fl.isRequired(), "(false) required false");
        check(fl.getResponses() == Responses.ALL, "(false) responses ALL");

        AddressingFeature tt = new AddressingFeature(true, true);
        check(tt.isEnabled() && tt.isRequired(), "(true,true) both true");
        check(tt.getResponses() == Responses.ALL, "(true,true) responses ALL");

        AddressingFeature tf = new AddressingFeature(true, false);
        check(tf.isEnabled() && !tf.isRequired(), "(true,false) enabled only");

        AddressingFeature ft = new AddressingFeature(false, true);
        check(!ft.isEnabled() && ft.isRequired(), "(false,true) required only");

        AddressingFeature anon = new AddressingFeature(true, false, Responses.ANONYMOUS);
        check(anon.getResponses() == Responses.ANONYMOUS, "responses ANONYMOUS");
        check(anon.isEnabled() && !anon.isRequired(), "responses ctor keeps flags");

        AddressingFeature non = new AddressingFeature(true, false, Responses.NON_ANONYMOUS);
        check(non.getResponses() == Responses.NON_ANONYMOUS, "responses NON_ANONYMOUS");

        AddressingFeature all = new AddressingFeature(false, true, Responses.ALL);
        check(all.getResponses() == Responses.ALL, "explicit ALL");
        check(!all.isEnabled() && all.isRequired(), "explicit ALL keeps flags");
        // 三值均可且互异
        check(anon.getResponses() != non.getResponses() && non.getResponses() != all.getResponses(),
              "three responses values distinct");
        // getResponses 永不为 null
        for (Responses r : Responses.values()) {
            check(new AddressingFeature(true, false, r).getResponses() == r,
                  "responses round-trip " + r);
        }
    }

    // ID 与默认一致性
    static void testIdAndDefaults() {
        check(AddressingFeature.ID.equals("http://www.w3.org/2005/08/addressing/module"),
              "ID field exact: " + AddressingFeature.ID);
        check(new AddressingFeature().getID().equals(AddressingFeature.ID), "getID equals ID");
        check(new AddressingFeature(false).getID().equals(AddressingFeature.ID),
              "getID independent of enabled");
        // 与 MTOMFeature ID 互异
        check(!AddressingFeature.ID.equals(javax.xml.ws.soap.MTOMFeature.ID),
              "addressing id distinct from MTOM id");
        // enabled 与 required 语义独立
        AddressingFeature x = new AddressingFeature(true, true);
        check(x.isEnabled() && x.isRequired(), "flags coexist");
        check(new AddressingFeature().isEnabled() != new AddressingFeature(false).isEnabled(),
              "enabled ctor governs isEnabled");
    }
}
