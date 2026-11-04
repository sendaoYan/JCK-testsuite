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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
