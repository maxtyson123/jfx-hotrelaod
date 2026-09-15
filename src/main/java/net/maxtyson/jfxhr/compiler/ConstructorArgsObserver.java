package net.maxtyson.jfxhr.compiler;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class ConstructorArgsObserver {

    @Advice.OnMethodExit
    public static void onExit(@Advice.This Object self, @Advice.AllArguments Object[] args, @Advice.Origin Constructor<?> constructor) throws Exception {

        // Inject a custom metadata feild to contain the args values
        Field argsMeta = self.getClass().getDeclaredField("__jfxhr_args");
        argsMeta.setAccessible(true);
        argsMeta.set(self, args.clone());

        // Inject a custom metadata feild to contain the constructor to use
        Field argsTypesMeta = self.getClass().getDeclaredField("__jfxhr_args_types");
        argsTypesMeta.setAccessible(true);
        argsTypesMeta.set(self, constructor.getParameterTypes().clone());
    }
}
