package br.com.ucsal.controller;

import br.com.ucsal.annotations.Singleton;
import org.reflections.Reflections;

import java.util.Set;

public class SingletonManagerInitializer {
    public static void initializeSingletons() {
        Reflections reflections = new Reflections("br.com.ucsal");
        Set<Class<?>> singletonClasses = reflections.getTypesAnnotatedWith(Singleton.class);

        for (Class<?> singletonClass : singletonClasses) {
            try {
                SingletonManager.getInstance(singletonClass);
                System.out.println("Initialized singleton: " + singletonClass.getSimpleName());
            } catch (Exception e) {
                System.err.println("Error initializing singleton " + singletonClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}