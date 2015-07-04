package as.vestera.stack;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.*;

public class Controller {
    Map<String, Deque<String>> stacks = new HashMap<>();
    Gson gson = new Gson();

    public Object getStackNames(Request request, Response response) {
        return stacks.keySet();
    }

    public Object createStack(Request request, Response response) {
        StackId id = gson.fromJson(request.body(), StackId.class);
        stacks.computeIfAbsent(id.name, (k) -> {
            response.status(201);
            return new LinkedList<>();
        });
        return new Message("OK");
    }

    public Object getStack(Request request, Response response) {
        Deque<String> stack = stacks.get(request.params(":name"));
        if (stack == null) throw new NoSuchElementException("No such stack");
        return stack;
    }

    public Object push(Request request, Response response) {
        Deque<String> stack = stacks.get(request.params(":name"));
        if (stack == null) throw new NoSuchElementException("No such stack");
        Message message = gson.fromJson(request.body(), Message.class);
        if (message == null || message.message == null) throw new IllegalArgumentException("No message posted");
        stack.push(message.message);
        return stack;
    }

    public Object pop(Request request, Response response) {
        Deque<String> stack = stacks.get(request.params(":name"));
        if (stack == null) throw new NoSuchElementException("No such stack");
        return stack.pop();
    }
}
