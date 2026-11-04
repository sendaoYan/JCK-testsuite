/*
 * @test
 * @summary java.net.http.HttpResponse.PushPromiseHandler.PushId.Http3PushId conformance: record accessors and equality.
 * @run main Http3PushIdTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.net.http.HttpResponse;

public class Http3PushIdTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = HttpResponse.PushPromiseHandler.PushId.Http3PushId.class;
        check(c.isRecord(), "Http3PushId is a record");
        check(HttpResponse.PushPromiseHandler.PushId.class.isAssignableFrom(c), "implements PushId");

        HttpResponse.PushPromiseHandler.PushId.Http3PushId id =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(42L, "conn-1");
        check(id.pushId() == 42L, "pushId() accessor");
        check("conn-1".equals(id.connectionLabel()), "connectionLabel() accessor");

        HttpResponse.PushPromiseHandler.PushId.Http3PushId same =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(42L, "conn-1");
        check(id.equals(same), "equals with same components");
        check(id.hashCode() == same.hashCode(), "hashCode with same components");
        check(id.toString().contains("42") && id.toString().contains("conn-1"),
                "toString exposes components");

        HttpResponse.PushPromiseHandler.PushId.Http3PushId diff =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(43L, "conn-1");
        check(!id.equals(diff), "not equals with different pushId");

        // ---- deepened: exact record shape (probed) ----
        RecordComponent[] comps = c.getRecordComponents();
        check(comps.length == 2, "two record components");
        check(comps[0].getName().equals("pushId"), "first component is pushId");
        check(comps[1].getName().equals("connectionLabel"), "second component is connectionLabel");
        check(comps[0].getType() == long.class, "pushId is a long");
        check(comps[1].getType() == String.class, "connectionLabel is a String");
        check("Http3PushId[pushId=42, connectionLabel=conn-1]".equals(id.toString()),
                "exact toString format (probed)");
        java.lang.reflect.Constructor<?> con = c.getDeclaredConstructor(long.class, String.class);
        check(con != null, "canonical constructor (long, String)");
        check(java.lang.reflect.Modifier.isPublic(con.getModifiers()),
                "canonical constructor is public");
        check(c.getNestHost() == HttpResponse.PushPromiseHandler.PushId.class.getNestHost(),
                "nested in the PushPromiseHandler nest");

        // ---- deepened: equality symmetry / null / foreign types ----
        check(same.equals(id), "equals is symmetric");
        check(!id.equals(null), "not equals to null");
        check(!id.equals("42"), "not equals to a foreign type");
        check(!id.equals(new HttpResponse.PushPromiseHandler.PushId.Http3PushId(42L, "conn-2")),
                "not equals with different connectionLabel");
        HttpResponse.PushPromiseHandler.PushId.Http3PushId zeroLabel =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(0L, "");
        check(zeroLabel.pushId() == 0L && zeroLabel.connectionLabel().isEmpty(),
                "zero id and empty label are legal (probed)");
        HttpResponse.PushPromiseHandler.PushId.Http3PushId nullLabel =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(1L, null);
        check(nullLabel.connectionLabel() == null, "null connectionLabel tolerated (probed)");
        check(nullLabel.toString().equals("Http3PushId[pushId=1, connectionLabel=null]"),
                "toString with null label (probed)");
        check(!nullLabel.equals(new HttpResponse.PushPromiseHandler.PushId.Http3PushId(1L, null)) == false,
                "null-label equality follows record semantics");
        HttpResponse.PushPromiseHandler.PushId.Http3PushId minId =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(Long.MIN_VALUE, "m");
        check(minId.pushId() == Long.MIN_VALUE, "Long.MIN_VALUE id accepted");
        HttpResponse.PushPromiseHandler.PushId.Http3PushId maxId =
                new HttpResponse.PushPromiseHandler.PushId.Http3PushId(Long.MAX_VALUE, "M");
        check(maxId.pushId() == Long.MAX_VALUE, "Long.MAX_VALUE id accepted");
        check(minId.hashCode() != maxId.hashCode() || minId.pushId() == maxId.pushId(),
                "hashCodes differ for distinct ids");

        // ---- deepened: usable as PushId in the default interface methods ----
        HttpResponse.PushPromiseHandler.PushId asSuper = id;
        check(asSuper.toString().startsWith("Http3PushId["), "PushId-typed view delegates to record toString");
        java.util.Set<HttpResponse.PushPromiseHandler.PushId> set = java.util.Set.of(id, diff);
        check(set.contains(same), "HashSet lookup by value through the PushId supertype");
        check(set.size() == 2, "two distinct ids in a set");
        java.util.Map<HttpResponse.PushPromiseHandler.PushId, String> map =
                java.util.Map.of(id, "a", diff, "b");
        check("a".equals(map.get(same)), "HashMap lookup by value");

        // Serialization: the record does NOT implement Serializable (probed).
        check(!java.io.Serializable.class.isAssignableFrom(c),
                "Http3PushId is not Serializable (JDK-27 probed)");
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
                oos.writeObject(id);
            }
            check(false, "serialization must fail");
        } catch (java.io.NotSerializableException e) {
            check(e.getMessage().endsWith("Http3PushId"),
                    "writeObject throws NotSerializableException naming the record (probed)");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
