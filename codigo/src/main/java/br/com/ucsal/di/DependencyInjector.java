package br.com.ucsal.di;

import br.com.ucsal.annotations.Inject;
import br.com.ucsal.annotations.Singleton;
import br.com.ucsal.persistencia.PersistenciaFactory;
import br.com.ucsal.persistencia.ProdutoRepository;
import org.reflections.Reflections;

import java.lang.reflect.Field;
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

        // Encontrar todas as classes anotadas com @Singleton
        Set<Class<?>> singletonClasses = reflections.getTypesAnnotatedWith(Singleton.class);
        for (Class<?> singletonClass : singletonClasses) {
            // Criar e armazenar a instância singleton
            Object singletonInstance = createSingletonInstance(singletonClass);
        }

        // Encontrar todas as classes que precisam de injeção de dependência
        Set<Class<?>> classesWithInjection = reflections.getTypesAnnotatedWith(Inject.class);
        for (Class<?> clazz : classesWithInjection) {
            // Criar a instância da classe
            Object instance = createInstance(clazz);

            // Injetar as dependências
            injectDependencies(instance, clazz);
        }
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating instance of class: " + clazz.getName(), e);
        }
    }

    private Object createSingletonInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            // Armazenar a instância singleton
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error creating singleton instance of class: " + clazz.getName(), e);
        }
    }

    private void injectDependencies(Object instance, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    // Injetar a instância do repositório apropriado
                    field.set(instance, PersistenciaFactory.getInstance().getHSQLProdutoRepository());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error injecting dependency into field: " + field.getName(), e);
                }
            }
        }
    }
}