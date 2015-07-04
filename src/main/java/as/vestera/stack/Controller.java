package as.vestera.stack;

import com.google.gson.Gson;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

public class Controller {
    PersistentStore store = new PersistentStore();
    Gson gson = new Gson();

    public ExceptionHandler getExceptionHandler(int statusCode) {
        return (exception, request, response) -> {
            response.status(statusCode);
            response.body(gson.toJson(new ErrorMessage(exception)));
        };
    }

    public Object getStackNames(Request request, Response response) {
        return store.listStacks();
    }

    public Object createStack(Request request, Response response) {
        StackId id = gson.fromJson(request.body(), StackId.class);
        if (store.createStack(id.name)) {
            response.status(201);
            return new Message("Stack %s created", id.name);
        }
        return new Message("Stack %s already exists", id.name);
    }

    public Object getStack(Request request, Response response) {
        return store.getStack(request.params(":name"));
    }

    public Object push(Request request, Response response) {
        Element element = gson.fromJson(request.body(), Element.class);
        if (element == null || element.value == null) throw new IllegalArgumentException("No message posted");
        String stackName = request.params(":name");
        store.push(stackName, element.value);
        return store.getStack(stackName);
    }

    public Object pop(Request request, Response response) {
        return new Element(store.pop(request.params(":name")));
    }
}
