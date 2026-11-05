/*
 * @test
 * @summary jdk.jshell.JShell.Subscription conformance: snippet event subscription.
 * @run main/othervm SubscriptionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SubscriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: subscription lifecycle and delivery semantics ----
    static void testSubscriptionLifecycle() throws Exception {
        try (JShell js = JShell.builder().executionEngine("local").build()) {
            JShell.Subscription sub = js.onSnippetEvent(e -> {});
            check(sub != null, "subscription");
            js.unsubscribe(sub);
            // multiple independent subscriptions each get every event
            int[] a = {0}, b = {0};
            JShell.Subscription sa = js.onSnippetEvent(e -> a[0]++);
            JShell.Subscription sb = js.onSnippetEvent(e -> b[0]++);
            js.eval("int ss1 = 1;");
            check(a[0] == 1 && b[0] == 1, "both subscriptions notified");
            // unsubscribing one leaves the other active
            js.unsubscribe(sa);
            js.eval("int ss2 = 2;");
            check(a[0] == 1 && b[0] == 2, "unsubscribed stays silent");
            // re-subscribing after unsubscribe works
            JShell.Subscription sa2 = js.onSnippetEvent(e -> a[0]++);
            js.eval("int ss3 = 3;");
            check(a[0] == 2 && b[0] == 3, "re-subscription active");
            // overwrite eval delivers one event per affected snippet (2 for method overwrite)
            a[0] = 0; b[0] = 0;
            js.eval("int ssm() { return 1; }");
            js.eval("int ssm() { return 2; }");
            check(a[0] == 3 && b[0] == 3, "events counted: 1 + 2 for overwrite");
            // unsubscribe(sb) and sa2 then a callback-free eval
            js.unsubscribe(sb);
            js.unsubscribe(sa2);
            int before = a[0];
            js.eval("int ss4 = 4;");
            check(a[0] == before, "no delivery after full unsubscribe");
            // unsubscribe of an already-removed subscription is harmless
            js.unsubscribe(sa2);
            check(true, "double unsubscribe harmless");
        }
    }

    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            JShell.Subscription sub = js.onSnippetEvent(e -> {});
            check(sub != null, "subscription");
            js.unsubscribe(sub);
        }
        testSubscriptionLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
