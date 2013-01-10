package nl.tweeenveertig.openstack.client.mock;

import nl.tweeenveertig.openstack.model.Container;
import nl.tweeenveertig.openstack.model.PaginationMap;
import nl.tweeenveertig.openstack.model.StoredObject;
import nl.tweeenveertig.openstack.exception.CommandException;
import nl.tweeenveertig.openstack.exception.CommandExceptionError;
import nl.tweeenveertig.openstack.exception.NotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static junit.framework.Assert.*;

public class ContainerMockTest {

    private ContainerMock container;

    private StoredObject object;

    @Before
    public void setup() {
        this.container = new ContainerMock(new AccountMock(), "someContainer");
        this.container.create();
        this.object = this.container.getObject("someObject");
    }

    // TODO implement exists() method before reactivating these chaps
    @Test
    public void getOrCreateDoesNotExist() {
        assertFalse(container.getObject("somevalue").exists());
    }

    @Test
    public void getDoesNotExist() {
        try {
            container.getObject("somevalue").delete();
            fail("Should have thrown an exception");
        } catch (CommandException err) {
            assertEquals(CommandExceptionError.ENTITY_DOES_NOT_EXIST, err.getError());
        }
    }

    @Test
    public void publicPrivate() {
        assertFalse(container.isPublic());
        container.makePublic();
        assertTrue(container.isPublic());
        container.makePrivate();
        assertFalse(container.isPublic());
    }

    @Test
    public void numberOfObjects() throws IOException {
        addObjects(3);
        assertEquals(3, container.getCount());
    }

    @Test
    public void listObjects() throws IOException {
        addObjects(3);
        assertEquals(3, container.listObjects().size());
    }

    @Test
    public void deleteObject() throws IOException {
        object.uploadObject(new byte[]{});
        assertEquals(1, container.getCount());
        object.delete();
        assertEquals(0, container.getCount());
    }

    @Test
    public void getInfo() throws IOException {
        addObject("object1", new byte[] { 0x01, 0x02, 0x03 } );
        addObject("object2", new byte[] { 0x01, 0x02 } );
        addObject("object3", new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 } );
        assertEquals(10, container.getBytesUsed());
        assertEquals(3, container.getCount());
    }

    @Test
    public void existence() {
        Container container = new AccountMock().getContainer("someContainer");
        assertFalse(container.exists());
        container.create();
        assertTrue(container.exists());
        Container newContainer = new ContainerMock(container.getAccount(), "test") {
            @Override
            protected void checkForInfo() {
                throw new NotFoundException(404, CommandExceptionError.ENTITY_DOES_NOT_EXIST);
            }
        };
        assertFalse(newContainer.exists());
    }

    @Test
    public void listObjectsPaged() {
        container.getObject("A").uploadObject(new byte[]{});
        container.getObject("B").uploadObject(new byte[]{});
        StoredObject object3 = container.getObject("C");
        object3.uploadObject(new byte[]{});
        StoredObject object4 = container.getObject("D");
        object4.uploadObject(new byte[]{});
        Collection<StoredObject> objects = container.list("B", 2);
        assertEquals(2, objects.size());
        objects.contains(object3);
        objects.contains(object4);
    }

    @Test
    public void listContainersUsePaginationMap() {
        container.getObject("A").uploadObject(new byte[]{});
        container.getObject("B").uploadObject(new byte[]{});
        StoredObject object3 = container.getObject("C");
        object3.uploadObject(new byte[]{});
        StoredObject object4 = container.getObject("D");
        object4.uploadObject(new byte[]{});
        PaginationMap paginationMap = container.getPaginationMap(2);
        Collection<StoredObject> objects = container.list(paginationMap, 1);
        assertEquals(2, objects.size());
        objects.contains(object3);
        objects.contains(object4);
    }

    @Test
    public void getObject() throws IOException {
        StoredObject object1 = container.getObject("some-object");
        assertFalse(object1.exists());
        object1.uploadObject(new byte[]{0x01});
        StoredObject object2 = container.getObject("some-object");
        assertEquals(object1, object2);
        assertTrue(object1.exists());
    }

    @Test
    public void addMetadata() {
        Map<String, Object> metadata = new TreeMap<String, Object>();
        metadata.put("name", "value");
        container.setMetadata(metadata);
        assertEquals(1, container.getMetadata().size());
    }

    @Test
    public void getObjectSegment() {
        StoredObject object = container.getObjectSegment("alpha", 14);
        assertEquals("alpha/00000014", object.getName());
    }

    protected void addObject(String name, byte[] bytes) throws IOException {
        StoredObject object = container.getObject(name);
        object.uploadObject(bytes);
    }

    protected void addObjects(int times) throws IOException {
        for (int i = 0; i < times; i++) {
            container.getObject("someobject"+i).uploadObject(new byte[] {});
        }
    }
}
