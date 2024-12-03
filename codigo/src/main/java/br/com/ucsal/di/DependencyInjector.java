package br.com.ucsal.di;

import br.com.ucsal.annotations.Inject;
import br.com.ucsal.annotations.RepositoryType;
import br.com.ucsal.annotations.Singleton;
import br.com.ucsal.controller.SingletonManager;
import br.com.ucsal.persistencia.PersistenciaFactory;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyInjector {
    private static DependencyInjector instance;

    private DependencyInjector() {
    }

    public static DependencyInjector getInstance() {
        if (instance == null) {
            instance = new DependencyInjector();
        }
        return instance;
    }

    public void injectDependencies() {
        System.out.println("===== DependencyInjector: Starting Dependency Injection =====");

        Reflections reflections = new Reflections("br.com.ucsal");

        // Get all class names in the package
        Set<String> classNames = reflections.getAllTypes();
        Set<Class<?>> allClasses = new HashSet<>();

        // Convert class names to Class objects
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                allClasses.add(clazz);
            } catch (ClassNotFoundException e) {
                System.err.println("Could not load class: " + className);
            }
        }

        System.out.println("Total Classes Found: " + allClasses.size());

        for (Class<?> clazz : allClasses) {
            // Verificar se a classe tem campos com @Inject
            boolean hasInjectFields = hasInjectFields(clazz);

            if (hasInjectFields) {
                System.out.println("==== Processing Class with Injection Fields: " + clazz.getName() + " ====");

                try {
                    // Criar a instância da classe
                    Object instance = createInstance(clazz);

                    // Injetar as dependências nos campos
                    if (instance != null) {
                        injectFieldDependencies(instance);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing class: " + clazz.getName());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("===== DependencyInjector: Dependency Injection Completed =====");
    }

    private boolean hasInjectFields(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                return true;
            }
        }
        return false;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            System.out.println("Creating instance for: " + clazz.getName());

            // Try to find a no-args constructor
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);

            Object instance = constructor.newInstance();
            System.out.println("Instance created successfully: " + instance);
            return instance;
        } catch (NoSuchMethodException e) {
            // No no-args constructor found
            System.err.println("No no-args constructor found for class: " + clazz.getName());
            return null;
        } catch (Exception e) {
            System.err.println("Error creating instance of class: " + clazz.getName());
            e.printStackTrace();
            return null;
        }
    }

    // Adicione um campo para armazenar instâncias únicas
    private Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();

    private void injectFieldDependencies(Object instance) {
        if (instance == null) return;

        Class<?> clazz = instance.getClass();
        System.out.println("Injecting dependencies for: " + clazz.getName());

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                System.out.println("Found @Inject field: " + field.getName());

                field.setAccessible(true);
                try {
                    Object dependency;
                    Class<?> fieldType = field.getType();
                    Class<?> implClass = fieldType;

                    // Check if the field type is an interface
                    if (fieldType.isInterface()) {
                        // Check for RepositoryType (equivalent to ImplementedBy)
                        if (field.isAnnotationPresent(RepositoryType.class)) {
                            RepositoryType implementedBy = field.getAnnotation(RepositoryType.class);
                            implClass = implementedBy.value();
                        } else {
                            throw new RuntimeException("Cannot inject dependency for interface " +
                                    fieldType.getName() + " without @RepositoryType annotation");
                        }
                    }

                    // Verificar se já existe uma instância em cache
                    if (singletonInstances.containsKey(implClass)) {
                        dependency = singletonInstances.get(implClass);
                        System.out.println("Using cached instance for: " + implClass.getName());
                    } else {
                        // Criar nova instância
                        dependency = implClass.getDeclaredConstructor().newInstance();

                        // Recursivamente injetar dependências
                        injectFieldDependencies(dependency);

                        // Armazenar instância no cache
                        singletonInstances.put(implClass, dependency);
                        System.out.println("Created and cached new instance for: " + implClass.getName());
                    }

                    // Definir a dependência
                    field.set(instance, dependency);
                    System.out.println("Injected dependency: " + dependency);

                } catch (Exception e) {
                    System.err.println("Error injecting dependency into field: " + field.getName());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}