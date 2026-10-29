/*
 * @test
 * @summary JLS 6.6.1: a public class and its public members are accessible from
 *          another package; private/package/protected members are reached only
 *          through public accessors.
 * @compile Api.java Main.java
 * @run main consumer.Main
 */
package consumer;

import apipkg.Api;

public class Main {
    public static void main(String[] args) {
        Api a = new Api();
        if (a.pub != 1) throw new AssertionError("public field");
        if (a.getPriv() != 4) throw new AssertionError("public accessor for private");
        if (a.getPack() != 3) throw new AssertionError("public accessor for package");
        if (a.getProt() != 2) throw new AssertionError("public accessor for protected");

        System.out.println("PASS");
    }
}
