/*
 * @test
 * @summary java.net.http.HttpResponse.PushPromiseHandler.PushId conformance: marker interface.
 * @run main PushIdTest
 */

import java.net.http.HttpResponse;

public class PushIdTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(HttpResponse.PushPromiseHandler.PushId.class.isInterface(), "PushId is an interface");
        check(HttpResponse.PushPromiseHandler.PushId.class.getDeclaredMethods().length == 0,
                "PushId declares no methods (marker interface)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
