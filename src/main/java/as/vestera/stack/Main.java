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

        exception(NoSuchElementException.class, (exception, request, response) -> {
            response.status(400);
            response.body(gson.toJson(new ErrorMessage(exception)));
        });

        exception(NoSuchStackException.class, (exception, request, response) -> {
            response.status(404);
            response.body(gson.toJson(new ErrorMessage(exception)));
        });

        exception(Exception.class, (exception, request, response) -> {
            response.status(500);
            response.body(gson.toJson(new ErrorMessage(exception)));
        });
    }
}
