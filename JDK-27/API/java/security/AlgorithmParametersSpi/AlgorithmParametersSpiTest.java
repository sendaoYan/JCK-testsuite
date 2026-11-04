/*
 * @test
 * @summary java.security.AlgorithmParametersSpi conformance: white-box structure + black-box behavior.
 * @run main AlgorithmParametersSpiTest
 */

import java.lang.reflect.Modifier;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class AlgorithmParametersSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmParametersSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
    }

    static void testBlackBox() throws Exception {
        MyApSpi ap = new MyApSpi();
        ap.doInit(new DummySpec());
        ap.doInit(new byte[]{1});
        ap.doInit(new byte[]{1}, "RAW");
        check(Arrays.equals(ap.doGetEncoded(), new byte[]{7}), "engineGetEncoded");
        check(Arrays.equals(ap.doGetEncoded("RAW"), new byte[]{7}), "engineGetEncoded(format)");
        check("MyApSpi".equals(ap.doToString()), "engineToString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static class MyApSpi extends AlgorithmParametersSpi {
        protected void engineInit(AlgorithmParameterSpec paramSpec) throws java.security.spec.InvalidParameterSpecException { }
        protected void engineInit(byte[] params) throws java.io.IOException { }
        protected void engineInit(byte[] params, String format) throws java.io.IOException { }
        protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec) throws java.security.spec.InvalidParameterSpecException { throw new java.security.spec.InvalidParameterSpecException(); }
        protected byte[] engineGetEncoded() throws java.io.IOException { return new byte[]{7}; }
        protected byte[] engineGetEncoded(String format) throws java.io.IOException { return new byte[]{7}; }
        protected String engineToString() { return "MyApSpi"; }
        void doInit(AlgorithmParameterSpec ps) throws java.security.spec.InvalidParameterSpecException { engineInit(ps); }
        void doInit(byte[] b) throws java.io.IOException { engineInit(b); }
        void doInit(byte[] b, String f) throws java.io.IOException { engineInit(b, f); }
        byte[] doGetEncoded() throws java.io.IOException { return engineGetEncoded(); }
        byte[] doGetEncoded(String f) throws java.io.IOException { return engineGetEncoded(f); }
        String doToString() { return engineToString(); }
    }

    static class DummySpec implements AlgorithmParameterSpec { }
}
