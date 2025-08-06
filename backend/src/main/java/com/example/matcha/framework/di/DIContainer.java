package com.example.matcha.framework.di;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DIContainer {
    private final Set<Class<?>> registeredClasses = new HashSet<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Set<Class<?>> creating = new HashSet<>();

    public void register(Class<?>... classes) {
        Objects.requireNonNull(classes, "클래스 배열이 null 입니다");
        for (int i = 0; i < classes.length; ++i) {
            Objects.requireNonNull(classes[i],
                    String.format("인덱스 %d의 클래스가 null 입니다", i));
        }
        Collections.addAll(registeredClasses, classes);
    }

    public void initialize() {
        for (Class<?> clazz : registeredClasses) {
            getBean(clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        Object instance = instances.get(clazz);
        if (instance != null) {
            return (T) instance;
        }

        if (!registeredClasses.contains(clazz)) {
            throw new RuntimeException("등록되지 않은 클래스입니다: " + clazz.getName());
        }
        return (T) createInstance(clazz);
    }

    private Object createInstance(Class<?> clazz) {
        if (creating.contains(clazz)) {
            throw new RuntimeException("순환 의존성 감지: " + clazz.getName());
        }

        creating.add(clazz);
        try {
            // 1. 생성자 찾기
            Constructor<?> constructor = findConstructor(clazz);

            // 2. 의존성 해결
            Object[] dependencies = resolveDependencies(constructor);

            // 3. 인스턴스 생성
            Object instance = constructor.newInstance(dependencies);
            instances.put(clazz, instance);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("빈 생성 실패: " + clazz.getName(), e);
        } finally {
            creating.remove(clazz);
        }
    }

    /**
     * 생성자의 의존성들을 해결
     */
    private Object[] resolveDependencies(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] dependencies = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; ++i) {
            dependencies[i] = getBean(parameterTypes[i]);
        }

        return dependencies;
    }

    /**
     * 적절한 생성자 찾기
     */
    private Constructor<?> findConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if (constructors.length == 1) {
            return constructors[0];
        }

        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("적절한 생성자를 찾을 수 없습니다. : " + clazz.getName());
        }
    }
}
