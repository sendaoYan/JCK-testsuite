/*
 * @test
 * @summary javax.xml.ws.soap.MTOMFeature conformance: constructor matrix (enabled/threshold), default threshold 0, negative threshold WebServiceException with exact message.
 * @run main MTOMFeatureTest
 */

import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.MTOMFeature;

public class MTOMFeatureTest {
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
        javax.xml.ws.soap.MTOMFeature f = new javax.xml.ws.soap.MTOMFeature();
        check("http://www.w3.org/2004/08/soap/features/http-optimization".equals(f.getID()), "id");
        check(f.isEnabled(), "enabled");
        check(f.getThreshold() == 0, "default threshold");
        testClassFace();
        testConstructorMatrix();
        testNegativeThreshold();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 类面：final，继承 WebServiceFeature，恰 4 个构造器
    static void testClassFace() throws Exception {
        Class<?> c = MTOMFeature.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == WebServiceFeature.class, "extends WebServiceFeature");
        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 4, "exactly 4 public constructors: " + ctors.length);
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(boolean.class) != null, "(boolean) ctor");
        check(c.getConstructor(int.class) != null, "(int) ctor");
        check(c.getConstructor(boolean.class, int.class) != null, "(boolean,int) ctor");
        try {
            c.getConstructor(int.class, boolean.class);
            check(false, "no (int,boolean) ctor");
        } catch (NoSuchMethodException e) {
            check(true, "no (int,boolean) ctor (order fixed)");
        }
    }

    // 构造矩阵：enabled/threshold 读回
    static void testConstructorMatrix() {
        MTOMFeature d = new MTOMFeature();
        check(d.isEnabled(), "() enabled true");
        check(d.getThreshold() == 0, "() threshold 0");

        MTOMFeature t = new MTOMFeature(true);
        check(t.isEnabled() && t.getThreshold() == 0, "(true) enabled, threshold 0");

        MTOMFeature fl = new MTOMFeature(false);
        check(!fl.isEnabled() && fl.getThreshold() == 0, "(false) disabled, threshold 0");

        MTOMFeature thr = new MTOMFeature(3000);
        check(thr.isEnabled(), "(3000) enabled true by default");
        check(thr.getThreshold() == 3000, "(3000) threshold 3000");

        MTOMFeature bt = new MTOMFeature(false, 5000);
        check(!bt.isEnabled(), "(false,5000) disabled");
        check(bt.getThreshold() == 5000, "(false,5000) threshold 5000");

        MTOMFeature b2 = new MTOMFeature(true, 5000);
        check(b2.isEnabled() && b2.getThreshold() == 5000, "(true,5000) both set");

        // 零阈值合法
        check(new MTOMFeature(0).getThreshold() == 0, "explicit 0 threshold legal");
        // 大阈值合法
        MTOMFeature max = new MTOMFeature(Integer.MAX_VALUE);
        check(max.getThreshold() == Integer.MAX_VALUE, "Integer.MAX_VALUE threshold");
        // getID 恒定
        check(MTOMFeature.ID.equals("http://www.w3.org/2004/08/soap/features/http-optimization"),
              "ID field exact: " + MTOMFeature.ID);
        check(new MTOMFeature(false).getID().equals(MTOMFeature.ID), "getID constant across ctors");
        check(!MTOMFeature.ID.equals(javax.xml.ws.soap.AddressingFeature.ID),
              "MTOM id distinct from Addressing id");
    }

    // 负例：负阈值 -> WebServiceException（消息含 must be >= 0），两种构造形态一致
    static void testNegativeThreshold() {
        try {
            new MTOMFeature(-1);
            check(false, "negative threshold must throw");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("must be >= 0"),
                  "MTOMFeature(-1) message: " + e.getMessage());
            check(e.getMessage().contains("-1"), "message carries actual value");
        }
        try {
            new MTOMFeature(true, -2);
            check(false, "negative threshold (2-arg) must throw");
        } catch (WebServiceException e) {
            check(e.getMessage() != null && e.getMessage().contains("must be >= 0"),
                  "MTOMFeature(true,-2) message: " + e.getMessage());
        }
        try {
            new MTOMFeature(Integer.MIN_VALUE);
            check(false, "MIN_VALUE threshold must throw");
        } catch (WebServiceException e) {
            check(true, "MIN_VALUE -> WebServiceException");
        }
        // 既有合法实例不受先前负例影响
        check(new MTOMFeature(100).getThreshold() == 100, "valid ctor still fine after negatives");
        // 特性独立性与可变性面（WebServiceFeature 基类语义）
        MTOMFeature a = new MTOMFeature(1000);
        MTOMFeature b = new MTOMFeature(2000);
        check(a != b && !a.equals(b), "identity semantics (equals not overridden)");
        check(a.isEnabled() == b.isEnabled(), "enabled default identical across instances");
        check(a.getThreshold() != b.getThreshold(), "thresholds independent per instance");
        // 两次读稳定
        check(a.getThreshold() == 1000 && a.getThreshold() == 1000, "getThreshold stable");
        check(a.getID().equals(b.getID()), "getID identical across instances");
        // 可作 WebServiceFeature 数组元素传递
        javax.xml.ws.WebServiceFeature[] fs = new javax.xml.ws.WebServiceFeature[] { a, b };
        check(fs.length == 2 && fs[0] instanceof MTOMFeature, "usable in feature arrays");
        // MTOM 注解默认值与特性互通（@MTOM enabled 默认 true、threshold 默认 0）
        MTOM ann = MTOMTestAnchor.class.getAnnotation(MTOM.class);
        check(ann.enabled(), "@MTOM default enabled matches feature default");
        check(ann.threshold() == 0, "@MTOM default threshold matches feature default");
        check(new MTOMFeature().isEnabled() == ann.enabled()
              && new MTOMFeature().getThreshold() == ann.threshold(),
              "no-arg feature equals @MTOM defaults");
    }

    @MTOM
    static class MTOMTestAnchor { }
}
