/*
 * @test
 * @summary com.sun.net.httpserver.Authenticator conformance: white-box structure + black-box behavior.
 * @run main AuthenticatorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AuthenticatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.net.httpserver.Authenticator");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(methodNames(c).contains("authenticate"), "authenticate");

        // nested result hierarchy
        Class<?> result = load("com.sun.net.httpserver.Authenticator$Result");
        check(Modifier.isAbstract(result.getModifiers()) &&
              Modifier.isStatic(result.getModifiers()), "Result abstract static nested");
        check(load("com.sun.net.httpserver.Authenticator$Success").getSuperclass() == result,
              "Success extends Result");
        check(load("com.sun.net.httpserver.Authenticator$Retry").getSuperclass() == result,
              "Retry extends Result");
        check(load("com.sun.net.httpserver.Authenticator$Failure").getSuperclass() == result,
              "Failure extends Result");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        com.sun.net.httpserver.HttpPrincipal principal =
                new com.sun.net.httpserver.HttpPrincipal("user", "realm");

        // Success
        com.sun.net.httpserver.Authenticator successAuth = new com.sun.net.httpserver.Authenticator() {
            public com.sun.net.httpserver.Authenticator.Result authenticate(com.sun.net.httpserver.HttpExchange t) {
                return new com.sun.net.httpserver.Authenticator.Success(principal);
            }
        };
        com.sun.net.httpserver.Authenticator.Result r = successAuth.authenticate(null);
        check(r instanceof com.sun.net.httpserver.Authenticator.Success, "Success type");
        check(((com.sun.net.httpserver.Authenticator.Success) r).getPrincipal() == principal,
              "Success.getPrincipal returns passed principal");

        // Retry
        com.sun.net.httpserver.Authenticator retryAuth = new com.sun.net.httpserver.Authenticator() {
            public com.sun.net.httpserver.Authenticator.Result authenticate(com.sun.net.httpserver.HttpExchange t) {
                return new com.sun.net.httpserver.Authenticator.Retry(401);
            }
        };
        com.sun.net.httpserver.Authenticator.Result retry = retryAuth.authenticate(null);
        check(retry instanceof com.sun.net.httpserver.Authenticator.Retry, "Retry type");
        check(((com.sun.net.httpserver.Authenticator.Retry) retry).getResponseCode() == 401,
              "Retry.getResponseCode");

        // Failure
        com.sun.net.httpserver.Authenticator failureAuth = new com.sun.net.httpserver.Authenticator() {
            public com.sun.net.httpserver.Authenticator.Result authenticate(com.sun.net.httpserver.HttpExchange t) {
                return new com.sun.net.httpserver.Authenticator.Failure(403);
            }
        };
        com.sun.net.httpserver.Authenticator.Result failure = failureAuth.authenticate(null);
        check(failure instanceof com.sun.net.httpserver.Authenticator.Failure, "Failure type");
        check(((com.sun.net.httpserver.Authenticator.Failure) failure).getResponseCode() == 403,
              "Failure.getResponseCode");

        // all results are instances of the common base Result
        check(r instanceof com.sun.net.httpserver.Authenticator.Result, "Success is Result");
        check(retry instanceof com.sun.net.httpserver.Authenticator.Result, "Retry is Result");
        check(failure instanceof com.sun.net.httpserver.Authenticator.Result, "Failure is Result");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
