/*
 * @test
 * @summary JLS 3.9 (Java SE 8): the 50 keywords and the reserved words const
 *          and goto may not be used as identifiers.  (The underscore _ is a
 *          legal identifier in Java SE 8 and is tested separately.)
 * @compile/fail ReservedKeywordsAreNotIdentifiers.java
 */

public class ReservedKeywordsAreNotIdentifiers {
    int abstract;
    int assert;
    int boolean;
    int break;
    int byte;
    int case;
    int catch;
    int char;
    int class;
    int const;
    int continue;
    int default;
    int do;
    int double;
    int else;
    int enum;
    int extends;
    int final;
    int finally;
    int float;
    int for;
    int goto;
    int if;
    int implements;
    int import;
    int instanceof;
    int int;
    int interface;
    int long;
    int native;
    int new;
    int package;
    int private;
    int protected;
    int public;
    int return;
    int short;
    int static;
    int strictfp;
    int super;
    int switch;
    int synchronized;
    int this;
    int throw;
    int throws;
    int transient;
    int try;
    int void;
    int volatile;
    int while;
}
