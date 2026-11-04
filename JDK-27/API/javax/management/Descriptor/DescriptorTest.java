/*
 * @test
 * @summary javax.management.Descriptor conformance: white-box structure + light behavior.
 * @run main DescriptorTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class DescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.Descriptor");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(java.lang.Cloneable.class.isAssignableFrom(c), "extends Cloneable");
        check(hasMethod(c, "getFieldValue"), "has getFieldValue");
        check(hasMethod(c, "setField"), "has setField");
        check(hasMethod(c, "getFields"), "has getFields");
        check(hasMethod(c, "getFieldNames"), "has getFieldNames");
        check(hasMethod(c, "getFieldValues"), "has getFieldValues");
        check(hasMethod(c, "removeField"), "has removeField");
        check(hasMethod(c, "setFields"), "has setFields");
        check(hasMethod(c, "clone"), "has clone");
        check(hasMethod(c, "isValid"), "has isValid");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // ImmutableDescriptor is a real Descriptor; read-only contract observable through the interface.
        Descriptor d = new ImmutableDescriptor("name=n", "description=d");
        check("n".equals(d.getFieldValue("name")), "getFieldValue");
        check(d.getFieldNames().length == 2, "getFieldNames");
        check(d.getFieldValues("name", "description").length == 2, "getFieldValues");
        check(d.getFields().length == 2, "getFields");
        check(d.isValid(), "isValid");
        Descriptor clone = (Descriptor) d.clone();
        check(clone.equals(d), "clone equals");

        // A mutable anonymous Descriptor supports setField/removeField through the interface.
        final Map<String, Object> store = new HashMap<String, Object>();
        Descriptor mutable = new Descriptor() {
            public Object getFieldValue(String f) { return store.get(f); }
            public void setField(String f, Object v) { store.put(f, v); }
            public String[] getFields() {
                List<String> out = new ArrayList<String>();
                for (Map.Entry<String, Object> e : store.entrySet()) out.add(e.getKey() + "=" + e.getValue());
                return out.toArray(new String[0]);
            }
            public String[] getFieldNames() {
                Set<String> ks = store.keySet();
                String[] a = ks.toArray(new String[0]);
                Arrays.sort(a);
                return a;
            }
            public Object[] getFieldValues(String... names) {
                if (names == null) return store.values().toArray();
                Object[] r = new Object[names.length];
                for (int i = 0; i < names.length; i++) r[i] = store.get(names[i]);
                return r;
            }
            public void removeField(String f) { store.remove(f); }
            public void setFields(String[] names, Object[] values) {
                for (int i = 0; i < names.length; i++) store.put(names[i], values[i]);
            }
            public Object clone() {
                Descriptor dd = new ImmutableDescriptor(store);
                return dd;
            }
            public boolean isValid() { return true; }
        };
        mutable.setField("k", "v");
        check("v".equals(mutable.getFieldValue("k")), "mutable setField/getFieldValue");
        mutable.removeField("k");
        check(mutable.getFieldValue("k") == null, "mutable removeField");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
