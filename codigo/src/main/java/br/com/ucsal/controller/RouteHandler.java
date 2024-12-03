package br.com.ucsal.controller;

import br.com.ucsal.annotations.Rota;
import br.com.ucsal.annotations.RouteNotFoundException;
import br.com.ucsal.di.DependencyInjector;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class RouteHandler {
    private static RouteHandler instance;
    private final Map<String, Command> routes = new HashMap<>();

    private RouteHandler() {
        loadRoutes();
    }

    public static RouteHandler getInstance() {
        if (instance == null) {
            instance = new RouteHandler();
        }
        return instance;
    }

    private void loadRoutes() {
        Reflections reflections = new Reflections("br.com.ucsal");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rota.class);

        for (Class<?> clazz : classes) {
            if (!Command.class.isAssignableFrom(clazz)) {
                continue;
            }

            Rota classRota = clazz.getAnnotation(Rota.class);
            if (classRota == null) {
                continue;
            }

            String classPath = classRota.value();
            if (!classPath.startsWith("/")) {
                classPath = "/" + classPath;
            }

            try {
                Command commandInstance = (Command) clazz.getDeclaredConstructor().newInstance();
                routes.put(classPath, commandInstance);
                System.out.println("olaaaaa");
            } catch (Exception e) {
                System.err.println("Error processing route for class " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        String fullPath = servletPath + (pathInfo != null ? pathInfo : "");

        Command command = findMatchingRoute(fullPath);

        if (command == null) {
            throw new RouteNotFoundException("No route found for path: " + fullPath);
        }

        try {
            DependencyInjector.getInstance().injectDependencies();
            command.execute(request, response);
        } catch (Exception e) {
            throw new ServletException("Error executing route: " + fullPath, e);
        }
    }

    private Command findMatchingRoute(String path) {
        path = path.replaceAll("^/|/$", "");

        Command exactMatch = routes.get("/" + path);
        if (exactMatch != null) {
            return exactMatch;
        }

        for (Map.Entry<String, Command> entry : routes.entrySet()) {
            String routePath = entry.getKey().replaceAll("^/|/$", "");
            if (path.equals(routePath) || path.endsWith(routePath)) {
                return entry.getValue();
            }
        }

        return null;
    }
}