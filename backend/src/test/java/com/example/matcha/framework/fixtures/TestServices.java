package com.example.matcha.framework.fixtures;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class TestServices {

    public static class SimpleService {
    }

    @Getter
    @RequiredArgsConstructor
    public static class A {
        private final B b;
    }

    @Getter
    @RequiredArgsConstructor
    public static class B {
        private final C c;
    }

    public static class C {
    }

    public static class UnregisteredClass {
    }

    @RequiredArgsConstructor
    public static class Circular1 {
        private final Circular2 c2;
    }

    @RequiredArgsConstructor
    public static class Circular2 {
        private final Circular1 c1;
    }

    @RequiredArgsConstructor
    public static class ServiceNeedingDependency {
        private final Repository repository;
    }

    private static class Repository {
    }

    @Getter
    @RequiredArgsConstructor
    public static class ServiceOnlyParameterizedConstructor {
        private final SimpleDependency dependency;
    }

    public static class SimpleDependency {
    }

    public static class ServiceWithPrivateConstructor {
        private ServiceWithPrivateConstructor() {
            throw new RuntimeException();
        }
    }

    public static class ServiceWithFailingConstructor {
        public ServiceWithFailingConstructor() {
            throw new RuntimeException();
        }
    }

    public static class DeepE {
    }

    @Getter
    @RequiredArgsConstructor
    public static class DeepD {
        private final DeepE e;
    }

    @Getter
    @RequiredArgsConstructor
    public static class DeepC {
        private final DeepD D;
    }

    @Getter
    @RequiredArgsConstructor
    public static class DeepB {
        private final DeepC c;
    }

    @Getter
    @RequiredArgsConstructor
    public static class DeepA {
        private final DeepB b;
    }

    public static class SharedDependency {
    }

    @Getter
    @RequiredArgsConstructor
    public static class ServiceB {
        private final SharedDependency sharedDependency;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ServiceA {
        private final SharedDependency sharedDependency;
    }

    public static class OtherService {
    }
}
