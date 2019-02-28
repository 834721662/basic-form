package server;

import server.Action.Action;
import server.Action.ExampleAction;

import java.util.HashMap;
import java.util.Map;

/**
 * action转发
 * @author zj
 * @since 2019/2/28
 */
public class Router {

    private static final String URL_EXAMPLE = "/url/example";

    private Map<String, Action> actionMap;

    public Router() {
        actionMap = new HashMap<>();
        actionMap.put(URL_EXAMPLE, new ExampleAction());
    }

    public Action route(String path) {
        return actionMap.get(path);
    }

}
