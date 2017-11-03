/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.provider.jffi;

import com.kenai.jffi.Platform;
import jnr.ffi.Address;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.NumberUtil.*;

final class AsmUtil {
    private AsmUtil() {}
    
    public static MethodVisitor newTraceMethodVisitor(MethodVisitor mv) {
        try {
            Class<? extends MethodVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceMethodVisitor").asSubclass(MethodVisitor.class);
            Constructor<? extends MethodVisitor> c = tmvClass.getDeclaredConstructor(MethodVisitor.class);
            return c.newInstance(mv);
        } catch (Throwable t) {
            return mv;
        }
    }

    public static ClassVisitor newTraceClassVisitor(ClassVisitor cv, OutputStream out) {
        return newTraceClassVisitor(cv, new PrintWriter(out, true));
    }

    public static ClassVisitor newTraceClassVisitor(ClassVisitor cv, PrintWriter out) {
        try {

            Class<? extends ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class, PrintWriter.class);
            return c.newInstance(cv, out);
        } catch (Throwable t) {
            return cv;
        }
    }

    public static ClassVisitor newTraceClassVisitor(PrintWriter out) {
        try {

            Class<? extends ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(PrintWriter.class);
            return c.newInstance(out);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static ClassVisitor newCheckClassAdapter(ClassVisitor cv) {
        try {
            Class<? extends ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.CheckClassAdapter").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class);
            return c.newInstance(cv);
        } catch (Throwable t) {
            return cv;
        }
    }

    public static Class unboxedReturnType(Class type) {
        return unboxedType(type);
    }

    public static Class unboxedType(Class boxedType) {
        if (boxedType == Byte.class) {
            return byte.class;

        } else if (boxedType == Short.class) {
            return short.class;

        } else if (boxedType == Character.class) {
            return char.class;

        } else if (boxedType == Integer.class) {
            return int.class;

        } else if (boxedType == Long.class) {
            return long.class;

        } else if (boxedType == Float.class) {
            return float.class;

        } else if (boxedType == Double.class) {
            return double.class;

        } else if (boxedType == Boolean.class) {
            return boolean.class;

        } else if (Pointer.class.isAssignableFrom(boxedType)) {
            return Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else if (Address.class == boxedType) {
            return Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else {
            return boxedType;
        }
    }

    public static Class boxedType(Class type) {
        if (type == byte.class) {
            return Byte.class;
        } else if (type == short.class) {
            return Short.class;
        } else if (type == char.class) {
            return Character.class;
        } else if (type == int.class) {
            return Integer.class;
        } else if (type == long.class) {
            return Long.class;
        } else if (type == float.class) {
            return Float.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == boolean.class) {
            return Boolean.class;
        } else {
            return type;
        }
    }

    
    static void emitReturnOp(SkinnyMethodAdapter mv, Class returnType) {
        if (!returnType.isPrimitive()) {
            mv.areturn();
        } else if (long.class == returnType) {
            mv.lreturn();
        } else if (float.class == returnType) {
            mv.freturn();
        } else if (double.class == returnType) {
            mv.dreturn();
        } else if (void.class == returnType) {
            mv.voidreturn();
        } else {
            mv.ireturn();
        }
    }

    /**
     * Calculates the size of a local variable
     *
     * @param type The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(Class type) {
        return long.class == type || double.class == type ? 2 : 1;
    }

    /**
     * Calculates the size of a local variable
     *
     * @param type The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(SigType type) {
        return calculateLocalVariableSpace(type.getDeclaredType());
    }

    /**
     * Calculates the size of a list of types in the local variable area.
     *
     * @param types The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(Class... types) {
        int size = 0;

        for (int i = 0; i < types.length; ++i) {
            size += calculateLocalVariableSpace(types[i]);
        }

        return size;
    }

    /**
     * Calculates the size of a list of types in the local variable area.
     *
     * @param types The type of parameter
     * @return The size in parameter units
     */
    static int calculateLocalVariableSpace(SigType... types) {
        int size = 0;

        for (SigType type : types) {
            size += calculateLocalVariableSpace(type);
        }

        return size;
    }

    private static void unboxPointerOrStruct(final SkinnyMethodAdapter mv, final Class type, final Class nativeType) {
        mv.invokestatic(p(AsmRuntime.class), long.class == nativeType ? "longValue" : "intValue",
                sig(nativeType, type));
    }

    static void unboxPointer(final SkinnyMethodAdapter mv, final Class nativeType) {
        unboxPointerOrStruct(mv, Pointer.class, nativeType);
    }

    static void unboxBoolean(final SkinnyMethodAdapter mv, Class boxedType, final Class nativeType) {
        mv.invokevirtual(p(boxedType), "booleanValue", "()Z");
        widen(mv, boolean.class, nativeType);
    }

    static void unboxBoolean(final SkinnyMethodAdapter mv, final Class nativeType) {
        unboxBoolean(mv, Boolean.class, nativeType);
    }

    static void unboxNumber(final SkinnyMethodAdapter mv, final Class boxedType, final Class unboxedType,
                                  final jnr.ffi.NativeType nativeType) {

        if (Number.class.isAssignableFrom(boxedType)) {

            switch (nativeType) {
                case SCHAR:
                case UCHAR:
                    mv.invokevirtual(p(boxedType), "byteValue", "()B");
                    convertPrimitive(mv, byte.class, unboxedType, nativeType);
                    break;

                case SSHORT:
                case USHORT:
                    mv.invokevirtual(p(boxedType), "shortValue", "()S");
                    convertPrimitive(mv, short.class, unboxedType, nativeType);
                    break;

                case SINT:
                case UINT:
                case SLONG:
                case ULONG:
                case ADDRESS:
                    if (sizeof(nativeType) == 4) {
                        mv.invokevirtual(p(boxedType), "intValue", "()I");
                        convertPrimitive(mv, int.class, unboxedType, nativeType);
                    } else {
                        mv.invokevirtual(p(boxedType), "longValue", "()J");
                        convertPrimitive(mv, long.class, unboxedType, nativeType);
                    }
                    break;

                case SLONGLONG:
                case ULONGLONG:
                    mv.invokevirtual(p(boxedType), "longValue", "()J");
                    narrow(mv, long.class, unboxedType);
                    break;

                case FLOAT:
                    mv.invokevirtual(p(boxedType), "floatValue", "()F");
                    break;

                case DOUBLE:
                    mv.invokevirtual(p(boxedType), "doubleValue", "()D");
                    break;
            }


        } else if (Boolean.class.isAssignableFrom(boxedType)) {
            unboxBoolean(mv, unboxedType);

        } else {
            throw new IllegalArgumentException("unsupported boxed type: " + boxedType);
        }
    }


    static void unboxNumber(final SkinnyMethodAdapter mv, final Class boxedType, final Class nativeType) {

        if (Number.class.isAssignableFrom(boxedType)) {

            if (byte.class == nativeType) {
                mv.invokevirtual(p(boxedType), "byteValue", "()B");

            } else if (short.class == nativeType) {
                mv.invokevirtual(p(boxedType), "shortValue", "()S");

            } else if (int.class == nativeType) {
                mv.invokevirtual(p(boxedType), "intValue", "()I");

            } else if (long.class == nativeType) {
                mv.invokevirtual(p(boxedType), "longValue", "()J");

            } else if (float.class == nativeType) {
                mv.invokevirtual(p(boxedType), "floatValue", "()F");

            } else if (double.class == nativeType) {
                mv.invokevirtual(p(boxedType), "doubleValue", "()D");

            } else {
                throw new IllegalArgumentException("unsupported Number subclass: " + boxedType);
            }

        } else if (Boolean.class.isAssignableFrom(boxedType)) {
            unboxBoolean(mv, nativeType);

        } else {
            throw new IllegalArgumentException("unsupported boxed type: " + boxedType);
        }
    }

    static void boxValue(AsmBuilder builder, SkinnyMethodAdapter mv, Class boxedType, Class unboxedType) {
        if (boxedType == unboxedType || boxedType.isPrimitive()) {

        } else if (Boolean.class.isAssignableFrom(boxedType)) {
            narrow(mv, unboxedType, boolean.class);
            mv.invokestatic(Boolean.class, "valueOf", Boolean.class, boolean.class);

        } else if (Pointer.class.isAssignableFrom(boxedType)) {
            getfield(mv, builder, builder.getRuntimeField());
            mv.invokestatic(AsmRuntime.class, "pointerValue", Pointer.class, unboxedType, jnr.ffi.Runtime.class);

        } else if (Address.class == boxedType) {
            mv.invokestatic(boxedType, "valueOf", boxedType, unboxedType);

        } else if (Number.class.isAssignableFrom(boxedType) && boxedType(unboxedType) == boxedType) {
            mv.invokestatic(boxedType, "valueOf", boxedType, unboxedType);

        } else if (Character.class == boxedType) {
            convertPrimitive(mv, unboxedType, char.class);
            mv.invokestatic(boxedType, "valueOf", boxedType, unboxedType);

        } else {
            throw new IllegalArgumentException("cannot box value of type " + unboxedType + " to " + boxedType);
        }
    }

    static int getNativeArrayFlags(int flags) {
        int nflags = 0;
        nflags |= ParameterFlags.isIn(flags) ? com.kenai.jffi.ArrayFlags.IN : 0;
        nflags |= ParameterFlags.isOut(flags) ? com.kenai.jffi.ArrayFlags.OUT : 0;
        nflags |= (ParameterFlags.isNulTerminate(flags) || ParameterFlags.isIn(flags))
                ? com.kenai.jffi.ArrayFlags.NULTERMINATE : 0;
        return nflags;
    }

    static int getNativeArrayFlags(Collection<Annotation> annotations) {
        return getNativeArrayFlags(ParameterFlags.parse(annotations));
    }

    static LocalVariable[] getParameterVariables(ParameterType[] parameterTypes) {
        LocalVariable[] lvars = new LocalVariable[parameterTypes.length];
        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            lvars[i] = new LocalVariable(parameterTypes[i].getDeclaredType(), lvar);
            lvar += calculateLocalVariableSpace(parameterTypes[i]);
        }

        return lvars;
    }

    static LocalVariable[] getParameterVariables(Class[] parameterTypes) {
        LocalVariable[] lvars = new LocalVariable[parameterTypes.length];
        int idx = 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            lvars[i] = new LocalVariable(parameterTypes[i], idx);
            idx += calculateLocalVariableSpace(parameterTypes[i]);
        }

        return lvars;
    }

    static void load(SkinnyMethodAdapter mv, Class parameterType, LocalVariable parameter) {
        if (!parameterType.isPrimitive()) {
            mv.aload(parameter);

        } else if (long.class == parameterType) {
            mv.lload(parameter);

        } else if (float.class == parameterType) {
            mv.fload(parameter);

        } else if (double.class == parameterType) {
            mv.dload(parameter);

        } else {
            mv.iload(parameter);
        }

    }


    static void store(SkinnyMethodAdapter mv, Class type, LocalVariable var) {
        if (!type.isPrimitive()) {
            mv.astore(var);

        } else if (long.class == type) {
            mv.lstore(var);

        } else if (double.class == type) {
            mv.dstore(var);

        } else if (float.class == type) {
            mv.fstore(var);

        } else {
            mv.istore(var);
        }
    }

    static void emitReturn(AsmBuilder builder, SkinnyMethodAdapter mv, Class returnType, Class nativeIntType) {
        if (returnType.isPrimitive()) {

            if (long.class == returnType) {
                mv.lreturn();

            } else if (float.class == returnType) {
                mv.freturn();

            } else if (double.class == returnType) {
                mv.dreturn();

            } else if (void.class == returnType) {
                mv.voidreturn();

            } else {
                mv.ireturn();
            }

        } else {
            boxValue(builder, mv, returnType, nativeIntType);
            mv.areturn();
        }
    }

    static void getfield(SkinnyMethodAdapter mv, AsmBuilder builder, AsmBuilder.ObjectField field) {
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), field.name, ci(field.klass));
    }

    static void tryfinally(SkinnyMethodAdapter mv, Runnable codeBlock, Runnable finallyBlock) {
        Label before = new Label(), after = new Label(), ensure = new Label(), done = new Label();
        mv.trycatch(before, after, ensure, null);
        mv.label(before);
        codeBlock.run();
        mv.label(after);
        if (finallyBlock != null) finallyBlock.run();
        mv.go_to(done);
        if (finallyBlock != null) {
            mv.label(ensure);
            finallyBlock.run();
            mv.athrow();
        }
        mv.label(done);
    }

    static void emitToNativeConversion(AsmBuilder builder, SkinnyMethodAdapter mv, ToNativeType toNativeType) {
        ToNativeConverter parameterConverter = toNativeType.getToNativeConverter();
        if (parameterConverter != null) {
            Method toNativeMethod = getToNativeMethod(toNativeType, builder.getClassLoader());

            if (toNativeType.getDeclaredType().isPrimitive()) {
                boxValue(builder, mv, getBoxedClass(toNativeType.getDeclaredType()), toNativeType.getDeclaredType());
            }
            if (!toNativeMethod.getParameterTypes()[0].isAssignableFrom(getBoxedClass(toNativeType.getDeclaredType()))) {
                mv.checkcast(toNativeMethod.getParameterTypes()[0]);
            }

            mv.aload(0);
            AsmBuilder.ObjectField toNativeConverterField = builder.getToNativeConverterField(parameterConverter);
            mv.getfield(builder.getClassNamePath(), toNativeConverterField.name, ci(toNativeConverterField.klass));
            if (!toNativeMethod.getDeclaringClass().equals(toNativeConverterField.klass)) {
                mv.checkcast(toNativeMethod.getDeclaringClass());
            }

            // Re-order so the value to be converted is on the top of the stack
            mv.swap();

            // load context parameter (if there is one)
            if (toNativeType.getToNativeContext() != null) {
                getfield(mv, builder, builder.getToNativeContextField(toNativeType.getToNativeContext()));
            } else {
                mv.aconst_null();
            }

            if (toNativeMethod.getDeclaringClass().isInterface()) {
                mv.invokeinterface(toNativeMethod.getDeclaringClass(), toNativeMethod.getName(),
                        toNativeMethod.getReturnType(), toNativeMethod.getParameterTypes());
            } else {
                mv.invokevirtual(toNativeMethod.getDeclaringClass(), toNativeMethod.getName(),
                        toNativeMethod.getReturnType(), toNativeMethod.getParameterTypes());
            }
            if (!parameterConverter.nativeType().isAssignableFrom(toNativeMethod.getReturnType())) {
                mv.checkcast(p(parameterConverter.nativeType()));
            }
        }
    }

    static void emitFromNativeConversion(AsmBuilder builder, SkinnyMethodAdapter mv, FromNativeType fromNativeType, Class nativeClass) {
        // If there is a result converter, retrieve it and put on the stack
        FromNativeConverter fromNativeConverter = fromNativeType.getFromNativeConverter();
        if (fromNativeConverter != null) {
            convertPrimitive(mv, nativeClass, unboxedType(fromNativeConverter.nativeType()), fromNativeType.getNativeType());
            boxValue(builder, mv, fromNativeConverter.nativeType(), nativeClass);

            Method fromNativeMethod = getFromNativeMethod(fromNativeType, builder.getClassLoader());
            getfield(mv, builder, builder.getFromNativeConverterField(fromNativeConverter));
            mv.swap();
            if (fromNativeType.getFromNativeContext() != null) {
                getfield(mv, builder, builder.getFromNativeContextField(fromNativeType.getFromNativeContext()));
            } else {
                mv.aconst_null();
            }

            if (fromNativeMethod.getDeclaringClass().isInterface()) {
                mv.invokeinterface(fromNativeMethod.getDeclaringClass(), fromNativeMethod.getName(),
                        fromNativeMethod.getReturnType(), fromNativeMethod.getParameterTypes());
            } else {
                mv.invokevirtual(fromNativeMethod.getDeclaringClass(), fromNativeMethod.getName(),
                        fromNativeMethod.getReturnType(), fromNativeMethod.getParameterTypes());
            }

            if (fromNativeType.getDeclaredType().isPrimitive()) {
                // The actual return type is a primitive, but there was a converter for it - extract the primitive value
                Class boxedType = getBoxedClass(fromNativeType.getDeclaredType());
                if (!boxedType.isAssignableFrom(fromNativeMethod.getReturnType())) mv.checkcast(p(boxedType));
                unboxNumber(mv, boxedType, fromNativeType.getDeclaredType(), fromNativeType.getNativeType());

            } else if (!fromNativeType.getDeclaredType().isAssignableFrom(fromNativeMethod.getReturnType())) {
                mv.checkcast(p(fromNativeType.getDeclaredType()));
            }

        } else if (!fromNativeType.getDeclaredType().isPrimitive()) {
            Class unboxedType = unboxedType(fromNativeType.getDeclaredType());
            convertPrimitive(mv, nativeClass, unboxedType, fromNativeType.getNativeType());
            boxValue(builder, mv, fromNativeType.getDeclaredType(), unboxedType);

        }
    }

    static Method getToNativeMethod(ToNativeType toNativeType, AsmClassLoader classLoader) {
        ToNativeConverter toNativeConverter = toNativeType.getToNativeConverter();
        if (toNativeConverter == null) {
            return null;
        }

        try {
            Class<? extends ToNativeConverter> toNativeConverterClass = toNativeConverter.getClass();
            if (Modifier.isPublic(toNativeConverterClass.getModifiers())) {
                for (Method method : toNativeConverterClass.getMethods()) {
                    if (!method.getName().equals("toNative")) continue;
                    Class[] methodParameterTypes = method.getParameterTypes();
                    if (toNativeConverter.nativeType().isAssignableFrom(method.getReturnType())
                            && methodParameterTypes.length == 2
                            && methodParameterTypes[0].isAssignableFrom(toNativeType.getDeclaredType())
                            && methodParameterTypes[1] == ToNativeContext.class
                            && methodIsAccessible(method)
                            && classIsVisible(classLoader, method.getDeclaringClass())) {
                        return method;
                    }
                }
            }
            Method method = toNativeConverterClass.getMethod("toNative", Object.class, ToNativeContext.class);
            return methodIsAccessible(method) && classIsVisible(classLoader, method.getDeclaringClass())
                    ? method : ToNativeConverter.class.getDeclaredMethod("toNative", Object.class, ToNativeContext.class);

        } catch (NoSuchMethodException nsme) {
            try {
                return ToNativeConverter.class.getDeclaredMethod("toNative", Object.class, ToNativeContext.class);
            } catch (NoSuchMethodException nsme2) {
                throw new RuntimeException("internal error. " + ToNativeConverter.class + " has no toNative() method");
            }
        }
    }


    static Method getFromNativeMethod(FromNativeType fromNativeType, AsmClassLoader classLoader) {
        FromNativeConverter fromNativeConverter = fromNativeType.getFromNativeConverter();
        if (fromNativeConverter == null) {
            return null;
        }

        try {
            Class<? extends FromNativeConverter> fromNativeConverterClass = fromNativeConverter.getClass();
            if (Modifier.isPublic(fromNativeConverterClass.getModifiers())) {
                for (Method method : fromNativeConverterClass.getMethods()) {
                    if (!method.getName().equals("fromNative")) continue;
                    Class[] methodParameterTypes = method.getParameterTypes();
                    Class javaType = fromNativeType.getDeclaredType().isPrimitive()
                            ? boxedType(fromNativeType.getDeclaredType())
                            : fromNativeType.getDeclaredType();
                    if (javaType.isAssignableFrom(method.getReturnType())
                            && methodParameterTypes.length == 2
                            && methodParameterTypes[0].isAssignableFrom(fromNativeConverter.nativeType())
                            && methodParameterTypes[1] == FromNativeContext.class
                            && methodIsAccessible(method)
                            && classIsVisible(classLoader, method.getDeclaringClass())) {
                        return method;
                    }
                }
            }
            Method method = fromNativeConverterClass.getMethod("fromNative", Object.class, FromNativeContext.class);
            return methodIsAccessible(method) && classIsVisible(classLoader, method.getDeclaringClass())
                    ? method : FromNativeConverter.class.getDeclaredMethod("fromNative", Object.class, FromNativeContext.class);

        } catch (NoSuchMethodException nsme) {
            try {
                return FromNativeConverter.class.getDeclaredMethod("fromNative", Object.class, FromNativeContext.class);
            } catch (NoSuchMethodException nsme2) {
                throw new RuntimeException("internal error. " + FromNativeConverter.class + " has no fromNative() method");
            }
        }
    }

    static boolean methodIsAccessible(Method method) {
        return Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(method.getDeclaringClass().getModifiers());
    }

    private static boolean classIsVisible(ClassLoader classLoader, Class klass) {
        try {
            return classLoader.loadClass(klass.getName()) == klass;

        } catch (ClassNotFoundException cnfe) {
            return false;
        }
    }


}
