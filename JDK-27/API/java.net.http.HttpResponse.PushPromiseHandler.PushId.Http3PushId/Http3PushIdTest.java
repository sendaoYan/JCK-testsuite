/*
 * @test
 * @summary java.net.http.HttpResponse.PushPromiseHandler.PushId.Http3PushId conformance: record accessors and equality.
 * @run main Http3PushIdTest
 */

import java.net.http.HttpResponse;

public class Http3PushIdTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
