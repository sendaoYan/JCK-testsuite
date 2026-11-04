/*
 * @test
 * @summary java.security.AlgorithmConstraints conformance: interface hierarchy and key methods.
 *          Appended: exact 3-overload permits surface (Set+CryptoPrimitive,String,AlgorithmParameters /
 *          Set,Key / Set,String,Key,AlgorithmParameters), deny-all/allow-all/selective recording
 *          implementations invoked with caller arguments verbatim, null-argument tolerance by
 *          implementation contract, real RSA/DSA key decisions, EnumSet usage combinations.
 * @run main AlgorithmConstraintsTest
 */

public class AlgorithmConstraintsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmConstraints");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("permits"), "declares permits");

        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        // exact overload surface: three permits(...) methods with precise parameter types
        java.lang.reflect.Method[] ms = java.security.AlgorithmConstraints.class.getDeclaredMethods();
        check(ms.length == 3, "exactly 3 permits overloads");
        boolean sig1 = false, sig2 = false, sig3 = false;
        for (java.lang.reflect.Method m : ms) {
            Class<?>[] p = m.getParameterTypes();
            if (m.getName().equals("permits") && p.length == 3
                    && p[0] == java.util.Set.class && p[1] == String.class
                    && p[2] == java.security.AlgorithmParameters.class) sig1 = true;
            if (m.getName().equals("permits") && p.length == 2
                    && p[0] == java.util.Set.class && p[1] == java.security.Key.class) sig2 = true;
            if (m.getName().equals("permits") && p.length == 4
                    && p[0] == java.util.Set.class && p[1] == String.class
                    && p[2] == java.security.Key.class
                    && p[3] == java.security.AlgorithmParameters.class) sig3 = true;
            check(m.getReturnType() == boolean.class, "permits returns boolean: " + m);
        }
        check(sig1, "overload permits(Set,String,AlgorithmParameters)");
        check(sig2, "overload permits(Set,Key)");
        check(sig3, "overload permits(Set,String,Key,AlgorithmParameters)");
        check(!ms[0].isDefault(), "no default implementations");

        // recording implementation: arguments delivered verbatim to the implementation
        final java.util.List<String> log = new java.util.ArrayList<String>();
        java.security.AlgorithmConstraints recording = new java.security.AlgorithmConstraints() {
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> usage, String algorithm,
                    java.security.AlgorithmParameters specs) {
                log.add("s:" + algorithm);
                return true;
            }
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> usage, java.security.Key key) {
                log.add("k:" + key.getAlgorithm());
                return true;
            }
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> usage, String algorithm,
                    java.security.Key key, java.security.AlgorithmParameters specs) {
                log.add("sk:" + algorithm + ":" + key.getAlgorithm());
                return true;
            }
        };
        java.util.Set<java.security.CryptoPrimitive> sig =
                java.util.EnumSet.of(java.security.CryptoPrimitive.SIGNATURE);
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair kp = kpg.generateKeyPair();
        check(recording.permits(sig, "SHA256withRSA", (java.security.AlgorithmParameters) null),
                "string overload returns impl value");
        check(recording.permits(sig, kp.getPublic()), "key overload returns impl value");
        check(recording.permits(sig, "RSA", kp.getPublic(), null), "key+alg overload returns impl value");
        check(log.equals(java.util.Arrays.asList("s:SHA256withRSA", "k:RSA", "sk:RSA:RSA")),
                "arguments delivered verbatim in order: " + log);

        // deny-all / allow-all
        java.security.AlgorithmConstraints denyAll = new java.security.AlgorithmConstraints() {
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, String a,
                    java.security.AlgorithmParameters s) { return false; }
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, java.security.Key k) { return false; }
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, String a,
                    java.security.Key k, java.security.AlgorithmParameters s) { return false; }
        };
        check(!denyAll.permits(sig, "SHA256withRSA", (java.security.AlgorithmParameters) null), "deny string");
        check(!denyAll.permits(sig, kp.getPublic()), "deny key-only");
        check(!denyAll.permits(sig, "RSA", kp.getPublic(), null), "deny key+alg");
        check(!denyAll.permits(null, "X", (java.security.AlgorithmParameters) null),
                "null usage set tolerated by impl");
        java.security.AlgorithmConstraints allowAll = new java.security.AlgorithmConstraints() {
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, String a,
                    java.security.AlgorithmParameters s) { return true; }
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, java.security.Key k) { return true; }
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, String a,
                    java.security.Key k, java.security.AlgorithmParameters s) { return true; }
        };
        check(allowAll.permits(sig, "anything", (java.security.AlgorithmParameters) null), "allow string");
        check(allowAll.permits(java.util.EnumSet.allOf(java.security.CryptoPrimitive.class), kp.getPublic()),
                "allow key with full usage set");

        // selective constraints: algorithm prefixes and key algorithms decide independently
        java.security.AlgorithmConstraints selective = new java.security.AlgorithmConstraints() {
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, String a,
                    java.security.AlgorithmParameters s) { return a != null && a.startsWith("SHA"); }
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, java.security.Key k) {
                return "RSA".equals(k.getAlgorithm());
            }
            public boolean permits(java.util.Set<java.security.CryptoPrimitive> u, String a,
                    java.security.Key k, java.security.AlgorithmParameters s) {
                return "RSA".equals(a) && "RSA".equals(k.getAlgorithm());
            }
        };
        check(selective.permits(sig, "SHA256withRSA", (java.security.AlgorithmParameters) null), "SHA allowed");
        check(!selective.permits(sig, "MD5withRSA", (java.security.AlgorithmParameters) null), "MD5 denied");
        check(!selective.permits(sig, null, (java.security.AlgorithmParameters) null), "null algorithm denied by impl");
        check(selective.permits(sig, kp.getPublic()), "RSA key allowed");
        check(selective.permits(sig, "RSA", kp.getPublic(), null), "RSA key+alg allowed");
        java.security.KeyPairGenerator dsa = java.security.KeyPairGenerator.getInstance("DSA");
        dsa.initialize(1024);
        java.security.Key dsaPub = dsa.generateKeyPair().getPublic();
        check(!selective.permits(sig, dsaPub), "DSA key denied");
        check(!selective.permits(sig, "DSA", dsaPub, null), "DSA key+alg denied");
    }
}
