package com.example.matcha.framework.di.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.matcha.framework.fixtures.TestServices.A;
import com.example.matcha.framework.fixtures.TestServices.B;
import com.example.matcha.framework.fixtures.TestServices.C;
import com.example.matcha.framework.fixtures.TestServices.Circular1;
import com.example.matcha.framework.fixtures.TestServices.Circular2;
import com.example.matcha.framework.fixtures.TestServices.DeepA;
import com.example.matcha.framework.fixtures.TestServices.DeepB;
import com.example.matcha.framework.fixtures.TestServices.DeepC;
import com.example.matcha.framework.fixtures.TestServices.DeepD;
import com.example.matcha.framework.fixtures.TestServices.DeepE;
import com.example.matcha.framework.fixtures.TestServices.ServiceA;
import com.example.matcha.framework.fixtures.TestServices.ServiceB;
import com.example.matcha.framework.fixtures.TestServices.ServiceNeedingDependency;
import com.example.matcha.framework.fixtures.TestServices.ServiceOnlyParameterizedConstructor;
import com.example.matcha.framework.fixtures.TestServices.ServiceWithFailingConstructor;
import com.example.matcha.framework.fixtures.TestServices.ServiceWithPrivateConstructor;
import com.example.matcha.framework.fixtures.TestServices.SharedDependency;
import com.example.matcha.framework.fixtures.TestServices.SimpleDependency;
import com.example.matcha.framework.fixtures.TestServices.SimpleService;
import com.example.matcha.framework.fixtures.TestServices.UnregisteredClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DIContainerTest {

    @Test
    @DisplayName("단순 빈을 생성할 수 있어야 한다")
    void shouldCreateSimpleBean() {
        DIContainer c = new DIContainer();
        c.register(SimpleService.class);
        c.initialize();
        assertNotNull(c.getBean(SimpleService.class));
    }

    @Test
    @DisplayName("생성자를 통한 의존성 주입이 가능해야 한다")
    void shouldInjectConstructorDependency() {
        DIContainer c = new DIContainer();
        c.register(A.class, B.class, C.class);
        c.initialize();

        A a = c.getBean(A.class);
        assertNotNull(a.getB());
    }

    @Test
    @DisplayName("의존성 그래프가 복잡해도 올바르게 주입되어야 한다")
    void shouldHandleComplexDependencyGraph() {
        DIContainer c = new DIContainer();
        c.register(C.class, B.class, A.class);
        c.initialize();

        A a1 = c.getBean(A.class);
        assertNotNull(a1.getB());
        assertNotNull(a1.getB().getC());
    }

    @Test
    @DisplayName("싱글톤 패턴처럼 동일한 인스턴스를 반환해야 한다")
    void shouldReturnSameInstance() {
        DIContainer c = new DIContainer();
        c.register(SimpleService.class);
        c.initialize();

        SimpleService instance1 = c.getBean(SimpleService.class);
        SimpleService instance2 = c.getBean(SimpleService.class);

        assertSame(instance1, instance2);
    }


    @Test
    @DisplayName("등록되지 않은 클래스를 요청하면 예외가 발생해야 한다")
    void shouldThrowExceptionForUnregisteredClass() {
        DIContainer c = new DIContainer();
        c.initialize();

        assertThrows(RuntimeException.class, () ->
                c.getBean(UnregisteredClass.class)
        );
    }

    @Test
    @DisplayName("순환 참조가 있는 경우 적절히 처리되어야 한다")
    void shouldHandleCircularDependencies() {
        DIContainer c = new DIContainer();
        c.register(Circular1.class, Circular2.class);

        assertThrows(RuntimeException.class, c::initialize);
    }

    @Test
    @DisplayName("initialize 를 호출하지 않아도 getBean 으로 인스턴스를 생성할 수 있어야 한다")
    void shouldCreateInstanceOnDemand() {
        DIContainer c = new DIContainer();
        c.register(SimpleService.class);

        SimpleService service = c.getBean(SimpleService.class);
        assertNotNull(service);
    }

    @Test
    @DisplayName("등록된 클래스가 등록되지 않은 의존성을 가지면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenDependencyNotRegistered() {
        DIContainer c = new DIContainer();
        c.register(ServiceNeedingDependency.class);

        assertThrows(RuntimeException.class, c::initialize);
    }

    @Test
    @DisplayName("기본 생성자가 없고 매개변수 생성자만 있을 때 적절히 선택해야 한다")
    void shouldChooseParameterizedConstructorWhenNoDefault() {
        DIContainer c = new DIContainer();
        c.register(ServiceOnlyParameterizedConstructor.class, SimpleDependency.class);
        c.initialize();

        ServiceOnlyParameterizedConstructor service = c.getBean(ServiceOnlyParameterizedConstructor.class);
        assertNotNull(service.getDependency());
    }

    @Test
    @DisplayName("private 생성자만 있으면 예외가 발생해야 한다")
    void shouldThrowExceptionForPrivateConstructorOnly() {
        DIContainer c = new DIContainer();
        c.register(ServiceWithPrivateConstructor.class);

        assertThrows(RuntimeException.class, c::initialize);
    }

    @Test
    @DisplayName("생성자에서 예외가 발생하면 적절히 처리해야 한다")
    void shouldHandleConstructorException() {
        DIContainer c = new DIContainer();
        c.register(ServiceWithFailingConstructor.class);

        RuntimeException exception = assertThrows(RuntimeException.class, c::initialize);

        assertTrue(exception.getMessage().contains("빈 생성 실패"));


    }

    @Test
    @DisplayName("예외 발생 후 creating 상태가 정리되어야 한다")
    void shouldCleanUpCreatingStateAfterException() {
        DIContainer c = new DIContainer();
        c.register(ServiceWithFailingConstructor.class);

        assertThrows(RuntimeException.class, c::initialize);
        assertThrows(RuntimeException.class, c::initialize);
        assertThrows(RuntimeException.class, c::initialize);
        assertThrows(RuntimeException.class, c::initialize);
    }

    @Test
    @DisplayName("null 클래스 등록 시 예외가 발생해야 한다")
    void shouldThrowExceptionForNullClass() {
        DIContainer c = new DIContainer();

        assertThrows(NullPointerException.class, () ->
                c.register((Class<?>) null)
        );
    }

    @Test
    @DisplayName("null 파라미터로 getBean 호출 시 예외가 발생해야 한다")
    void shouldThrowExceptionForNullGetBean() {
        DIContainer c = new DIContainer();

        assertThrows(NullPointerException.class, () ->
                c.getBean(null)
        );
    }

    @Test
    @DisplayName("깊은 의존성 체인도 올바르게 처리해야 한다")
    void shouldHandleDeepDependencyChain() {
        DIContainer c = new DIContainer();
        c.register(DeepA.class, DeepB.class, DeepC.class, DeepD.class, DeepE.class);
        c.initialize();

        DeepA a = c.getBean(DeepA.class);
        assertNotNull(a);
        assertNotNull(a.getB());
        assertNotNull(a.getB().getC());
        assertNotNull(a.getB().getC().getD());
        assertNotNull(a.getB().getC().getD().getE());
    }

    @Test
    @DisplayName("중복 등록해도 문제없이 동작해야 한다")
    void shouldHandleDuplicateRegistration() {
        DIContainer c = new DIContainer();
        c.register(SimpleService.class);
        c.register(SimpleService.class);
        c.register(SimpleService.class);
        c.initialize();

        SimpleService service1 = c.getBean(SimpleService.class);
        SimpleService service2 = c.getBean(SimpleService.class);

        assertSame(service1, service2);
    }

    @Test
    @DisplayName("여러 클래스가 같은 의존성을 공유해도 싱글톤이어야 한다")
    void shouldShareSingletonDependency() {
        DIContainer c = new DIContainer();
        c.register(ServiceA.class, ServiceB.class, SharedDependency.class);
        c.initialize();

        ServiceA serviceA = c.getBean(ServiceA.class);
        ServiceB serviceB = c.getBean(ServiceB.class);

        assertSame(serviceA.getSharedDependency(), serviceB.getSharedDependency());
    }
}

