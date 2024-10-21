package persistence.study;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ReflectionTest {

    @Test
    @DisplayName("Car 객체 정보 가져오기")
    void showClass() {
        Class<Car> carClass = Car.class;
        assertAll("Car 클래스의 모든 필드, 생성자, 메소드 정보 검증",
                () -> assertThat(Arrays.stream(carClass.getDeclaredFields())
                        .map(Formatter::formatField)
                        .toList())
                        .containsExactlyInAnyOrder("private String name", "private int price"),
                () -> assertThat(Arrays.stream(carClass.getDeclaredConstructors())
                        .map(Formatter::formatConstructor)
                        .toList())
                        .containsExactlyInAnyOrder("public persistence.study.Car(String, int)", "public persistence.study.Car()"),
                () -> assertThat(Arrays.stream(carClass.getDeclaredMethods())
                        .map(Formatter::formatMethod)
                        .toList())
                        .containsExactlyInAnyOrder("public void printView()", "public String testGetName()", "public String testGetPrice()")
        );
    }

    @Test
    @DisplayName("test 로 시작하는 메소드 실행")
    void testMethodRun() {
        Car car = new Car("Dream Car", 100_000_000);
        assertThat(Arrays.stream(car.getClass().getMethods())
                .filter(method -> method.getName().startsWith("test"))
                .map(method -> invokeMethod(method, car))
        ).containsExactly("test : Dream Car", "test : 100000000");
    }

    @Test
    @DisplayName("@PrintView 애노테이션 메소드 실행")
    void testAnnotationMethodRun() {
        Class<Car> carClass = Car.class;
        Car mockCar = mock(carClass);
        Arrays.stream(carClass.getMethods())
                        .filter(method -> method.isAnnotationPresent(PrintView.class))
                        .forEach(method -> invokeMethod(method, mockCar));
        verify(mockCar).printView();
    }

    private Object invokeMethod(Method method, Car car) {
        try {
            return method.invoke(car);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("private field 에 값 할당")
    void privateFieldAccess() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        Class<Car> carClass = Car.class;

        Field nameField = carClass.getDeclaredField("name");
        nameField.setAccessible(true);

        Field priceField = carClass.getDeclaredField("price");
        priceField.setAccessible(true);

        Car car = carClass.getDeclaredConstructor().newInstance();
        nameField.set(car, "드림카");
        priceField.set(car, 100_000_000);

        assertAll("Car 객체 필드값 설정 검증",
                () -> assertEquals(nameField.get(car), "드림카"),
                () -> assertEquals(priceField.get(car), 100_000_000));
    }

    @Test
    @DisplayName("인자를 가진 생성자의 인스턴스 생성")
    void constructorWithArgs() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Class<Car> carClass = Car.class;
        Constructor<Car> constructor = carClass.getDeclaredConstructor(String.class, int.class);
        Car car = constructor.newInstance("드림카", 100_000_000);

        Field nameField = carClass.getDeclaredField("name");
        nameField.setAccessible(true);

        Field priceField = carClass.getDeclaredField("price");
        priceField.setAccessible(true);

        assertAll("Car 객체 필드값 초기화 검증",
                () -> assertEquals(nameField.get(car), "드림카"),
                () -> assertEquals(priceField.get(car), 100_000_000));
    }

    static class Formatter {

        public static String formatField(Field field) {
            return MessageFormat.format("{0} {1} {2}",
                    Modifier.toString(field.getModifiers()),
                    field.getType().getSimpleName(),
                    field.getName());
        }

        public static String formatConstructor(Constructor<?> constructor) {
            return MessageFormat.format("{0} {1}({2})",
                    Modifier.toString(constructor.getModifiers()),
                    constructor.getName(),
                    formatParameterTypes(constructor.getParameterTypes()));
        }

        public static String formatMethod(Method method) {
            return MessageFormat.format("{0} {1} {2}({3})",
                    Modifier.toString(method.getModifiers()),
                    method.getReturnType().getSimpleName(),
                    method.getName(),
                    formatParameterTypes(method.getParameterTypes()));
        }

        public static String formatParameterTypes(Class<?>[] types) {
            return String.join(", ",
                    Arrays.stream(types).map(Class::getSimpleName).toList());
        }

    }
}
