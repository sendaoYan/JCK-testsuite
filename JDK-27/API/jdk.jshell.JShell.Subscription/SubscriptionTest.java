/*
 * @test
 * @summary jdk.jshell.JShell.Subscription conformance: snippet event subscription.
 * @run main SubscriptionTest
 */

import jdk.jshell.*;
import jdk.jshell.SourceCodeAnalysis.*;
import java.util.*;

public class SubscriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        try (JShell js = JShell.create()) {
            JShell.Subscription sub = js.onSnippetEvent(e -> {});
            check(sub != null, "subscription");
            js.unsubscribe(sub);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
