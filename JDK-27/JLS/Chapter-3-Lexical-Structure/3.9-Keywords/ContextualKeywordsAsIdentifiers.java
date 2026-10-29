/*
 * @test
 * @summary JLS 3.9: contextual keywords (exports, opens, requires, uses,
 *          yield, module, permits, sealed, var, provides, to, when, open,
 *          record, transitive, with) are reduced to identifiers outside the
 *          contexts in which they are keywords, so they may be used as names.
 * @run main ContextualKeywordsAsIdentifiers
 */

public class ContextualKeywordsAsIdentifiers {
    int exports = 1;
    int opens = 2;
    int requires = 3;
    int uses = 4;
    int yield = 5;
    int module = 6;
    int permits = 7;
    int sealed = 8;
    int var = 9;
    int provides = 10;
    int to = 11;
    int when = 12;
    int open = 13;
    int record = 14;
    int transitive = 15;
    int with = 16;

    // A method may also be named with a contextual keyword.
    int record() { return 14; }
    int with() { return 16; }

    public static void main(String[] args) {
        ContextualKeywordsAsIdentifiers t = new ContextualKeywordsAsIdentifiers();
        if (t.exports != 1 || t.opens != 2 || t.requires != 3 || t.uses != 4
                || t.yield != 5 || t.module != 6 || t.permits != 7 || t.sealed != 8
                || t.var != 9 || t.provides != 10 || t.to != 11 || t.when != 12
                || t.open != 13 || t.record != 14 || t.transitive != 15 || t.with != 16) {
            throw new AssertionError("a contextual keyword could not be used as an identifier");
        }
        if (t.record() != 14 || t.with() != 16) {
            throw new AssertionError("a contextual keyword could not be used as a method name");
        }
        System.out.println("PASS");
    }
}
