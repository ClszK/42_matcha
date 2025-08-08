package com.example.matcha.framework.di.core;

import com.example.matcha.framework.di.annotation.Component;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ComponentScanner {
    private final DIContainer container;
    private final String basePackage;

    public void scan() {
        try (
                ScanResult scanResult = new ClassGraph()
                        .enableClassInfo()
                        .enableAnnotationInfo()
                        .acceptPackages(basePackage)
                        .scan();
        ) {
            Set<Class<?>> candidates = new LinkedHashSet<>();
            scanResult.getClassesWithAnnotation(Component.class.getName())
                    .forEach(ci -> candidates.add(ci.loadClass()));

            // 찾은 클래스들을 등록
            for (Class<?> clazz : candidates) {
                try {
                    registerComponent(clazz);
                } catch (Exception ex) {
                    log.warn("컴포넌트 등록 중 오류 ({}): {}", clazz.getName(), ex.getMessage(), ex);
                }
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

    private String extractBeanNameFromAnyAnnotation(Class<?> clazz) {
        for (Annotation ann : clazz.getAnnotations()) {
            Class<? extends Annotation> annType = ann.annotationType();

            try {
                Method valueMethod = annType.getMethod("value");
                Object value = valueMethod.invoke(ann);
                if (value instanceof String str && !str.isBlank()) {
                    return str;
                }
            } catch (NoSuchMethodException e) {
            } catch (ReflectiveOperationException e) {
                log.warn("애노테이션 값 추출 실패: {}", annType.getName(), e);
            }
        }
        return "";
    }
}
