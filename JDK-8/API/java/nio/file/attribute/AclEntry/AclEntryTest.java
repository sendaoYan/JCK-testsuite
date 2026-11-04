/*
 * @test
 * @summary java.nio.file.attribute.AclEntry conformance: type/principal/permissions.
 * @run main AclEntryTest
 */

import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.FileSystems;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class AclEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAclEntry() throws Exception {
        UserPrincipal owner = FileSystems.getDefault().getUserPrincipalLookupService()
                .lookupPrincipalByName(System.getProperty("user.name"));
        Set<AclEntryPermission> perms = new HashSet<AclEntryPermission>();
        perms.add(AclEntryPermission.READ_DATA);
        AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(perms)
                .build();
        check(entry.type() == AclEntryType.ALLOW, "type");
        check(entry.principal().equals(owner), "principal");
        check(entry.permissions().contains(AclEntryPermission.READ_DATA), "permissions");
        check(entry.flags().isEmpty(), "flags empty");
        check(entry.toString() != null, "toString");
    }

    public static void main(String[] args) throws Exception {
        testAclEntry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
