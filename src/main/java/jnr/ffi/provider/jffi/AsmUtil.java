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

import com.kenai.jffi.Type;
import jnr.ffi.*;
import com.kenai.jffi.Platform;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.*;

import jnr.ffi.annotations.Delegate;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;
import jnr.ffi.provider.ParameterFlags;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static jnr.ffi.provider.jffi.NumberUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;

final class AsmUtil {
    private AsmUtil() {}
    
    public static final MethodVisitor newTraceMethodVisitor(MethodVisitor mv) {
        try {
            Class<? extends MethodVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceMethodVisitor").asSubclass(MethodVisitor.class);
            Constructor<? extends MethodVisitor> c = tmvClass.getDeclaredConstructor(MethodVisitor.class);
            return c.newInstance(mv);
        } catch (Throwable t) {
            return mv;
        }
    }

    public static final ClassVisitor newTraceClassVisitor(ClassVisitor cv, OutputStream out) {
        return newTraceClassVisitor(cv, new PrintWriter(out, true));
    }

    public static final ClassVisitor newTraceClassVisitor(ClassVisitor cv, PrintWriter out) {
        try {

            Class<? extends ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class, PrintWriter.class);
            return c.newInstance(cv, out);
        } catch (Throwable t) {
            return cv;
        }
    }

    public static final ClassVisitor newTraceClassVisitor(PrintWriter out) {
        try {

            Class<? extends ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(PrintWriter.class);
            return c.newInstance(out);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static final ClassVisitor newCheckClassAdapter(ClassVisitor cv) {
        try {
            Class<? extends ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.CheckClassAdapter").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class);
            return c.newInstance(cv);
        } catch (Throwable t) {
            return cv;
        }
    }

    public static final Class unboxedReturnType(Class type) {
        if (Pointer.class.isAssignableFrom(type)
            || Struct.class.isAssignableFrom(type)
            || String.class.isAssignableFrom(type)) {
            return Platform.getPlatform().addressSize() == 32 ? int.class : long.class;
        }
        
        return unboxedType(type);
    }

    public static final Class unboxedParameterType(Class type) {
        if (Buffer.class.isAssignableFrom(type)) {
            return Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else {
            return unboxedType(type);
        }
    }

    public static final Class unboxedType(Class boxedType) {
        if (boxedType == Byte.class) {
            return byte.class;

        } else if (boxedType == Short.class) {
            return short.class;

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

        } else if (boxedType == NativeLong.class) {
            return Platform.getPlatform().longSize() == 32 ? int.class : long.class;

        } else if (Pointer.class.isAssignableFrom(boxedType) || Struct.class.isAssignableFrom(boxedType)) {
            return Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else if (String.class == boxedType) {
            return Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else if (Enum.class.isAssignableFrom(boxedType)) {
            return int.class;
        
        } else {
            return boxedType;
        }
    }

    public static final Class boxedType(Class type) {
        if (type == byte.class) {
            return Byte.class;
        } else if (type == short.class) {
            return Short.class;
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

    
    static final void emitReturnOp(SkinnyMethodAdapter mv, Class returnType) {
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
    static final int calculateLocalVariableSpace(Class type) {
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
    static final int calculateLocalVariableSpace(Class... types) {
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

    private static final void unboxPointerOrStruct(final SkinnyMethodAdapter mv, final Class type, final Class nativeType) {
        mv.invokestatic(p(AsmRuntime.class), long.class == nativeType ? "longValue" : "intValue",
                sig(nativeType, type));
    }

    static final void unboxPointer(final SkinnyMethodAdapter mv, final Class nativeType) {
        unboxPointerOrStruct(mv, Pointer.class, nativeType);
    }

    static final void unboxStruct(final SkinnyMethodAdapter mv, final Class nativeType) {
        unboxPointerOrStruct(mv, Struct.class, nativeType);
    }

    static final void unboxBuffer(final SkinnyMethodAdapter mv, final Class type, final Class nativeType) {
        mv.invokestatic(p(AsmRuntime.class), "longValue", sig(long.class, type));
        narrow(mv, long.class, nativeType);
    }

    static final void unboxEnum(final SkinnyMethodAdapter mv, final Class nativeType) {
        mv.invokestatic(p(AsmRuntime.class), long.class == nativeType ? "longValue" : "intValue",
                sig(nativeType, Enum.class));
    }

    static final void unboxBoolean(final SkinnyMethodAdapter mv, Class boxedType, final Class nativeType) {
        mv.invokevirtual(p(boxedType), "booleanValue", "()Z");
        widen(mv, boolean.class, nativeType);
    }

    static final void unboxBoolean(final SkinnyMethodAdapter mv, final Class nativeType) {
        unboxBoolean(mv, Boolean.class, nativeType);
    }

    static void unboxNumber(final SkinnyMethodAdapter mv, final Class boxedType, final Class unboxedType,
                                  final jnr.ffi.NativeType nativeType) {

        if (Number.class.isAssignableFrom(boxedType)) {

            switch (nativeType) {
                case SCHAR:
                case UCHAR:
                    mv.invokevirtual(p(boxedType), "byteValue", "()B");
                    if (nativeType == NativeType.UCHAR) {
                        mv.pushInt(0xff);
                        mv.iand();
                    }
                    widen(mv, int.class, unboxedType);
                    break;

                case SSHORT:
                case USHORT:
                    mv.invokevirtual(p(boxedType), "shortValue", "()S");
                    if (nativeType == NativeType.USHORT) {
                        mv.pushInt(0xffff);
                        mv.iand();
                    }
                    widen(mv, int.class, unboxedType);
                    narrow(mv, int.class, unboxedType);
                    break;

                case SINT:
                case UINT:
                    mv.invokevirtual(p(boxedType), "intValue", "()I");
                    widen(mv, int.class, unboxedType);
                    if (long.class == unboxedType && nativeType == NativeType.UINT) {
                        mv.pushInt(0xffffffff);
                        mv.i2l();
                        mv.land();
                    } else {
                        narrow(mv, int.class, unboxedType);
                    }

                    break;

                case SLONG:
                case ULONG:
                    if (sizeof(nativeType) == 4) {
                        mv.invokevirtual(p(boxedType), "intValue", "()I");
                        widen(mv, int.class, unboxedType);
                        narrow(mv, int.class, unboxedType);
                    } else {
                        mv.invokevirtual(p(boxedType), "longValue", "()J");
                        narrow(mv, int.class, unboxedType);
                    }
                    break;

                case SLONGLONG:
                case ULONGLONG:
                    mv.invokevirtual(p(boxedType), "longValue", "()J");
                    narrow(mv, int.class, unboxedType);
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

        } else if (Enum.class.isAssignableFrom(boxedType)) {
            unboxEnum(mv, unboxedType);

        } else {
            throw new IllegalArgumentException("unsupported boxed type: " + boxedType);
        }
    }


    static final void unboxNumber(final SkinnyMethodAdapter mv, final Class boxedType, final Class nativeType) {

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

        } else if (Enum.class.isAssignableFrom(boxedType)) {
            unboxEnum(mv, nativeType);

        } else {
            throw new IllegalArgumentException("unsupported boxed type: " + boxedType);
        }
    }

    static final void boxValue(SkinnyMethodAdapter mv, Class returnType, Class nativeReturnType) {
        if (returnType == nativeReturnType) {
            return;

        } else if (Boolean.class.isAssignableFrom(returnType)) {
            narrow(mv, nativeReturnType, boolean.class);
            mv.invokestatic(Boolean.class, "valueOf", Boolean.class, boolean.class);

        } else if (Pointer.class.isAssignableFrom(returnType)) {
            mv.invokestatic(AsmRuntime.class, "pointerValue", Pointer.class, nativeReturnType);

        } else if (Address.class == returnType) {
            mv.invokestatic(returnType, "valueOf", returnType, nativeReturnType);

        } else if (Struct.class.isAssignableFrom(returnType)) {
            boxStruct(mv, returnType, nativeReturnType);

        } else if (Number.class.isAssignableFrom(returnType)) {
            boxNumber(mv, returnType, nativeReturnType);

        } else if (String.class == returnType) {
            mv.invokestatic(AsmRuntime.class, "stringValue", String.class, nativeReturnType);

        } else {
            throw new IllegalArgumentException("cannot box value of type " + nativeReturnType + " to " + returnType);
        }
    }

    static final void boxValue(SkinnyMethodAdapter mv, Class returnType, Class nativeReturnType, NativeType nativeType) {
        if (returnType == nativeReturnType) {
            return;

        } else if (Boolean.class.isAssignableFrom(returnType)) {
            narrow(mv, nativeReturnType, boolean.class);
            mv.invokestatic(Boolean.class, "valueOf", Boolean.class, boolean.class);

        } else if (Pointer.class.isAssignableFrom(returnType)) {
            mv.invokestatic(AsmRuntime.class, "pointerValue", Pointer.class, nativeReturnType);

        } else if (Address.class == returnType) {
            mv.invokestatic(returnType, "valueOf", returnType, nativeReturnType);

        } else if (Struct.class.isAssignableFrom(returnType)) {
            boxStruct(mv, returnType, nativeReturnType);

        } else if (Number.class.isAssignableFrom(returnType)) {
            boxNumber(mv, returnType, nativeReturnType, nativeType);

        } else if (String.class == returnType) {
            mv.invokestatic(AsmRuntime.class, "stringValue", String.class, nativeReturnType);

        } else {
            throw new IllegalArgumentException("cannot box value of type " + nativeReturnType + " to " + returnType);
        }
    }


    static final void boxStruct(SkinnyMethodAdapter mv, Class structClass, Class nativeType) {
        Label nonnull = new Label();
        Label end = new Label();

        if (long.class == nativeType) {
            mv.dup2();
            mv.lconst_0();
            mv.lcmp();
            mv.ifne(nonnull);
            mv.pop2();

        } else {
            mv.dup();
            mv.ifne(nonnull);
            mv.pop();
        }

        mv.aconst_null();
        mv.go_to(end);

        mv.label(nonnull);

        // Create an instance of the struct subclass
        mv.newobj(p(structClass));
        mv.dup();
        try {
            Constructor<? extends Struct> constructor = structClass.asSubclass(Struct.class).getConstructor(jnr.ffi.Runtime.class);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("struct subclass " + structClass.getName() + " has no constructor that takes a "
                + jnr.ffi.Runtime.class.getName(), ex);
        }
        mv.invokestatic(p(NativeRuntime.class), "getInstance", sig(NativeRuntime.class));
        mv.invokespecial(structClass, "<init>", void.class, jnr.ffi.Runtime.class);
        if (long.class == nativeType) {
            mv.dup_x2();
        } else {
            mv.dup_x1();
        }

        // associate the memory with the struct and return the struct
        mv.invokestatic(AsmRuntime.class, "useMemory", void.class, nativeType, Struct.class);
        mv.label(end);
    }

    static final void boxNumber(SkinnyMethodAdapter mv, Class type, Class nativeType) {
        Class primitiveClass = getPrimitiveClass(type);

        // Emit widening/narrowing ops if necessary
        widen(mv, nativeType, primitiveClass);
        narrow(mv, nativeType, primitiveClass);

        mv.invokestatic(type, "valueOf", type, primitiveClass);
    }

    static final void boxNumber(SkinnyMethodAdapter mv, Class type, Class nativeType, Type jffiType) {
        Class primitiveClass = getPrimitiveClass(type);

        // Emit widening/narrowing ops if necessary
        convertPrimitive(mv, nativeType, primitiveClass, jffiType);
        mv.invokestatic(type, "valueOf", type, primitiveClass);
    }

    static final void boxNumber(SkinnyMethodAdapter mv, Class type, Class nativePrimitiveType, NativeType nativeType) {
        Class primitiveClass = getPrimitiveClass(type);

        // Emit widening/narrowing ops if necessary
        convertPrimitive(mv, nativePrimitiveType, primitiveClass, nativeType);
        mv.invokestatic(type, "valueOf", type, primitiveClass);
    }

    static boolean isDelegate(Class klass) {
        for (Method m : klass.getMethods()) {
            if (m.isAnnotationPresent(Delegate.class)) {
                return true;
            }
        }

        return false;
    }

    static boolean isDelegate(SigType type) {
        return isDelegate(type.getDeclaredType());
    }


    static final Type getNativeParameterType(Method method, int paramIndex, TypeMapper mapper) {
        Class type = method.getParameterTypes()[paramIndex];
        ToNativeConverter converter = mapper.getToNativeConverter(type);

        return InvokerUtil.getNativeParameterType(converter != null ? converter.nativeType() : type,
                method.getParameterAnnotations()[paramIndex]);
    }

    static final int getParameterFlags(Method method, int paramIndex) {
        return getParameterFlags(method.getParameterAnnotations()[paramIndex]);
    }

    static final int getParameterFlags(Annotation[] annotations) {
        return ParameterFlags.parse(annotations);
    }

    static final int getNativeArrayFlags(int flags) {
        int nflags = 0;
        nflags |= ParameterFlags.isIn(flags) ? com.kenai.jffi.ArrayFlags.IN : 0;
        nflags |= ParameterFlags.isOut(flags) ? com.kenai.jffi.ArrayFlags.OUT : 0;
        nflags |= (ParameterFlags.isNulTerminate(flags) || ParameterFlags.isIn(flags))
                ? com.kenai.jffi.ArrayFlags.NULTERMINATE : 0;
        return nflags;
    }

    static final int getNativeArrayFlags(Annotation[] annotations) {
        return getNativeArrayFlags(getParameterFlags(annotations));
    }

    static Class getNativeClass(NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
                return byte.class;

            case SSHORT:
            case USHORT:
                return short.class;

            case SINT:
            case UINT:
                return int.class;

            case SLONG:
            case ULONG:
                return sizeof(nativeType) == 4 ? int.class : long.class;

            case SLONGLONG:
            case ULONGLONG:
                return long.class;

            case FLOAT:
                return float.class;

            case DOUBLE:
                return double.class;

            case ADDRESS:
                return sizeof(nativeType) == 4 ? int.class : long.class;

            case VOID:
                return void.class;

            default:
                throw new IllegalArgumentException("unsupported native type: " + nativeType);
        }
    }

    static LocalVariable[] getParameterVariables(ParameterType[] parameterTypes) {
        LocalVariable[] lvars = new LocalVariable[parameterTypes.length];
        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            lvars[i] = new LocalVariable(lvar);
            lvar += calculateLocalVariableSpace(parameterTypes[i]);
        }

        return lvars;
    }

    static void loadParameter(SkinnyMethodAdapter mv, Class parameterType, LocalVariable parameter) {
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

    static final Label emitDirectCheck(SkinnyMethodAdapter mv, Class[] parameterTypes) {

        // Iterate through any parameters that might require a HeapInvocationBuffer
        Label bufferInvocationLabel = new Label();
        boolean needBufferInvocation = false;
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);
                mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, Pointer.class);
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;

            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);
                mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, Struct.class);
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;

            } else if (Buffer.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);


                if (Platform.getPlatform().getJavaMajorVersion() < 6 && Buffer.class == parameterTypes[i]) {
                    // Buffer#isDirect() is only available in java 6 or later, so need to use a slower test
                    // that checks / casts to the appropriate buffer subclass
                    mv.invokestatic(AsmRuntime.class, "isDirect5", boolean.class, Buffer.class);

                } else {
                    mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, parameterTypes[i]);
                }
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;

            } else {
                lvar += calculateLocalVariableSpace(parameterTypes[i]);
            }
        }

        return needBufferInvocation ? bufferInvocationLabel : null;
    }

    static final Label emitDirectCheck(SkinnyMethodAdapter mv, ParameterType[] parameterTypes) {

        // Iterate through any parameters that might require a HeapInvocationBuffer
        Label bufferInvocationLabel = new Label();
        boolean needBufferInvocation = false;
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            Class javaType = parameterTypes[i].getDeclaredType();
            if (Pointer.class.isAssignableFrom(javaType)) {
                mv.aload(lvar++);
                mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, Pointer.class);
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;

            } else if (Struct.class.isAssignableFrom(javaType)) {
                mv.aload(lvar++);
                mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, Struct.class);
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;

            } else if (Buffer.class.isAssignableFrom(javaType)) {
                mv.aload(lvar++);


                if (Platform.getPlatform().getJavaMajorVersion() < 6 && Buffer.class == javaType) {
                    // Buffer#isDirect() is only available in java 6 or later, so need to use a slower test
                    // that checks / casts to the appropriate buffer subclass
                    mv.invokestatic(AsmRuntime.class, "isDirect5", boolean.class, Buffer.class);

                } else {
                    mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, javaType);
                }
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;

            } else {
                lvar += calculateLocalVariableSpace(parameterTypes[i]);
            }
        }

        return needBufferInvocation ? bufferInvocationLabel : null;
    }

    static void emitReturn(SkinnyMethodAdapter mv, Class returnType, Class nativeIntType, NativeType nativeType) {
        if (returnType.isPrimitive()) {
            convertPrimitive(mv, nativeIntType, returnType, nativeType);

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
            boxValue(mv, returnType, nativeIntType, nativeType);
            mv.areturn();
        }
    }
}
