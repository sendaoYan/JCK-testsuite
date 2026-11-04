/*
 * @test
 * @summary JLS 6.6.1 (Java SE 8): a public class and its public members are
 *          accessible from another package; protected, package-private, and
 *          private members are reachable only through public accessors.
 * @compile Api.java Main.java
 * @run main consumer8.Main
 */

package consumer8;

import apipkg8.Api;

public class Main {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        Api a = new Api();
        check(a.pub == 1, "public field accessible across packages");
        check(a.getProt() == 2, "protected member via public accessor");
        check(a.getPack() == 3, "package-private member via public accessor");
        check(a.getPriv() == 4, "private member via public accessor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
