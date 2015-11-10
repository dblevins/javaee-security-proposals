package javax.security.idm.model;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Partition;

import static org.junit.Assert.*;

/**
 * Tests {@link javax.security.idm.model.Helper}
 */
@SuppressWarnings("unchecked")
public class HelperTest {

    private static final String WORKING_DIRECTORY = "./target/picketlink";
    private static final String REALM_NAME = "myRealm";

    {
        // Configure a file-based identity store
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        builder
                .named("file.config")
                .stores()
                .file()
                .supportType(
                        Caller.class,
                        Role.class,
                        Group.class,
                        Realm.class)
                .supportGlobalRelationship(
                        Grant.class,
                        GroupMembership.class)
                .supportCredentials(true)
                .workingDirectory(WORKING_DIRECTORY)
                .preserveState(false);
        IdentityConfiguration config = builder.build();

        // Build a partition manager
        PartitionManager partitionManager = new DefaultPartitionManager(config);
        Partition partition = new Realm(REALM_NAME);
        partitionManager.add(partition);

        identityManager = partitionManager.createIdentityManager(partition);
        relationshipManager = partitionManager.createRelationshipManager();
    }

    private final IdentityManager identityManager;
    private final RelationshipManager relationshipManager;

    @Test
    public void caller() {

        assertNull(Helper.getCaller(identityManager, "foo"));

        Caller joe = new Caller("joe");
        Caller sam = new Caller("sam");
        Caller mary = new Caller("mary");

        identityManager.add(joe);
        identityManager.add(sam);
        identityManager.add(mary);

        assertEquals("joe", joe, Helper.getCaller(identityManager, "joe"));
        assertEquals("sam", sam, Helper.getCaller(identityManager, "sam"));
        assertEquals("mary", mary, Helper.getCaller(identityManager, "mary"));
    }

    @Test
    public void role() {

        assertNull(Helper.getRole(identityManager, "bottle washer"));

        Role admin = new Role("admin");
        Role manager = new Role("manager");
        Role user = new Role("user");

        identityManager.add(admin);
        identityManager.add(manager);
        identityManager.add(user);

        assertEquals("admin", admin, Helper.getRole(identityManager, "admin"));
        assertEquals("manager", manager, Helper.getRole(identityManager, "manager"));
        assertEquals("user", user, Helper.getRole(identityManager, "user"));
    }

    @Test
    public void group() {

        assertNull(Helper.getGroup(identityManager, "bottle washer"));

        Group admin = new Group("admin");
        Group manager = new Group("manager");
        Group user = new Group("user", manager);

        identityManager.add(admin);
        identityManager.add(manager);
        identityManager.add(user);

        // getGroup(IdentityManager identityManager, String groupPath)
        assertEquals("admin", admin, Helper.getGroup(identityManager, "admin"));
        assertEquals("/admin", admin, Helper.getGroup(identityManager, "/admin"));
        assertNull("/manager/", Helper.getGroup(identityManager, "/manager/"));
        assertEquals("/manager", manager, Helper.getGroup(identityManager, "/manager"));
        assertEquals("manager", manager, Helper.getGroup(identityManager, "manager"));
        assertNull("user", Helper.getGroup(identityManager, "user"));
        assertNull("/user", Helper.getGroup(identityManager, "/user"));
        assertEquals("/manager/user", user, Helper.getGroup(identityManager, "/manager/user"));

        // getGroup(IdentityManager identityManager, String groupName, Group parent)
        assertEquals("admin, null", admin, Helper.getGroup(identityManager, "admin", null));
        assertNull("/admin, null", Helper.getGroup(identityManager, "/admin", null));
        assertEquals("manager, null", manager, Helper.getGroup(identityManager, "manager", null));
        assertEquals("user, manager", user, Helper.getGroup(identityManager, "user", manager));
        assertNull("manager, user", Helper.getGroup(identityManager, "manager", user));
    }

    @Test
    public void groupMembership() {

        Group g1 = new Group("g1");
        Group g2 = new Group("g2");
        Group g3 = new Group("g3", g2);

        identityManager.add(g1);
        identityManager.add(g2);
        identityManager.add(g3);

        Caller a1 = new Caller("a1");
        Caller a2 = new Caller("a2");
        Caller a3 = new Caller("a3");

        identityManager.add(a1);
        identityManager.add(a2);
        identityManager.add(a3);

        Helper.addToGroup(relationshipManager, a1, g1);
        Helper.addToGroup(relationshipManager, a2, g2);
        Helper.addToGroup(relationshipManager, a3, g3);

        assertTrue("1-1", Helper.isMember(relationshipManager, a1, g1));
        assertTrue("2-2", Helper.isMember(relationshipManager, a2, g2));
        assertFalse("1-2", Helper.isMember(relationshipManager, a1, g2));
        assertFalse("2-1", Helper.isMember(relationshipManager, a2, g1));
        assertFalse("3-1", Helper.isMember(relationshipManager, a3, g1));
        // Parent
        assertTrue("3-2", Helper.isMember(relationshipManager, a3, g2));
        assertTrue("3-3", Helper.isMember(relationshipManager, a3, g3));

        Helper.removeFromGroup(relationshipManager, a1, g1);

        assertFalse("1-1", Helper.isMember(relationshipManager, a1, g1));
        assertTrue("2-2", Helper.isMember(relationshipManager, a2, g2));
    }

    @Test
    public void grant() {

        Role r1 = new Role("r1");
        Role r2 = new Role("r2");

        identityManager.add(r1);
        identityManager.add(r2);

        Caller a1 = new Caller("a1");
        Caller a2 = new Caller("a2");
        Caller a3 = new Caller("a3");

        identityManager.add(a1);
        identityManager.add(a2);
        identityManager.add(a3);

        Group g1 = new Group("g1");
        Group g2 = new Group("g2", g1);
        Group g3 = new Group("g3", g2);

        identityManager.add(g1);
        identityManager.add(g2);
        identityManager.add(g3);
        relationshipManager.add(new GroupMembership(a3, g3));

        Helper.grantRole(relationshipManager, a1, r1);
        Helper.grantRole(relationshipManager, a2, r2);
        Helper.grantRole(relationshipManager, g2, r2);

        assertTrue("1-1", Helper.hasRole(relationshipManager, a1, r1));
        assertTrue("2-2", Helper.hasRole(relationshipManager, a2, r2));
        assertFalse("1-2", Helper.hasRole(relationshipManager, a1, r2));
        assertFalse("2-1", Helper.hasRole(relationshipManager, a2, r1));
        // Inherited
        assertTrue("3-2", Helper.hasRole(relationshipManager, g3, r2));
        assertTrue("3-2", Helper.hasRole(relationshipManager, a3, r2));
        assertTrue("2-2", Helper.hasRole(relationshipManager, g2, r2));
        // Privilege not passed up to parent, only down to child
        assertFalse("1-2", Helper.hasRole(relationshipManager, g1, r2));

        Helper.revokeRole(relationshipManager, a1, r1);

        assertFalse("1-1", Helper.hasRole(relationshipManager, a1, r1));
        assertTrue("2-2", Helper.hasRole(relationshipManager, a2, r2));
    }
}
