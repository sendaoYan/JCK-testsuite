/*
 * @test
 * @summary java.security.Provider conformance: full contract coverage.
 *          abstract class hierarchy, subclass construction + getName/getVersion/
 *          getInfo, Hashtable put/get/remove/containsKey/containsValue/size with
 *          null key/value -> NPE, legacy keys()/elements(), getService (null for
 *          unregistered, Service metadata for registered), getServices(), toString,
 *          equals/hashCode, and serialization round-trip preserving name/version/info.
 * @run main ProviderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Provider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class TestProvider extends Provider {
        TestProvider(String name, double version, String info) { super(name, version, info); }
        void putSvc(Provider.Service s) { putService(s); }
        void removeSvc(Provider.Service s) { removeService(s); }
    }

    /** public engine implementation usable by Service.newInstance (public class + ctor) */
    public static class MyDigest extends java.security.MessageDigest {
        public MyDigest() { super("MyDigest"); }
        protected void engineUpdate(byte b) { }
        protected void engineUpdate(byte[] b, int off, int len) { }
        protected byte[] engineDigest() { return new byte[16]; }
        protected void engineReset() { }
    }

    public static void main(String[] args) throws Exception {
        // ---- abstract class hierarchy ----
        Class<?> c = Class.forName("java.security.Provider");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(),
                "Provider is abstract class");
        check(java.util.Properties.class.isAssignableFrom(c), "Provider extends Properties");

        // ---- subclass construction + getters ----
        TestProvider p = new TestProvider("TestProv", 1.5, "test info string");
        check("TestProv".equals(p.getName()), "getName");
        check(p.getVersion() == 1.5, "getVersion");
        check("test info string".equals(p.getInfo()), "getInfo");

        // ---- Hashtable map operations ----
        check(p.put("key1", "value1") == null, "put returns previous null");
        check("value1".equals(p.put("key1", "value2")), "put returns previous value");
        check("value2".equals(p.get("key1")), "get round-trip");
        check(p.containsKey("key1") && p.containsValue("value2"), "containsKey/containsValue");
        check("value2".equals(p.remove("key1")), "remove returns value");
        check(!p.containsKey("key1"), "remove deletes");

        // ---- null key/value -> NPE ----
        final TestProvider pn = new TestProvider("Pn", 1.0, "i");
        checkThrows(NullPointerException.class, () -> pn.put(null, "v"), "put(null key)");
        checkThrows(NullPointerException.class, () -> pn.put("k", null), "put(null value)");

        // ---- legacy keys()/elements() ----
        TestProvider pe = new TestProvider("Pe", 1.0, "i");
        pe.put("k1", "v1");
        pe.put("k2", "v2");
        Set<Object> keys = new HashSet<>();
        for (Object o : Collections.list(pe.keys())) keys.add(o);
        check(keys.contains("k1") && keys.contains("k2"), "keys() enumeration");
        Set<Object> values = new HashSet<>();
        for (Object o : Collections.list(pe.elements())) values.add(o);
        check(values.contains("v1") && values.contains("v2"), "elements() enumeration");

        // ---- getService / getServices ----
        TestProvider ps = new TestProvider("Ps", 1.0, "i");
        check(ps.getService("Cipher", "AES") == null, "getService unregistered null");
        ps.put("Cipher.AES", "com.example.AESImpl");
        ps.put("Cipher.DES", "com.example.DESImpl");
        Provider.Service svc = ps.getService("Cipher", "AES");
        check(svc != null, "getService registered non-null");
        check("Cipher".equals(svc.getType()), "Service.getType");
        check("AES".equals(svc.getAlgorithm()), "Service.getAlgorithm");
        check("com.example.AESImpl".equals(svc.getClassName()), "Service.getClassName");
        check("Ps".equals(svc.getProvider().getName()), "Service.getProvider");
        check(ps.getServices().size() == 2, "getServices count");
        check(ps.getService("Cipher", "aes") != null, "getService algorithm case-insensitive");

        // ---- toString ----
        check(p.toString().contains("TestProv"), "toString contains name");
        check(p.toString().contains("1.5"), "toString contains version");

        // ---- equals/hashCode ----
        TestProvider eq1 = new TestProvider("Eq", 2.0, "i");
        eq1.put("a", "1");
        TestProvider eq2 = new TestProvider("Eq", 2.0, "i");
        eq2.put("a", "1");
        check(eq1.equals(eq2), "equals identical maps");
        check(eq1.hashCode() == eq2.hashCode(), "hashCode equal");
        eq2.put("b", "2");
        check(!eq1.equals(eq2), "equals differing maps");

        // ---- serialization round-trip ----
        TestProvider ser = new TestProvider("Ser", 3.0, "serial info");
        ser.put("c1", "v1");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(ser);
        TestProvider back;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            back = (TestProvider) ois.readObject();
        }
        check("Ser".equals(back.getName()), "serialization preserves name");
        check(back.getVersion() == 3.0, "serialization preserves version");
        check("serial info".equals(back.getInfo()), "serialization preserves info");
        check("v1".equals(back.get("c1")), "serialization preserves entries");

        // ==== contract deepening: Provider.id system keys, Service lifecycle, supportsParameter ====

        // ---- Provider.id system entries (className is the concrete subclass) ----
        java.util.List<String> idKeys = new java.util.ArrayList<>();
        for (Object k : Collections.list(p.keys())) {
            String ks = (String) k;
            if (ks.startsWith("Provider.id")) idKeys.add(ks);
        }
        java.util.Collections.sort(idKeys);
        check(idKeys.size() == 4, "Provider.id key count (name/version/info/className)");
        check("TestProv".equals(p.get("Provider.id name")), "Provider.id name");
        check("1.5".equals(p.get("Provider.id version")), "Provider.id version");
        check("test info string".equals(p.get("Provider.id info")), "Provider.id info");
        check((ProviderTest.class.getName() + "$TestProvider").equals(p.get("Provider.id className")),
                "Provider.id className is concrete subclass");

        // ---- Service lifecycle through protected putService/removeService ----
        TestProvider sp = new TestProvider("SvcProv", 2.0, "svc");
        check(sp.getService("MessageDigest", "MyDigest") == null, "getService before put null");
        java.util.List<String> aliases = java.util.Arrays.asList("MyD");
        java.util.HashMap<String, String> attrs = new java.util.HashMap<>();
        attrs.put("SupportedKeyClasses", "java.security.Key");
        Provider.Service svc2 = new Provider.Service(sp, "MessageDigest", "MyDigest",
                MyDigest.class.getName(), aliases, attrs);
        sp.putSvc(svc2);
        check(sp.getService("MessageDigest", "MyDigest") == svc2, "getService after putService");
        check(sp.getService("MessageDigest", "MyD") == svc2, "alias lookup");
        check(sp.getService("MessageDigest", "mydigest") == svc2, "case-insensitive algorithm");
        check(sp.getServices().contains(svc2), "getServices contains added service");
        java.util.Set<Provider.Service> svcSet = sp.getServices();
        checkThrows(java.lang.UnsupportedOperationException.class, () -> svcSet.add(svc2),
                "getServices unmodifiable");
        sp.removeSvc(svc2);
        check(sp.getService("MessageDigest", "MyDigest") == null, "getService after removeService null");
        check(sp.getServices().isEmpty(), "getServices empty after removeService");
        // legacy property put also surfaces as a Service
        sp.put("MessageDigest.Legacy", MyDigest.class.getName());
        check(sp.getService("MessageDigest", "Legacy") != null, "legacy put surfaces as Service");

        // ---- Service constructor null matrix -> NPE ----
        final TestProvider np = sp;
        checkThrows(NullPointerException.class, () -> new Provider.Service(null, "T", "A", "C", null, null),
                "Service(null provider)");
        checkThrows(NullPointerException.class, () -> new Provider.Service(np, null, "A", "C", null, null),
                "Service(null type)");
        checkThrows(NullPointerException.class, () -> new Provider.Service(np, "T", null, "C", null, null),
                "Service(null algorithm)");
        checkThrows(NullPointerException.class, () -> new Provider.Service(np, "T", "A", null, null, null),
                "Service(null className)");

        // ---- newInstance: real engine instantiation, distinct instances, wrong type ----
        Provider.Service real = new Provider.Service(sp, "MessageDigest", "MyDigest",
                MyDigest.class.getName(), null, null);
        sp.putSvc(real);  // newInstance requires registration
        Object o1 = real.newInstance(null);
        check(o1 instanceof java.security.MessageDigest, "newInstance returns engine instance");
        check(real.newInstance(null) != o1, "newInstance distinct instances");
        Provider.Service wrong = new Provider.Service(sp, "MessageDigest", "NotEngine",
                "java.lang.String", null, null);
        sp.putSvc(wrong);
        // recorded: hand-registered services are NOT engine-type validated at newInstance;
        // a java.lang.String under "MessageDigest" instantiates without complaint
        Object raw = wrong.newInstance(null);
        check(raw instanceof String, "newInstance no engine-type check for manual services");
        // unregistered service cannot be instantiated
        Provider.Service unreg = new Provider.Service(sp, "MessageDigest", "Unreg",
                MyDigest.class.getName(), null, null);
        checkThrows(java.security.NoSuchAlgorithmException.class, () -> unreg.newInstance(null),
                "newInstance unregistered service");

        // ---- Service metadata ----
        check("java.security.Key".equals(svc2.getAttribute("SupportedKeyClasses")), "getAttribute");
        check(svc2.getAttribute("NoSuchAttribute") == null, "getAttribute missing null");
        check(svc2.toString().contains("MessageDigest") && svc2.toString().contains("MyDigest")
                && svc2.toString().contains("MyD"), "Service toString contents");

        // ---- supportsParameter semantics ----
        java.security.Key dsaKey = java.security.KeyPairGenerator.getInstance("DSA")
                .generateKeyPair().getPublic();
        java.security.Key rsaKey = java.security.KeyPairGenerator.getInstance("RSA")
                .generateKeyPair().getPublic();
        final Provider.Service mdSvc = svc2;
        checkThrows(java.security.InvalidParameterException.class, () -> mdSvc.supportsParameter(dsaKey),
                "supportsParameter on MessageDigest engine");
        java.util.HashMap<String, String> sigAttrs = new java.util.HashMap<>();
        sigAttrs.put("SupportedKeyClasses", "java.security.interfaces.DSAPublicKey");
        Provider.Service sigSvc = new Provider.Service(sp, "Signature", "FakeSig",
                "java.lang.String", null, sigAttrs);
        check(sigSvc.supportsParameter(dsaKey), "supportsParameter matching key class");
        check(!sigSvc.supportsParameter(rsaKey), "supportsParameter non-matching key class");
        checkThrows(java.security.InvalidParameterException.class, () -> sigSvc.supportsParameter("not-a-key"),
                "supportsParameter non-Key");
        Provider.Service bareSig = new Provider.Service(sp, "Signature", "BareSig",
                "java.lang.String", null, null);
        check(bareSig.supportsParameter(rsaKey), "supportsParameter without attribute always true");

        // ---- version-string API: JDK-9+ getVersionStr/isConfigured, absent on JDK-8 ----
        try {
            java.lang.reflect.Method gs = Provider.class.getMethod("getVersionStr");
            check("1.5".equals(gs.invoke(p)), "getVersionStr (JDK-9+)");
        } catch (NoSuchMethodException e) {
            check(true, "getVersionStr absent on JDK-8");
        }
        try {
            java.lang.reflect.Method ic = Provider.class.getMethod("isConfigured");
            check((Boolean) ic.invoke(p), "isConfigured default true (JDK-9+)");
        } catch (NoSuchMethodException e) {
            check(true, "isConfigured absent on JDK-8");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
