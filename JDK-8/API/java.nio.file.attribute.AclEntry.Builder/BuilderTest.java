/*
 * @test
 * @summary java.nio.file.attribute.AclEntry.Builder conformance: fluent build.
 * @run main BuilderTest
 */

import java.nio.file.FileSystems;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

public class BuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuilder() throws Exception {
        UserPrincipal owner = FileSystems.getDefault().getUserPrincipalLookupService()
                .lookupPrincipalByName(System.getProperty("user.name"));
        Set<AclEntryPermission> perms = new HashSet<AclEntryPermission>();
        perms.add(AclEntryPermission.WRITE_DATA);
        perms.add(AclEntryPermission.APPEND_DATA);
        AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.DENY)
                .setPrincipal(owner)
                .setPermissions(perms)
                .setFlags(AclEntryFlag.FILE_INHERIT)
                .build();
        check(entry.type() == AclEntryType.DENY, "type DENY");
        check(entry.permissions().contains(AclEntryPermission.WRITE_DATA), "WRITE_DATA");
        check(entry.permissions().contains(AclEntryPermission.APPEND_DATA), "APPEND_DATA");
        check(entry.flags().contains(AclEntryFlag.FILE_INHERIT), "FILE_INHERIT");
    }

    public static void main(String[] args) throws Exception {
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
