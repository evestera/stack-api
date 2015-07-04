package as.vestera.stack;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import spark.Spark;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class IntegrationTest {
    private final static String HOST = "http://localhost:" + Configuration.getPort();
    private final static Type listStringType = new TypeToken<List<String>>() {}.getType();
    private Gson gson = new Gson();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws Exception {
        Main.main(null);
        Thread.sleep(500);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Spark.stop();
    }

    @Before
    public void perTestSetUp() throws Exception {
        File tempFile = tempFolder.newFile();
        Configuration.setDbFile(tempFile.getAbsolutePath());
    }

    @Test
    public void testCreate() throws Exception {
        HttpResponse<String> response = listStacks();
        assertThat(response.getStatus(), is(200));
        List<String> stacks = gson.fromJson(response.getBody(), listStringType);
        assertThat(stacks.size(), is(0));

        response = createStack("cats");
        assertThat(response.getStatus(), is(201));

        response = listStacks();
        stacks = gson.fromJson(response.getBody(), listStringType);
        assertThat(stacks, hasItem("cats"));
    }

    @Test
    public void testPush() throws Exception {
        HttpResponse<String> response = createStack("cats");
        assertThat(response.getStatus(), is(201));

        response = push("cats", "tiger");
        assertThat(response.getStatus(), is(200));

        response = getStack("cats");
        List<String> stack = gson.fromJson(response.getBody(), listStringType);
        assertThat(stack, hasItem("tiger"));
    }

    @Test
    public void testPop() throws Exception {
        HttpResponse<String> response = createStack("cats");
        assertThat(response.getStatus(), is(201));

        push("cats", "tiger");
        push("cats", "fluffy");

        response = pop("cats");
        String value = gson.fromJson(response.getBody(), Element.class).value;
        assertThat(value, is("fluffy"));

        response = pop("cats");
        value = gson.fromJson(response.getBody(), Element.class).value;
        assertThat(value, is("tiger"));
    }

    @Test
    public void testPopFromEmptyStack() throws Exception {
        HttpResponse<String> response = createStack("cats");
        assertThat(response.getStatus(), is(201));

        response = pop("cats");
        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void testPushToNonExistingStack() throws Exception {
        HttpResponse<String> response = push("cats", "tiger");
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void testPopFromNonExistingStack() throws Exception {
        HttpResponse<String> response = pop("cats");
        assertThat(response.getStatus(), is(404));
    }

    private HttpResponse<String> listStacks() throws UnirestException {
        return Unirest
            .get(HOST + "/stacks")
            .asString();
    }

    private HttpResponse<String> createStack(String stackName) throws UnirestException {
        return Unirest
            .post(HOST + "/stacks")
            .body(gson.toJson(new StackId(stackName)))
            .asString();
    }

    private HttpResponse<String> getStack(String stackName) throws UnirestException {
        return Unirest
            .get(HOST + "/stacks/" + stackName)
            .asString();
    }

    private HttpResponse<String> push(String stackName, String value) throws UnirestException {
        return Unirest
            .post(HOST + "/stacks/" + stackName + "/push")
            .body(gson.toJson(new Element(value)))
            .asString();
    }

    private HttpResponse<String> pop(String stackName) throws UnirestException {
        return Unirest
            .post(HOST + "/stacks/" + stackName + "/pop")
            .asString();
    }
}
