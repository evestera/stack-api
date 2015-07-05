package as.vestera.stack;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class PersistentStoreTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    public PersistentStore store = new PersistentStore();

    @Before
    public void setUp() throws Exception {
        File tempFile = tempFolder.newFile();
        Configuration.setDbFile(tempFile.getAbsolutePath());
    }

    @Test
    public void testCreateStack() throws Exception {
        assertThat(store.createStack("foo"), is(true));
        assertThat(store.createStack("foo"), is(false));
        assertThat(store.createStack("bar"), is(true));

        assertThat(store.listStacks(), hasItems("foo", "bar"));
    }

    @Test
    public void testPushPop() throws Exception {
        store.createStack("cats");
        store.push("cats", "tiger");
        store.push("cats", "fluffy");

        assertThat(store.getStack("cats"), is(Arrays.asList("tiger", "fluffy")));

        assertThat(store.pop("cats"), is("fluffy"));
        assertThat(store.pop("cats"), is("tiger"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testPoppingWithoutPushing() throws Exception {
        store.createStack("dogs");
        store.pop("dogs");
    }

    @Test(expected = NoSuchStackException.class)
    public void testPushToNonExistingStack() throws Exception {
        store.push("dogs", "fido");
    }

    @Test(expected = NoSuchStackException.class)
    public void testPopFromNonExistingStack() throws Exception {
        store.pop("dogs");
    }

    @Test
    public void testSqlInjection() throws Exception {
        store.createStack("cats");
        store.push("cats", "tiger");
        store.push("cats", "robert'); drop table cats; --");
        assertThat(store.listStacks(), hasItem("cats"));
    }

    @Test
    public void testUnicodeValues() throws Exception {
        store.createStack("cats");
        store.push("cats", "辉煌虎");
        store.push("cats", "Gråtass");
        store.push("cats", "😸");
        assertThat(store.pop("cats"), is("😸"));
        assertThat(store.pop("cats"), is("Gråtass"));
        assertThat(store.pop("cats"), is("辉煌虎"));
    }

    @Test
    public void testHidingInternalTables() throws Exception {
        store.createStack("cats");
        store.push("cats", "bob");
        assertThat(store.listStacks(), not(hasItem("sqlite_sequence")));
    }
}
