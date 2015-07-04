package as.vestera.stack;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import spark.Spark;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class IntegrationTest {
    private final static int PORT = 4567;
    private final static String HOST = "http://localhost:" + PORT;
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
        HttpResponse<String> response = Unirest
            .get(HOST + "/stacks")
            .asString();
        assertThat(response.getStatus(), is(200));
        List<String> stacks = gson.fromJson(response.getBody(), listStringType);
        assertThat(stacks.size(), is(0));

        response = Unirest
            .post(HOST + "/stacks")
            .body(gson.toJson(new StackId("cats")))
            .asString();
        assertThat(response.getStatus(), is(201));

        response = Unirest
            .get(HOST + "/stacks")
            .asString();
        stacks = gson.fromJson(response.getBody(), listStringType);
        assertThat(stacks, hasItem("cats"));
    }

    @Test
    public void testPush() throws Exception {
        HttpResponse<String> response = Unirest
            .post(HOST + "/stacks")
            .body(gson.toJson(new StackId("cats")))
            .asString();
        assertThat(response.getStatus(), is(201));

        response = Unirest
            .post(HOST + "/stacks/cats/push")
            .body(gson.toJson(new Element("tiger")))
            .asString();
        assertThat(response.getStatus(), is(200));

        response = Unirest
            .get(HOST + "/stacks/cats")
            .asString();
        List<String> stack = gson.fromJson(response.getBody(), listStringType);
        assertThat(stack, hasItem("tiger"));
    }

    @Test
    public void testPopFromEmptyStack() throws Exception {
        HttpResponse<String> response = Unirest
            .post(HOST + "/stacks")
            .body(gson.toJson(new StackId("cats")))
            .asString();
        assertThat(response.getStatus(), is(201));

        response = Unirest
            .post(HOST + "/stacks/cats/pop")
            .asString();
        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void testPushToNonExistingStack() throws Exception {
        HttpResponse<String> response = Unirest
            .post(HOST + "/stacks/cats/push")
            .body(gson.toJson(new Element("tiger")))
            .asString();
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void testPopFromNonExistingStack() throws Exception {
        HttpResponse<String> response = Unirest
            .post(HOST + "/stacks/cats/pop")
            .asString();
        assertThat(response.getStatus(), is(404));
    }
}
