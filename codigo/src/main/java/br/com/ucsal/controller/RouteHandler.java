package br.com.ucsal.controller;

import br.com.ucsal.annotations.Rota;
import br.com.ucsal.annotations.RouteNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class RouteHandler {
    private static RouteHandler instance;
    private final Map<String, RouteInfo> routes = new HashMap<>();

    private RouteHandler() {
        loadRoutes();
        printLoadedRoutes(); // Add this to help debug
    }

    public static RouteHandler getInstance() {
        if (instance == null) {
            instance = new RouteHandler();
        }
        return instance;
    }

    private void printLoadedRoutes() {
        System.out.println("Loaded Routes:");
        for (String route : routes.keySet()) {
            System.out.println("Route: " + route);
        }
    }

    private void loadRoutes() {
        Reflections reflections = new Reflections("br.com.ucsal");

        // Find all classes with @Rota annotation
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rota.class);

        System.out.println("Scanning for routes in package br.com.ucsal");
        System.out.println("Total classes with @Rota annotation: " + classes.size());

        for (Class<?> clazz : classes) {
            System.out.println("Processing class: " + clazz.getName());

            boolean implementsCommand = Command.class.isAssignableFrom(clazz);
            System.out.println("Implements Command: " + implementsCommand);

            if (!implementsCommand) {
                System.out.println("Skipping class as it doesn't implement Command interface");
                continue;
            }

            Rota classRota = clazz.getAnnotation(Rota.class);
            if (classRota == null) {
                System.out.println("No @Rota annotation found on class");
                continue;
            }

            String classPath = classRota.value();
            System.out.println("Class-level route: " + classPath);

            try {
                Object classInstance = clazz.getDeclaredConstructor().newInstance();

                Method executeMethod = null;
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals("execute") &&
                            method.getParameterCount() == 2 &&
                            method.getParameterTypes()[0] == HttpServletRequest.class &&
                            method.getParameterTypes()[1] == HttpServletResponse.class) {
                        executeMethod = method;
                        break;
                    }
                }

                if (executeMethod == null) {
                    System.out.println("No suitable execute method found in " + clazz.getName());
                    continue;
                }

                if (!classPath.startsWith("/")) {
                    classPath = "/" + classPath;
                }

                System.out.println("Registering route: " + classPath + " for class: " + clazz.getName());

                routes.put(classPath, new RouteInfo(classInstance, executeMethod));

            } catch (Exception e) {
                System.err.println("Error processing routes for class " + clazz.getName());
                e.printStackTrace();
            }
        }

        // Print all registered routes
        System.out.println("Final registered routes:");
        routes.forEach((path, routeInfo) ->
                System.out.println(path + " -> " + routeInfo.instance.getClass().getSimpleName() + "." + routeInfo.method.getName())
        );
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        System.out.println("Servlet Path: " + servletPath);
        System.out.println("Path Info: " + pathInfo);

        String fullPath = servletPath + (pathInfo != null ? pathInfo : "");
        System.out.println("Full Path: " + fullPath);

        RouteInfo routeInfo = findMatchingRoute(fullPath);

        if (routeInfo == null) {
            System.out.println("No route found for path: " + fullPath);
            throw new RouteNotFoundException("No route found for path: " + fullPath);
        }

        try {
            routeInfo.method.invoke(routeInfo.instance, request, response);
        } catch (Exception e) {
            System.err.println("Error executing route: " + fullPath);
            e.printStackTrace();
            throw new ServletException("Error executing route: " + fullPath, e);
        }
    }

    private RouteInfo findMatchingRoute(String path) {
        path = path.replaceAll("^/|/$", "");

        RouteInfo exactMatch = routes.get("/" + path);
        if (exactMatch != null) {
            return exactMatch;
        }

        for (Map.Entry<String, RouteInfo> entry : routes.entrySet()) {
            String routePath = entry.getKey().replaceAll("^/|/$", "");
            if (path.equals(routePath) || path.endsWith(routePath)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static class RouteInfo {
        final Object instance;
        final Method method;

        RouteInfo(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
        }
    }
}