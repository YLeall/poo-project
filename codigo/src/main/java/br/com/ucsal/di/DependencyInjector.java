package br.com.ucsal.di;

import br.com.ucsal.annotations.Inject;
import br.com.ucsal.annotations.RepositoryType;
import br.com.ucsal.annotations.Singleton;
import br.com.ucsal.controller.SingletonManager;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

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
        Reflections reflections = new Reflections("br.com.ucsal");
        Set<String> classNames = reflections.getAllTypes();
        Set<Class<?>> allClasses = new HashSet<>();

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                allClasses.add(clazz);
            } catch (ClassNotFoundException e) {
                System.err.println("Não foi possível carregar a classe: " + className);
            }
        }

        for (Class<?> clazz : allClasses) {
            boolean hasInjectFields = hasInjectFields(clazz);

            if (hasInjectFields) {
                try {
                    Object instance = createInstance(clazz);
                    if (instance != null) {
                        injectFieldDependencies(instance);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean hasInjectFields(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                return true;
            }
        }
        return false;
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        if (clazz.isAnnotationPresent(Singleton.class)) {
            return SingletonManager.getInstance(clazz);
        }

        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    public Object createAndInjectInstance(Class<?> clazz) throws Exception {
        Object instance = createInstance(clazz);
        injectFieldDependencies(instance);
        return instance;
    }

    private void injectFieldDependencies(Object instance) {
        if (instance == null) return;

        Class<?> clazz = instance.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    Object dependency;
                    Class<?> fieldType = field.getType();
                    Class<?> implClass = fieldType;

                    if (fieldType.isInterface()) {
                        if (field.isAnnotationPresent(RepositoryType.class)) {
                            RepositoryType implementedBy = field.getAnnotation(RepositoryType.class);
                            implClass = implementedBy.value();
                        } else {
                            throw new RuntimeException("Cannot inject dependency for interface " +
                                    fieldType.getName() + " without @RepositoryType annotation");
                        }
                    }

                    if (implClass.isAnnotationPresent(Singleton.class)) {
                        dependency = SingletonManager.getInstance(implClass);
                    } else {
                        dependency = implClass.getDeclaredConstructor().newInstance();
                    }

                    injectFieldDependencies(dependency);

                    field.set(instance, dependency);

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}