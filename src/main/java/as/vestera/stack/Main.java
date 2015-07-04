package as.vestera.stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.NoSuchElementException;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        port(Configuration.getPort());

        Controller controller = new Controller();
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

        get("/stacks", controller::getStackNames, gson::toJson);
        post("/stacks", controller::createStack, gson::toJson);

        get("/stacks/:name", controller::getStack, gson::toJson);
        post("/stacks/:name/push", controller::push, gson::toJson);
        post("/stacks/:name/pop", controller::pop, gson::toJson);

        exception(NoSuchElementException.class, controller.getExceptionHandler(400));
        exception(IllegalArgumentException.class, controller.getExceptionHandler(400));
        exception(NoSuchStackException.class, controller.getExceptionHandler(404));
        exception(Exception.class, controller.getExceptionHandler(500));

        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, DELETE");
        });
    }
}
