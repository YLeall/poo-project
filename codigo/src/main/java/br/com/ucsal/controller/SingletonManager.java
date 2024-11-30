package br.com.ucsal.controller;

import br.com.ucsal.annotations.Singleton;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonManager {
    private static final Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();

    public static <T> T getInstance(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Singleton.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Singleton");
        }

        T instance = (T) singletonInstances.get(clazz);
        if (instance == null) {
            synchronized (SingletonManager.class) {
                instance = (T) singletonInstances.get(clazz);
                if (instance == null) {
                    try {
                        Constructor<T> constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        instance = constructor.newInstance();
                        singletonInstances.put(clazz, instance);
                    } catch (NoSuchMethodException | InstantiationException |
                             IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("Error creating singleton instance", e);
                    }
                }
            }
        }
        return instance;
    }
}