package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;
import jnr.ffi.Address;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.byref.ByReference;
import jnr.ffi.provider.InvocationSession;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.struct.Struct;

import java.lang.annotation.Annotation;
import java.nio.Buffer;

import static jnr.ffi.provider.jffi.AsmUtil.calculateLocalVariableSpace;
import static jnr.ffi.provider.jffi.AsmUtil.unboxNumber;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.ffi.provider.jffi.NumberUtil.isLong32;
import static jnr.ffi.provider.jffi.NumberUtil.isLong64;
import static jnr.ffi.provider.jffi.NumberUtil.isPrimitiveInt;

/**
 *
 */
final class BufferMethodGenerator extends BaseMethodGenerator {

    BufferMethodGenerator(AsmLibraryLoader loader) {
        super(loader);
    }

    public void generate(SkinnyMethodAdapter mv,
                         Class returnType, Annotation[] resultAnnotations,
                         Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreError) {
        generateBufferInvocation(mv, returnType, resultAnnotations, parameterTypes, parameterAnnotations);
    }

    public boolean isSupported(Class returnType, Annotation[] resultAnnotations,
                         Class[] parameterTypes, Annotation[][] parameterAnnotations,
                         CallingConvention convention) {
        // Buffer invocation supports everything
        return true;
    }

    static final void emitInvocationBufferNumericParameter(final SkinnyMethodAdapter mv,
            final Class parameterType, final Annotation[] parameterAnnotations) {
        String paramMethod = null;
        Class nativeParamType = int.class;

        if (byte.class == parameterType || Byte.class == parameterType) {
            paramMethod = "putByte";

        } else if (short.class == parameterType || Short.class == parameterType) {
            paramMethod = "putShort";

        } else if (int.class == parameterType || Integer.class == parameterType
                || boolean.class == parameterType || Boolean.class == parameterType) {
            paramMethod = "putInt";

        } else if (isLong32(parameterType, parameterAnnotations)) {
            paramMethod = "putInt";
            nativeParamType = int.class;

        } else if (isLong64(parameterType, parameterAnnotations)) {
            paramMethod = "putLong";
            nativeParamType = long.class;

        } else if (float.class == parameterType || Float.class == parameterType) {
            paramMethod = "putFloat";
            nativeParamType = float.class;

        } else if (double.class == parameterType || Double.class == parameterType) {
            paramMethod = "putDouble";
            nativeParamType = double.class;

        } else {
            throw new IllegalArgumentException("unsupported parameter type " + parameterType);
        }

        if (!parameterType.isPrimitive()) {
            unboxNumber(mv, parameterType, nativeParamType);
        }


        mv.invokevirtual(HeapInvocationBuffer.class, paramMethod, void.class, nativeParamType);
    }

    static boolean isSessionRequired(Class parameterType, Annotation[] annotations) {
        return StringBuilder.class.isAssignableFrom(parameterType)
                || StringBuffer.class.isAssignableFrom(parameterType)
                || ByReference.class.isAssignableFrom(parameterType)
                || (parameterType.isArray() && Pointer.class.isAssignableFrom(parameterType.getComponentType()))
                || (parameterType.isArray() && CharSequence.class.isAssignableFrom(parameterType.getComponentType()))
                || (parameterType.isArray() && NativeLong.class.isAssignableFrom(parameterType.getComponentType()))
                || (parameterType.isArray() && isLong32(parameterType.getComponentType(), annotations))
                ;
    }

    static boolean isSessionRequired(Class[] parameterTypes, Annotation[][] annotations) {
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (isSessionRequired(parameterTypes[i], annotations[i])) {
                return true;
            }
        }

        return false;
    }

    static final void marshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(AsmRuntime.class), "marshal", sig(void.class, ci(InvocationBuffer.class), parameterTypes));
    }

    static final void sessionmarshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(AsmRuntime.class), "marshal",
                sig(void.class, ci(InvocationBuffer.class) + ci(InvocationSession.class), parameterTypes));
    }

    static final void generateBufferInvocation(SkinnyMethodAdapter mv,
                                               Class returnType, Annotation[] resultAnnotations,
                                               Class[] parameterTypes, Annotation[][] parameterAnnotations) {
        // [ stack contains: Invoker, Function ]
        final boolean sessionRequired = isSessionRequired(parameterTypes, parameterAnnotations);
        final int lvarSession = sessionRequired ? calculateLocalVariableSpace(parameterTypes) + 1 : -1;
        if (sessionRequired) {
            mv.newobj(p(InvocationSession.class));
            mv.dup();
            mv.invokespecial(InvocationSession.class, "<init>", void.class);
            mv.astore(lvarSession);
        }

        // [ stack contains: Invoker, Function, Function ]
        mv.dup();
        mv.invokestatic(AsmRuntime.class, "newHeapInvocationBuffer", HeapInvocationBuffer.class, Function.class);
        // [ stack contains: Invoker, Function, HeapInvocationBuffer ]

        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            mv.dup(); // dup ref to HeapInvocationBuffer

            if (isSessionRequired(parameterTypes[i], parameterAnnotations[i])) {
                mv.aload(lvarSession);
            }

            lvar = AsmLibraryLoader.loadParameter(mv, parameterTypes[i], lvar);

            final int parameterFlags = DefaultInvokerFactory.getParameterFlags(parameterAnnotations[i]);
            final int nativeArrayFlags = DefaultInvokerFactory.getNativeArrayFlags(parameterFlags)
                        | ((parameterFlags & ParameterFlags.IN) != 0 ? ArrayFlags.NULTERMINATE : 0);

            if (parameterTypes[i].isArray() && parameterTypes[i].getComponentType().isPrimitive()) {
                mv.pushInt(nativeArrayFlags);

                if (isLong32(parameterTypes[i].getComponentType(), parameterAnnotations[i])) {
                    mv.invokestatic(p(AsmRuntime.class), "marshal32",
                        sig(void.class, ci(InvocationBuffer.class) + ci(InvocationSession.class), parameterTypes[i], int.class));
                } else {
                    marshal(mv, parameterTypes[i], int.class);
                }

            } else if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Pointer.class, int.class);

            } else if (Address.class.isAssignableFrom(parameterTypes[i])) {
                marshal(mv, Address.class);

            } else if (Enum.class.isAssignableFrom(parameterTypes[i])) {
                marshal(mv, Enum.class);

            } else if (Buffer.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, parameterTypes[i], int.class);

            } else if (ByReference.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(nativeArrayFlags);
                // stack should be: [ session, buffer, ref, flags ]
                sessionmarshal(mv, ByReference.class, int.class);

            } else if (StringBuilder.class.isAssignableFrom(parameterTypes[i]) || StringBuffer.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                // stack should be: [ session, buffer, ref, flags ]
                sessionmarshal(mv, parameterTypes[i], int.class, int.class);

            } else if (CharSequence.class.isAssignableFrom(parameterTypes[i])) {
                // stack should be: [ Buffer, array, flags ]
                marshal(mv, CharSequence.class);

            } else if (parameterTypes[i].isArray() && CharSequence.class.isAssignableFrom(parameterTypes[i].getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                sessionmarshal(mv, CharSequence[].class, int.class, int.class);

            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Struct.class, int.class, int.class);

            } else if (parameterTypes[i].isArray() && Struct.class.isAssignableFrom(parameterTypes[i].getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Struct[].class, int.class, int.class);

            } else if (parameterTypes[i].isArray() && Pointer.class.isAssignableFrom(parameterTypes[i].getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                sessionmarshal(mv, Pointer[].class, int.class, int.class);

            } else if (parameterTypes[i].isPrimitive() || Number.class.isAssignableFrom(parameterTypes[i])
                    || Boolean.class == parameterTypes[i]) {
                emitInvocationBufferNumericParameter(mv, parameterTypes[i], parameterAnnotations[i]);

            } else {
                throw new IllegalArgumentException("unsupported parameter type " + parameterTypes[i]);
            }
        }

        String invokeMethod = null;
        Class nativeReturnType = null;

        if (isPrimitiveInt(returnType) || void.class == returnType
                || Byte.class == returnType || Short.class == returnType || Integer.class == returnType) {
            invokeMethod = "invokeInt";
            nativeReturnType = int.class;

        } else if (isLong32(returnType, resultAnnotations)) {
            invokeMethod = "invokeInt";
            nativeReturnType = int.class;

        } else if (isLong64(returnType, resultAnnotations)) {
            invokeMethod = "invokeLong";
            nativeReturnType = long.class;

        } else if (Pointer.class == returnType || Address.class == returnType
            || Struct.class.isAssignableFrom(returnType) || String.class.isAssignableFrom(returnType)) {
            invokeMethod = Platform.getPlatform().addressSize() == 32 ? "invokeInt" : "invokeLong";
            nativeReturnType = Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else if (Float.class == returnType || float.class == returnType) {
            invokeMethod = "invokeFloat";
            nativeReturnType = float.class;

        } else if (Double.class == returnType || double.class == returnType) {
            invokeMethod = "invokeDouble";
            nativeReturnType = double.class;

        } else {
            throw new IllegalArgumentException("unsupported return type " + returnType);
        }

        mv.invokevirtual(Invoker.class, invokeMethod,
                nativeReturnType, Function.class, HeapInvocationBuffer.class);

        if (sessionRequired) {
            mv.aload(lvarSession);
            mv.invokevirtual(p(InvocationSession.class), "finish", "()V");
        }

        AsmLibraryLoader.emitReturn(mv, returnType, nativeReturnType);
    }
}
