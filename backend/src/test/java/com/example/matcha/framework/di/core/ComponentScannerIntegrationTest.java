package com.example.matcha.framework.di.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.matcha.framework.fixtures.BarComponent;
import com.example.matcha.framework.fixtures.FooComponent;
import com.example.matcha.framework.fixtures.NoAnnotationClass;
import com.example.matcha.framework.fixtures.ServiceWithDependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ComponentScannerIntegrationTest {
    private ComponentScanner scanner;
    private DIContainer container;
    private static final String FIXTURES_PACKAGE = "com.example.matcha.framework.fixtures";

    @BeforeEach
    void init() {
        container = new DIContainer();
        scanner = new ComponentScanner(container, FIXTURES_PACKAGE);
    }

    @Test
    @DisplayName("scan(): @Component(\"fooBean\") 붙으면 fooBean 이름으로 등록된다")
    void scan_shouldRegisterWithExplicitName() {
        scanner.scan();
        assertTrue(container.isRegistered("fooBean"), "명시적 이름(fooBean)으로 등록되어야 한다");
    }

    @Test
    @DisplayName("scan(): @Component 붙으면 기본 빈 이름으로 등록된다")
    void scan_shouldRegisterWithDefaultName() {
        scanner.scan();
        assertTrue(container.isRegistered("barComponent"), "기본 이름(barComponent)으로 등록되어야 한다");
    }

    @Test
    @DisplayName("scan(): @Component 없는 클래스는 등록되지 않는다")
    void scan_shouldNotRegisterClassWithoutAnnotation() {
        scanner.scan();

        assertFalse(container.isRegistered("noAnnotationClass"), "@Component 없는 클래스는 등록되지 않아야 한다");
        assertFalse(container.isRegistered(NoAnnotationClass.class), "클래스도 등록되지 않아야 한다");
    }

    @Test
    @DisplayName("scan(): 의존성이 있는 컴포넌트도 정상 등록된다")
    void scan_shouldRegisterComponentWithDependencies() {
        scanner.scan();

        assertTrue(container.isRegistered("serviceWithDependency"), "의존성 있는 컴포넌트도 등록되어야 한다");
        assertTrue(container.isRegistered("fooBean"), "의존성 있는 컴포넌트도 등록되어야 한다");
    }

    @Test
    @DisplayName("scan(): 스캔 후 초기화하면 모든 빈이 생성된다")
    void scan_shouldCreateAllBeansAfterInitialization() {
        scanner.scan();

        assertDoesNotThrow(() -> container.initialize());

        assertNotNull(container.getBean(FooComponent.class));
        assertNotNull(container.getBean(BarComponent.class));
        assertNotNull(container.getBean(ServiceWithDependency.class));
    }

    @Test
    @DisplayName("scan(): 존재하지 않는 패키지를 스캔해도 예외가 발생하지 않는다")
    void scan_shouldHandleNonExistentPackageGracefully() {
        ComponentScanner nonExistentScanner = new ComponentScanner(container, "com.nonexistent.package");

        assertDoesNotThrow(nonExistentScanner::scan, "존재하지 않는 패키지 스캔 시 예외 발생하지 않아야 한다");

        assertFalse(container.isRegistered("fooBean"));
        assertFalse(container.isRegistered("barComponent"));
    }

    @Test
    @DisplayName("scan(): 의존성 주입이 올바르게 동작한다")
    void scan_shouldInjectDependenciesCorrectly() {
        scanner.scan();
        container.initialize();

        ServiceWithDependency service= container.getBean(ServiceWithDependency.class);

        assertNotNull(service);

        assertDoesNotThrow(service::doService, "의존성이 주입된 서비스가 정상 동작해야 한다");
    }

    @Test
    @DisplayName("@Service 붙은 클래스도 스캔된다")
    void scan_shouldPickUpMetaAnnotatedService() {
        scanner.scan();
        assertTrue(container.isRegistered("metaService"));
    }
}

















