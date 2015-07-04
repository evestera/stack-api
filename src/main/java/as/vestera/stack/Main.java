package as.vestera.stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        String portString = System.getenv("PORT");
        if (portString != null) {
            try {
                port(Integer.parseInt(portString));
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + portString);
            }
        }

        Controller controller = new Controller();
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

        get("/stacks", controller::getStackNames, gson::toJson);
        post("/stacks", controller::createStack, gson::toJson);

        get("/stacks/:name", controller::getStack, gson::toJson);
        post("/stacks/:name/push", controller::push, gson::toJson);
        post("/stacks/:name/pop", controller::pop, gson::toJson);

        exception(Exception.class, (exception, request, response) -> {
            response.status(500);
            response.body(gson.toJson(new ErrorMessage(exception)));
        });
    }
}
