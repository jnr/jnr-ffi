package jnr.ffi.provider.jffi;

import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Variable;
import jnr.ffi.mapper.*;
import jnr.ffi.util.EnumMapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.InvokerUtil.*;
import static jnr.ffi.provider.jffi.NumberUtil.narrow;
import static jnr.ffi.provider.jffi.NumberUtil.widen;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generate global variable accessors
 */
public class VariableAccessorGenerator {
    private final AtomicLong nextClassID = new AtomicLong(0);
    static final Map<NativeType, PointerOp> pointerOperations;

    public void generate(AsmBuilder builder, Class interfaceClass, String variableName, long address,
                         Class javaType, Annotation[] annotations,
                         TypeMapper typeMapper, NativeClosureManager closureManager) {
        System.out.println("generating accessor for " + variableName + " of type " + javaType);
        FromNativeConverter fromNativeConverter = getFromNativeConverter(javaType, annotations, typeMapper, closureManager, null);
        ToNativeConverter toNativeConverter = getToNativeConverter(javaType, annotations, typeMapper, closureManager, null);

        Variable variableAccessor = buildVariableAccessor(address, interfaceClass, javaType, annotations,
                toNativeConverter, fromNativeConverter);
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL,
                variableName, sig(Variable.class), null, null);
        mv.start();
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getVariableName(variableAccessor), ci(Variable.class));
        mv.areturn();
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    Variable buildVariableAccessor(long address, Class interfaceClass, Class javaType, Annotation[] annotations,
                                   ToNativeConverter toNativeConverter, FromNativeConverter fromNativeConverter) {
        boolean debug = AsmLibraryLoader.DEBUG && !hasAnnotation(annotations, NoTrace.class);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = debug ? AsmUtil.newCheckClassAdapter(cw) : cw;

        AsmBuilder builder = new AsmBuilder(p(interfaceClass) + "$VariableAccessor$$" + nextClassID.getAndIncrement(), cv);
        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, builder.getClassNamePath(), null, p(Object.class),
                new String[] { p(Variable.class) });
        cv.visitField(ACC_PRIVATE | ACC_FINAL, "pointer", ci(Pointer.class), null, null);
        cv.visitField(ACC_PRIVATE | ACC_FINAL, "toNativeConverter", ci(ToNativeConverter.class), null, null);
        cv.visitField(ACC_PRIVATE | ACC_FINAL, "fromNativeConverter", ci(FromNativeConverter.class), null, null);

        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>",
                sig(void.class, Pointer.class, ToNativeConverter.class, FromNativeConverter.class),
                null, null);
        init.start();
        init.aload(0);
        init.invokespecial(p(Object.class), "<init>", sig(void.class));
        init.aload(0);
        init.aload(1);
        init.putfield(builder.getClassNamePath(), "pointer", ci(Pointer.class));
        init.aload(0);
        init.aload(2);
        init.putfield(builder.getClassNamePath(), "toNativeConverter", ci(ToNativeConverter.class));
        init.aload(0);
        init.aload(3);
        init.putfield(builder.getClassNamePath(), "fromNativeConverter", ci(FromNativeConverter.class));
        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        SkinnyMethodAdapter set = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL, "set",
                sig(void.class, Object.class),
                null, null);
        Class boxedType = toNativeConverter != null ? toNativeConverter.nativeType() : javaType;
        Class primitiveType = unboxedType(boxedType);


        LocalVariableAllocator setVariableAllocator = new LocalVariableAllocator(1);

        set.start();
        set.aload(0);
        set.getfield(builder.getClassNamePath(), "pointer", ci(Pointer.class));
        set.lconst_0();

        if (toNativeConverter != null) {
            set.aload(0);
            set.getfield(builder.getClassNamePath(), "toNativeConverter", ci(ToNativeConverter.class));
            set.aload(1);
            set.aconst_null();
            set.invokeinterface(ToNativeConverter.class, "toNative", Object.class, Object.class, ToNativeContext.class);
            set.checkcast(toNativeConverter.nativeType());
        } else {
            set.aload(1);
            set.checkcast(javaType);
        }

        if (Number.class.isAssignableFrom(boxedType)) {
            unboxNumber(set, boxedType, primitiveType);

        } else if (Pointer.class.isAssignableFrom(boxedType)) {
            unboxPointer(set, primitiveType);

        } else {
            throw new IllegalArgumentException("global variable type not supported: " + javaType);
        }

        NativeType nativeType = getNativeType(NativeRuntime.getInstance(), boxedType, annotations);
        PointerOp op = pointerOperations.get(nativeType);
        if (op == null) {
            throw new IllegalArgumentException("global variable type not supported: " + javaType);
        }
        op.put(set, primitiveType);

        set.voidreturn();
        set.visitMaxs(10, 10 + setVariableAllocator.getSpaceUsed());
        set.visitEnd();

        SkinnyMethodAdapter get = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL, "get",
                sig(Object.class),
                null, null);

        LocalVariableAllocator getVariableAllocator = new LocalVariableAllocator(0);
        get.start();
        if (fromNativeConverter != null) {
            get.aload(0);
            get.getfield(builder.getClassNamePath(), "fromNativeConverter", ci(FromNativeConverter.class));
        }

        get.aload(0);
        get.getfield(builder.getClassNamePath(), "pointer", ci(Pointer.class));
        get.lconst_0();
        op.get(get, primitiveType);
        boxValue(get, boxedType, primitiveType);
        if (fromNativeConverter != null) {
            get.aconst_null();
            get.invokeinterface(FromNativeConverter.class, "fromNative", Object.class, Object.class, FromNativeContext.class);
            get.checkcast(javaType);
        }
        get.areturn();
        get.visitMaxs(10, 10 + getVariableAllocator.getSpaceUsed());
        get.visitEnd();
        cv.visitEnd();

        try {
            byte[] bytes = cw.toByteArray();
            if (debug) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }

            Class<Variable> implClass = new AsmClassLoader(interfaceClass.getClassLoader()).defineClass(builder.getClassNamePath().replace("/", "."), bytes);
            Constructor<Variable> cons = implClass.getDeclaredConstructor(Pointer.class, ToNativeConverter.class, FromNativeConverter.class);
            return cons.newInstance(NativeRuntime.getInstance().getMemoryManager().newPointer(address),
                    toNativeConverter, fromNativeConverter);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    static {
        Map<NativeType, PointerOp> ops = new HashMap<NativeType, PointerOp>();
        op(ops, NativeType.SCHAR, "Byte", byte.class);
        op(ops, NativeType.UCHAR, "Byte", byte.class);
        op(ops, NativeType.SSHORT, "Short", short.class);
        op(ops, NativeType.USHORT, "Short", short.class);
        op(ops, NativeType.SINT, "Int", int.class);
        op(ops, NativeType.UINT, "Int", int.class);
        op(ops, NativeType.SLONG, "Long", long.class);
        op(ops, NativeType.ULONG, "Long", long.class);
        op(ops, NativeType.SLONGLONG, "LongLong", long.class);
        op(ops, NativeType.ULONGLONG, "LongLong", long.class);
        op(ops, NativeType.FLOAT, "Float", float.class);
        op(ops, NativeType.DOUBLE, "Double", double.class);
        op(ops, NativeType.ADDRESS, "Address", long.class);

        pointerOperations = Collections.unmodifiableMap(ops);
    }

    private static void op(Map<NativeType, PointerOp> ops, NativeType type, String name, Class nativeIntType) {
        ops.put(type, new PointerOp(type, name, nativeIntType));
    }

    private static final class PointerOp {
        private final NativeType nativeType;
        private final String getMethodName;
        private final String putMethodName;
        private final Class nativeIntType;

        private PointerOp(NativeType nativeType, String name, Class nativeIntType) {
            this.nativeType = nativeType;
            this.getMethodName = "get" + name;
            this.putMethodName = "put" + name;
            this.nativeIntType = nativeIntType;
        }

        void put(SkinnyMethodAdapter mv, Class javaType) {
            widen(mv, javaType, nativeIntType, nativeType);
            narrow(mv, javaType, nativeIntType);
            mv.invokevirtual(Pointer.class, putMethodName, void.class, long.class, nativeIntType);
        }

        void get(SkinnyMethodAdapter mv, Class javaType) {
            mv.invokevirtual(Pointer.class, getMethodName, nativeIntType, long.class);
            widen(mv, nativeIntType, javaType);
            narrow(mv, nativeIntType, javaType);
        }
    }
}
