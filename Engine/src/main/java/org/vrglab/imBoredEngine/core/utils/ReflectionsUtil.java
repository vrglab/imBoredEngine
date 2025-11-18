package org.vrglab.imBoredEngine.core.utils;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class ReflectionsUtil {

    private final static String PACKAGES = "org.vrglab.imBoredEngine";

    static Reflections methodsFinder = new Reflections(
            new ConfigurationBuilder()
                    .forPackage(PACKAGES)
                    .filterInputsBy(new FilterBuilder().includePackage(PACKAGES))
                    .setScanners(Scanners.MethodsAnnotated)
    );

    static Reflections classesFinder = new Reflections(
            new ConfigurationBuilder()
                    .forPackage(PACKAGES)
                    .filterInputsBy(new FilterBuilder().includePackage(PACKAGES))
                    .setScanners(Scanners.TypesAnnotated)
    );


    public static <T extends Annotation> Map<Integer, Method> findPrioritisedMethods(Class<T> annotationClass) {
        Set<Method> methods = methodsFinder.getMethodsAnnotatedWith(annotationClass);

        Map<Integer, Method> methodMap = new TreeMap<>();

        for (Method method : methods) {
            T annotation = method.getAnnotation(annotationClass);

            methodMap.put(callMethod(annotation, "priority", Integer.class), method);
        }

        return methodMap;
    }

    public static <T extends Annotation> Set<Method> findMethods(Class<T> annotationClass) {
        return methodsFinder.getMethodsAnnotatedWith(annotationClass);
    }

    public static <T extends Annotation> Set<Class<?>> findClasses(Class<T> annotationClass) {
        return classesFinder.getTypesAnnotatedWith(annotationClass);
    }



    public static <T> T getField(Object target, String fieldName, Class<T> type) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return type.cast(field.get(target));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access field " + fieldName + " in " + target.getClass(), e);
        }
    }

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set field " + fieldName + " in " + target.getClass(), e);
        }
    }

    /**
     * Calls a private/protected method with no arguments
     */
    public static <T> T callMethod(Object target, String methodName, Class<T> returnType) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return returnType.cast(method.invoke(target));
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method " + methodName + " on " + target.getClass(), e);
        }
    }

    /**
     * Calls a private/protected method with arguments
     */
    public static <T> T callMethod(Object target, String methodName, Class<T> returnType, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return returnType.cast(method.invoke(target, args));
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method " + methodName + " on " + target.getClass(), e);
        }
    }

    /**
     * Instantiates a class dynamically using the no-arg constructor
     */
    public static <T> T createInstance(Class<T> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate class " + clazz, e);
        }
    }

    /**
     * Instantiate class with constructor arguments
     */
    public static <T> T createInstance(Class<T> clazz, Class<?>[] paramTypes, Object... args) {
        try {
            var constructor = clazz.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate class " + clazz, e);
        }
    }


    /**
     * Gets a private nested class type by name
     */
    public static Class<?> getNestedClass(Class<?> outerClass, String nestedName)  {
        for (Class<?> inner : outerClass.getDeclaredClasses()) {
            if (inner.getSimpleName().equals(nestedName)) {
                return inner;
            }
        }
        throw new RuntimeException("Nested class " + nestedName + " not found in " + outerClass);
    }

    /**
     * Instantiates a nested class with explicit constructor types
     */
    public static Object createNestedInstance(Class<?> nestedClass, Object outerInstance, Class<?>[] paramTypes, Object... args) {
        try {
            Object[] finalArgs;

            if (nestedClass.getEnclosingClass() != null && !Modifier.isStatic(nestedClass.getModifiers())) {
                if (outerInstance == null)
                    throw new IllegalArgumentException("Outer instance cannot be null for non-static nested class");

                finalArgs = new Object[args.length + 1];
                finalArgs[0] = outerInstance;
                System.arraycopy(args, 0, finalArgs, 1, args.length);
            } else {
                finalArgs = args;
            }

            var constructor = nestedClass.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(finalArgs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate nested class " + nestedClass, e);
        }
    }

    /**
     * Checks if the given type is the same as, or a subclass (or sub-subclass) of the parent class
     */
    public static boolean isSubclassOrSame(Class<?> type, Class<?> parent) {
        if (type == null || parent == null) return false;
        return parent.isAssignableFrom(type);
    }
}
