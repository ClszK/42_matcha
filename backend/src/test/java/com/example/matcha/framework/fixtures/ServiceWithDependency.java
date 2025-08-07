package com.example.matcha.framework.fixtures;

import com.example.matcha.framework.di.annotaion.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceWithDependency {
    private final FooComponent fooComponent;

    public void doService() {
        System.out.println("Service Execute");
    }
}
