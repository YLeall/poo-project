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
            // Verificar se a classe tem campos com @Inject
            boolean hasInjectFields = hasInjectFields(clazz);

            if (hasInjectFields) {

                try {
                    // Criar a instância da classe
                    Object instance = createInstance(clazz);

                    // Injetar as dependências nos campos
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

    private Object createInstance(Class<?> clazz) {
        try {

            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);

            Object instance = constructor.newInstance();
            return instance;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object createAndInjectInstance(Class<?> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();

        injectFieldDependencies(instance);

        return instance;
    }

    private Map<Class<?>, Object> singletonInstances = new ConcurrentHashMap<>();

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

                    // Verificar se já existe uma instância em cache
                    if (singletonInstances.containsKey(implClass)) {
                        dependency = singletonInstances.get(implClass);
                    } else {
                        // Criar nova instância
                        dependency = implClass.getDeclaredConstructor().newInstance();

                        // Recursivamente injetar dependências
                        injectFieldDependencies(dependency);

                        // Armazenar instância no cache
                        singletonInstances.put(implClass, dependency);
                    }

                    // Definir a dependência
                    field.set(instance, dependency);

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}