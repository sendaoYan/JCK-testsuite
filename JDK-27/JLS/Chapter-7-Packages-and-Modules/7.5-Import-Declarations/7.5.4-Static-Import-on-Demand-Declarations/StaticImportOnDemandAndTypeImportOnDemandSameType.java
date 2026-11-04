/*
 * @test
 * @summary JLS 7.5.4: a compilation unit containing both a
 *          static-import-on-demand and a type-import-on-demand naming the same
 *          type imports the static member types of that type only once.
 * @run main StaticImportOnDemandAndTypeImportOnDemandSameType
 */

import java.util.*;
import static java.util.Map.*;

public class StaticImportOnDemandAndTypeImportOnDemandSameType {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Entry<String, String> e = new AbstractMap.SimpleEntry<String, String>("k", "v");
        check("k".equals(e.getKey()), "Entry via static member type");
        check("v".equals(e.getValue()), "value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
