package com.example.matcha.framework.di.core;

import com.example.matcha.framework.di.annotaion.Component;
import com.example.matcha.framework.di.annotaion.Repository;
import com.example.matcha.framework.di.annotaion.RestController;
import com.example.matcha.framework.di.annotaion.Service;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

@Slf4j
@RequiredArgsConstructor
public class ComponentScanner {
    private final DIContainer container;
    private final Reflections reflections;

    // 구현한 애노테이션 여기다가 하나씩 추가
    private static final Map<Class<? extends Annotation>, ValueExtractor<?>> VALUE_EXTRACTORS = Map.of(
            Component.class, (Component c) -> c.value(),
            Service.class, (Service s) -> s.value(),
            Repository.class, (Repository r) -> r.value(),
            RestController.class, (RestController rc) -> rc.value()
    );

    public ComponentScanner(DIContainer container, String basePackage) {
        this(container, new Reflections(basePackage));
    }

    public void scan() {
        Set<Class<?>> allComponentClasses = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : VALUE_EXTRACTORS.keySet()) {
            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotation);
            allComponentClasses.addAll(classes);
        }

        // 찾은 클래스들을 등록
        for (Class<?> clazz : allComponentClasses) {
            try {
                registerComponent(clazz);
            } catch (Exception ex) {
                log.warn("컴포넌트 등록 중 오류 ({}): {}", clazz.getName(), ex.getMessage(), ex);
            }
        }
    }

    private void registerComponent(Class<?> clazz) {
        if (clazz.isInterface() ||
                Modifier.isAbstract(clazz.getModifiers()) ||
                clazz.isMemberClass()) {
            log.debug("스캔 제외: {}", clazz.getName());
            return;
        }

        String beanName = extractBeanNameFromAnyAnnotation(clazz);

        if (beanName.isEmpty()) {
            container.register(clazz);
            beanName = Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1);
        } else {
            container.register(beanName, clazz);
        }
        log.info("등록된 빈: {} -> {}", beanName, clazz.getName());
    }

    @SuppressWarnings("unchecked")
    private String extractBeanNameFromAnyAnnotation(Class<?> clazz) {
        for (Class<? extends Annotation> annotationType : VALUE_EXTRACTORS.keySet()) {
            if (clazz.isAnnotationPresent(annotationType)) {
                ValueExtractor extractor = VALUE_EXTRACTORS.get(annotationType);
                return extractor.extractValue(clazz.getAnnotation(annotationType));
            }
        }
        return "";
    }
}

@FunctionalInterface
interface ValueExtractor<T extends Annotation> {
    String extractValue(T annotation);
}
