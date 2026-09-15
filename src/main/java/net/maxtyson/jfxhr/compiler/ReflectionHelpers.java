package net.maxtyson.jfxhr.compiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Objects;

public class ReflectionHelpers {


    public static boolean isArgValue(Object value, Object[] args) {

        // No value or arg to check
        if (value == null || args == null)
            return false;

        // See if the value is an args
        for (Object arg : args)
            if (Objects.equals(value, arg))
                return true;

        return false;
    }


    public static Object[] getArgs(Object oldInstance, Object newInstance, Constructor<?> constructor) throws Exception {

        // Store the args that have been chosen
        Parameter[] params = constructor.getParameters();
        Object[] args = new Object[params.length];

        Object[] oldArgs = internalReadArgs(oldInstance);
        Object[] newArgs = internalReadArgs(newInstance);

        for (int i = 0; i < params.length; i++) {

            // New args aren't known, so just assume it must be more up to date
            if (newArgs == null) {
                args[i] = readField(oldInstance, params[i].getName());
                continue;
            }

            // If the new arg isn't on the old constructor or they are different, then the new code must be providing a more up to date arg
            boolean parentChangedArg = oldArgs == null || i >= oldArgs.length || !Objects.equals(oldArgs[i], newArgs[i]);
            args[i] = parentChangedArg ? newArgs[i] : readField(oldInstance, params[i].getName());
        }

        return args;
    }


    public static boolean isDefaultValue(Object val, Class<?> type) {

        if (val == null)
            return true;

        if (type == boolean.class)
            return Boolean.FALSE.equals(val);

        if (type.isPrimitive())
            return ((Number) val).doubleValue() == 0.0;

        if (type == char.class)
            return Character.valueOf('\0').equals(val);

        return false;
    }


    ///@todo make an internalRead(Inst, Field)
    public static Class<?>[] internalReadArgsTypes(Object instance) {

        // Nothing to pull from
        if (instance == null)
            return null;

        try {

            Object[] argsTypes = (Object[])readField(instance, "__jfxhr_args_types");

            // Yet to be compiled by hotfx so doesn't have the injected metadata
            if (argsTypes == null)
                return null;

            // Read and clone the types
            Class<?>[] types = new Class<?>[argsTypes.length];
            for (int i = 0; i < argsTypes.length; i++)
                types[i] = (Class<?>) argsTypes[i];

            return types;


        } catch (Exception e) {
            return null;
        }
    }

    public static Object[] internalReadArgs(Object instance) {

        // Nothing to pull from
        if (instance == null)
            return null;

        try {

             Object[] args = (Object[])readField(instance, "__jfxhr_args");

            // Yet to be compiled by hotfx so doesn't have the injected metadata
            if (args == null)
                return null;

            return args;

        }  catch (Exception e) {
            return null;
        }
    }



    private static Object readField(Object instance, String name) throws Exception {

        // Force field to be able to be read
        Field field = instance.getClass().getDeclaredField(name);
        field.setAccessible(true);

        // Parse the value of the field
        return field.get(instance);
    }
}
