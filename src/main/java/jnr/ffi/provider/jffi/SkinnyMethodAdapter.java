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

import org.objectweb.asm.*;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import static jnr.ffi.provider.jffi.CodegenUtils.*;

/**
 *
 * @author headius
 */
public class SkinnyMethodAdapter extends MethodVisitor implements Opcodes {
    private final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private MethodVisitor method;
    
    /** 
     * Creates a new instance of SkinnyMethodAdapter.
     *
     * @param cv The class visitor instance.
     * @param flags The flags.
     * @param name The name.
     * @param signature The signature.
     * @param something Something.
     * @param exceptions The array of exceptions.
     */
    public SkinnyMethodAdapter(ClassVisitor cv, int flags, String name, String signature, String something, String[] exceptions) {
        super(Opcodes.ASM4);
        setMethodVisitor(cv.visitMethod(flags, name, signature, something, exceptions));
    }
    
    public MethodVisitor getMethodVisitor() {
        return method;
    }
    
    public void setMethodVisitor(MethodVisitor mv) {
        this.method = DEBUG ? AsmUtil.newTraceMethodVisitor(mv) : mv;
    }
    
    public void aload(int arg0) {
        getMethodVisitor().visitVarInsn(ALOAD, arg0);
    }

    public void aload(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(ALOAD, arg0.idx);
    }

    public void aload(int... args) {
        for (int arg : args) {
            getMethodVisitor().visitVarInsn(ALOAD, arg);
        }
    }

    public void aload(LocalVariable... args) {
        for (LocalVariable arg : args) {
            getMethodVisitor().visitVarInsn(ALOAD, arg.idx);
        }
    }
    
    public void iload(int arg0) {
        getMethodVisitor().visitVarInsn(ILOAD, arg0);
    }

    public void iload(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(ILOAD, arg0.idx);
    }

    public void iload(int... args) {
        for (int arg : args) {
            getMethodVisitor().visitVarInsn(ILOAD, arg);
        }
    }

    public void iload(LocalVariable... args) {
        for (LocalVariable arg : args) {
            getMethodVisitor().visitVarInsn(ILOAD, arg.idx);
        }
    }
    
    public void lload(int arg0) {
        getMethodVisitor().visitVarInsn(LLOAD, arg0);
    }

    public void lload(int... args) {
        for (int arg : args) {
            getMethodVisitor().visitVarInsn(LLOAD, arg);
        }
    }

    public void lload(LocalVariable... args) {
        for (LocalVariable arg : args) {
            getMethodVisitor().visitVarInsn(LLOAD, arg.idx);
        }
    }
    
    public void fload(int arg0) {
        getMethodVisitor().visitVarInsn(FLOAD, arg0);
    }

    public void fload(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(FLOAD, arg0.idx);
    }

    public void fload(int... args) {
        for (int arg : args) {
            getMethodVisitor().visitVarInsn(FLOAD, arg);
        }
    }
    
    public void dload(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(DLOAD, arg0.idx);
    }

    public void dload(int arg0) {
        getMethodVisitor().visitVarInsn(DLOAD, arg0);
    }

    public void dload(int... args) {
        for (int arg : args) {
            getMethodVisitor().visitVarInsn(DLOAD, arg);
        }
    }
    
    public void astore(int arg0) {
        getMethodVisitor().visitVarInsn(ASTORE, arg0);
    }

    public void astore(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(ASTORE, arg0.idx);
    }
    
    public void istore(int arg0) {
        getMethodVisitor().visitVarInsn(ISTORE, arg0);
    }

    public void istore(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(ISTORE, arg0.idx);
    }
    
    public void lstore(int arg0) {
        getMethodVisitor().visitVarInsn(LSTORE, arg0);
    }

    public void lstore(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(LSTORE, arg0.idx);
    }
    
    public void fstore(int arg0) {
        getMethodVisitor().visitVarInsn(FSTORE, arg0);
    }

    public void fstore(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(FSTORE, arg0.idx);
    }
    
    public void dstore(int arg0) {
        getMethodVisitor().visitVarInsn(DSTORE, arg0);
    }

    public void dstore(LocalVariable arg0) {
        getMethodVisitor().visitVarInsn(DSTORE, arg0.idx);
    }
    
    public void ldc(Object arg0) {
        getMethodVisitor().visitLdcInsn(arg0);
    }
    
    public void bipush(int arg) {
        getMethodVisitor().visitIntInsn(BIPUSH, arg);
    }
    
    public void sipush(int arg) {
        getMethodVisitor().visitIntInsn(SIPUSH, arg);
    }
        
    public void pushInt(int value) {
        if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
            switch (value) {
            case -1:
                iconst_m1();
                break;
            case 0:
                iconst_0();
                break;
            case 1:
                iconst_1();
                break;
            case 2:
                iconst_2();
                break;
            case 3:
                iconst_3();
                break;
            case 4:
                iconst_4();
                break;
            case 5:
                iconst_5();
                break;
            default:
                bipush(value);
                break;
            }
        } else if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
            sipush(value);
        } else {
            ldc(value);
        }
    }
        
    public void pushBoolean(boolean bool) {
        if (bool) iconst_1(); else iconst_0();
    }
    
    public void invokestatic(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitMethodInsn(INVOKESTATIC, arg1, arg2, arg3);
    }

    public void invokestatic(Class recv, String methodName, Class returnType, Class... parameterTypes) {
        getMethodVisitor().visitMethodInsn(INVOKESTATIC, p(recv), methodName, sig(returnType, parameterTypes));
    }
    
    public void invokespecial(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitMethodInsn(INVOKESPECIAL, arg1, arg2, arg3);
    }

    public void invokespecial(Class recv, String methodName, Class returnType, Class... parameterTypes) {
        getMethodVisitor().visitMethodInsn(INVOKESPECIAL, p(recv), methodName, sig(returnType, parameterTypes));
    }
    
    public void invokevirtual(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitMethodInsn(INVOKEVIRTUAL, arg1, arg2, arg3);
    }

    public void invokevirtual(Class recv, String methodName, Class returnType, Class... parameterTypes) {
        getMethodVisitor().visitMethodInsn(INVOKEVIRTUAL, p(recv), methodName, sig(returnType, parameterTypes));
    }
    
    public void invokeinterface(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitMethodInsn(INVOKEINTERFACE, arg1, arg2, arg3);
    }

    public void invokeinterface(Class recv, String methodName, Class returnType, Class... parameterTypes) {
        getMethodVisitor().visitMethodInsn(INVOKEINTERFACE, p(recv), methodName, sig(returnType, parameterTypes));
    }

    public void invokedynamic(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitMethodInsn(INVOKEDYNAMIC, arg1, arg2, arg3);
    }

    public void aprintln() {
        dup();
        getstatic(p(System.class), "out", ci(PrintStream.class));
        swap();
        invokevirtual(p(PrintStream.class), "println", sig(void.class, params(Object.class)));
    }
    
    public void areturn() {
        getMethodVisitor().visitInsn(ARETURN);
    }
    
    public void ireturn() {
        getMethodVisitor().visitInsn(IRETURN);
    }
    
    public void freturn() {
        getMethodVisitor().visitInsn(FRETURN);
    }
    
    public void lreturn() {
        getMethodVisitor().visitInsn(LRETURN);
    }
    
    public void dreturn() {
        getMethodVisitor().visitInsn(DRETURN);
    }
    
    public void newobj(String arg0) {
        getMethodVisitor().visitTypeInsn(NEW, arg0);
    }
    
    public void dup() {
        getMethodVisitor().visitInsn(DUP);
    }
    
    public void swap() {
        getMethodVisitor().visitInsn(SWAP);
    }
    
    public void swap2() {
        dup2_x2();
        pop2();
    }
    
    public void getstatic(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitFieldInsn(GETSTATIC, arg1, arg2, arg3);
    }
    
    public void putstatic(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitFieldInsn(PUTSTATIC, arg1, arg2, arg3);
    }
    
    public void getfield(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitFieldInsn(GETFIELD, arg1, arg2, arg3);
    }
    
    public void putfield(String arg1, String arg2, String arg3) {
        getMethodVisitor().visitFieldInsn(PUTFIELD, arg1, arg2, arg3);
    }
    
    public void voidreturn() {
        getMethodVisitor().visitInsn(RETURN);
    }
    
    public void anewarray(String arg0) {
        getMethodVisitor().visitTypeInsn(ANEWARRAY, arg0);
    }
    
    public void multianewarray(String arg0, int dims) {
        getMethodVisitor().visitMultiANewArrayInsn(arg0, dims);
    }
    
    public void newarray(int arg0) {
        getMethodVisitor().visitIntInsn(NEWARRAY, arg0);
    }
    
    public void iconst_m1() {
        getMethodVisitor().visitInsn(ICONST_M1);
    }
    
    public void iconst_0() {
        getMethodVisitor().visitInsn(ICONST_0);
    }
    
    public void iconst_1() {
        getMethodVisitor().visitInsn(ICONST_1);
    }
    
    public void iconst_2() {
        getMethodVisitor().visitInsn(ICONST_2);
    }
    
    public void iconst_3() {
        getMethodVisitor().visitInsn(ICONST_3);
    }
    
    public void iconst_4() {
        getMethodVisitor().visitInsn(ICONST_4);
    }
    
    public void iconst_5() {
        getMethodVisitor().visitInsn(ICONST_5);
    }
    
    public void lconst_0() {
        getMethodVisitor().visitInsn(LCONST_0);
    }
    
    public void aconst_null() {
        getMethodVisitor().visitInsn(ACONST_NULL);
    }
    
    public void label(Label label) {
        getMethodVisitor().visitLabel(label);
    }
    
    public void nop() {
        getMethodVisitor().visitInsn(NOP);
    }
    
    public void pop() {
        getMethodVisitor().visitInsn(POP);
    }
    
    public void pop2() {
        getMethodVisitor().visitInsn(POP2);
    }
    
    public void arrayload() {
        getMethodVisitor().visitInsn(AALOAD);
    }
    
    public void arraystore() {
        getMethodVisitor().visitInsn(AASTORE);
    }
    
    public void iarrayload() {
        getMethodVisitor().visitInsn(IALOAD);
    }
    
    public void barrayload() {
        getMethodVisitor().visitInsn(BALOAD);
    }
    
    public void barraystore() {
        getMethodVisitor().visitInsn(BASTORE);
    }
    
    public void aaload() {
        getMethodVisitor().visitInsn(AALOAD);
    }
    
    public void aastore() {
        getMethodVisitor().visitInsn(AASTORE);
    }
    
    public void iaload() {
        getMethodVisitor().visitInsn(IALOAD);
    }
    
    public void iastore() {
        getMethodVisitor().visitInsn(IASTORE);
    }
    
    public void laload() {
        getMethodVisitor().visitInsn(LALOAD);
    }
    
    public void lastore() {
        getMethodVisitor().visitInsn(LASTORE);
    }
    
    public void baload() {
        getMethodVisitor().visitInsn(BALOAD);
    }
    
    public void bastore() {
        getMethodVisitor().visitInsn(BASTORE);
    }
    
    public void saload() {
        getMethodVisitor().visitInsn(SALOAD);
    }
    
    public void sastore() {
        getMethodVisitor().visitInsn(SASTORE);
    }
    
    public void caload() {
        getMethodVisitor().visitInsn(CALOAD);
    }
    
    public void castore() {
        getMethodVisitor().visitInsn(CASTORE);
    }
    
    public void faload() {
        getMethodVisitor().visitInsn(FALOAD);
    }
    
    public void fastore() {
        getMethodVisitor().visitInsn(FASTORE);
    }
    
    public void daload() {
        getMethodVisitor().visitInsn(DALOAD);
    }
    
    public void dastore() {
        getMethodVisitor().visitInsn(DASTORE);
    }
    
    public void fcmpl() {
        getMethodVisitor().visitInsn(FCMPL);
    }
    
    public void fcmpg() {
        getMethodVisitor().visitInsn(FCMPG);
    }
    
    public void dcmpl() {
        getMethodVisitor().visitInsn(DCMPL);
    }
    
    public void dcmpg() {
        getMethodVisitor().visitInsn(DCMPG);
    }
    
    public void dup_x2() {
        getMethodVisitor().visitInsn(DUP_X2);
    }
    
    public void dup_x1() {
        getMethodVisitor().visitInsn(DUP_X1);
    }
    
    public void dup2_x2() {
        getMethodVisitor().visitInsn(DUP2_X2);
    }
    
    public void dup2_x1() {
        getMethodVisitor().visitInsn(DUP2_X1);
    }
    
    public void dup2() {
        getMethodVisitor().visitInsn(DUP2);
    }
    
    public void trycatch(Label arg0, Label arg1, Label arg2,
                                   String arg3) {
        getMethodVisitor().visitTryCatchBlock(arg0, arg1, arg2, arg3);
    }
    
    public void trycatch(String type, Runnable body, Runnable catchBody) {
        Label before = new Label();
        Label after = new Label();
        Label catchStart = new Label();
        Label done = new Label();

        trycatch(before, after, catchStart, type);
        label(before);
        body.run();
        label(after);
        go_to(done);
        if (catchBody != null) {
            label(catchStart);
            catchBody.run();
        }
        label(done);
    }
    
    public void go_to(Label arg0) {
        getMethodVisitor().visitJumpInsn(GOTO, arg0);
    }
    
    public void lookupswitch(Label arg0, int[] arg1, Label[] arg2) {
        getMethodVisitor().visitLookupSwitchInsn(arg0, arg1, arg2);
    }
    
    public void athrow() {
        getMethodVisitor().visitInsn(ATHROW);
    }
    
    public void instance_of(String arg0) {
        getMethodVisitor().visitTypeInsn(INSTANCEOF, arg0);
    }
    
    public void ifeq(Label arg0) {
        getMethodVisitor().visitJumpInsn(IFEQ, arg0);
    }

    public void iffalse(Label arg0) {
        ifeq(arg0);
    }
    
    public void ifne(Label arg0) {
        getMethodVisitor().visitJumpInsn(IFNE, arg0);
    }

    public void iftrue(Label arg0) {
        ifne(arg0);
    }
    
    public void if_acmpne(Label arg0) {
        getMethodVisitor().visitJumpInsn(IF_ACMPNE, arg0);
    }
    
    public void if_acmpeq(Label arg0) {
        getMethodVisitor().visitJumpInsn(IF_ACMPEQ, arg0);
    }
    
    public void if_icmple(Label arg0) {
        getMethodVisitor().visitJumpInsn(IF_ICMPLE, arg0);
    }
    
    public void if_icmpgt(Label arg0) {
        getMethodVisitor().visitJumpInsn(IF_ICMPGT, arg0);
    }

    public void if_icmpge(Label arg0) {
        getMethodVisitor().visitJumpInsn(IF_ICMPGE, arg0);
    }

    public void if_icmplt(Label arg0) {
        getMethodVisitor().visitJumpInsn(IF_ICMPLT, arg0);
    }
    
    public void if_icmpne(Label arg0) {
        getMethodVisitor().visitJumpInsn(IF_ICMPNE, arg0);
    }
    
    public void if_icmpeq(Label arg0) {
        getMethodVisitor().visitJumpInsn(IF_ICMPEQ, arg0);
    }
    
    public void checkcast(String arg0) {
        getMethodVisitor().visitTypeInsn(CHECKCAST, arg0);
    }

    public void checkcast(Class clazz) {
        getMethodVisitor().visitTypeInsn(CHECKCAST, p(clazz));
    }
    
    public void start() {
        getMethodVisitor().visitCode();
    }
    private void dump() {
        PrintWriter pw = new PrintWriter(System.out);

        Class tmvClass = getMethodVisitor().getClass();
        try {
            Method print = tmvClass.getDeclaredMethod("print", PrintWriter.class);

            pw.write("*** Dumping ***\n");

            print.invoke(getMethodVisitor(), pw);
        } catch (Throwable ex) {
        } finally {
            pw.flush();
        }
    }
    public void end() {
        if (DEBUG) {
            dump();
        }
        getMethodVisitor().visitMaxs(1, 1);
        getMethodVisitor().visitEnd();
    }

    public void line(int line) {
        Label label = new Label();
        label(label);
        visitLineNumber(line, label);
    }

    public void line(int line, Label label) {
        visitLineNumber(line, label);
    }
    
    public void ifnonnull(Label arg0) {
        getMethodVisitor().visitJumpInsn(IFNONNULL, arg0);
    }
    
    public void ifnull(Label arg0) {
        getMethodVisitor().visitJumpInsn(IFNULL, arg0);
    }
    
    public void iflt(Label arg0) {
        getMethodVisitor().visitJumpInsn(IFLT, arg0);
    }
    
    public void ifle(Label arg0) {
        getMethodVisitor().visitJumpInsn(IFLE, arg0);
    }
    
    public void ifgt(Label arg0) {
        getMethodVisitor().visitJumpInsn(IFGT, arg0);
    }
    
    public void ifge(Label arg0) {
        getMethodVisitor().visitJumpInsn(IFGE, arg0);
    }
    
    public void arraylength() {
        getMethodVisitor().visitInsn(ARRAYLENGTH);
    }
    
    public void ishr() {
        getMethodVisitor().visitInsn(ISHR);
    }
    
    public void ishl() {
        getMethodVisitor().visitInsn(ISHL);
    }
    
    public void iushr() {
        getMethodVisitor().visitInsn(IUSHR);
    }
    
    public void lshr() {
        getMethodVisitor().visitInsn(LSHR);
    }
    
    public void lshl() {
        getMethodVisitor().visitInsn(LSHL);
    }
    
    public void lushr() {
        getMethodVisitor().visitInsn(LUSHR);
    }
    
    public void lcmp() {
        getMethodVisitor().visitInsn(LCMP);
    }
    
    public void iand() {
        getMethodVisitor().visitInsn(IAND);
    }
    
    public void ior() {
        getMethodVisitor().visitInsn(IOR);
    }
    
    public void ixor() {
        getMethodVisitor().visitInsn(IXOR);
    }
    
    public void land() {
        getMethodVisitor().visitInsn(LAND);
    }
    
    public void lor() {
        getMethodVisitor().visitInsn(LOR);
    }
    
    public void lxor() {
        getMethodVisitor().visitInsn(LXOR);
    }
    
    public void iadd() {
        getMethodVisitor().visitInsn(IADD);
    }
    
    public void ladd() {
        getMethodVisitor().visitInsn(LADD);
    }
    
    public void fadd() {
        getMethodVisitor().visitInsn(FADD);
    }
    
    public void dadd() {
        getMethodVisitor().visitInsn(DADD);
    }
    
    public void isub() {
        getMethodVisitor().visitInsn(ISUB);
    }
    
    public void lsub() {
        getMethodVisitor().visitInsn(LSUB);
    }
    
    public void fsub() {
        getMethodVisitor().visitInsn(FSUB);
    }
    
    public void dsub() {
        getMethodVisitor().visitInsn(DSUB);
    }
    
    public void idiv() {
        getMethodVisitor().visitInsn(IDIV);
    }
    
    public void irem() {
        getMethodVisitor().visitInsn(IREM);
    }
    
    public void ineg() {
        getMethodVisitor().visitInsn(INEG);
    }
    
    public void i2d() {
        getMethodVisitor().visitInsn(I2D);
    }
    
    public void i2l() {
        getMethodVisitor().visitInsn(I2L);
    }
    
    public void i2f() {
        getMethodVisitor().visitInsn(I2F);
    }
    
    public void i2s() {
        getMethodVisitor().visitInsn(I2S);
    }
    
    public void i2c() {
        getMethodVisitor().visitInsn(I2C);
    }
    
    public void i2b() {
        getMethodVisitor().visitInsn(I2B);
    }
    
    public void ldiv() {
        getMethodVisitor().visitInsn(LDIV);
    }
    
    public void lrem() {
        getMethodVisitor().visitInsn(LREM);
    }
    
    public void lneg() {
        getMethodVisitor().visitInsn(LNEG);
    }
    
    public void l2d() {
        getMethodVisitor().visitInsn(L2D);
    }
    
    public void l2i() {
        getMethodVisitor().visitInsn(L2I);
    }
    
    public void l2f() {
        getMethodVisitor().visitInsn(L2F);
    }
    
    public void fdiv() {
        getMethodVisitor().visitInsn(FDIV);
    }
    
    public void frem() {
        getMethodVisitor().visitInsn(FREM);
    }
    
    public void fneg() {
        getMethodVisitor().visitInsn(FNEG);
    }
    
    public void f2d() {
        getMethodVisitor().visitInsn(F2D);
    }
    
    public void f2i() {
        getMethodVisitor().visitInsn(F2D);
    }
    
    public void f2l() {
        getMethodVisitor().visitInsn(F2L);
    }
    
    public void ddiv() {
        getMethodVisitor().visitInsn(DDIV);
    }
    
    public void drem() {
        getMethodVisitor().visitInsn(DREM);
    }
    
    public void dneg() {
        getMethodVisitor().visitInsn(DNEG);
    }
    
    public void d2f() {
        getMethodVisitor().visitInsn(D2F);
    }
    
    public void d2i() {
        getMethodVisitor().visitInsn(D2I);
    }
    
    public void d2l() {
        getMethodVisitor().visitInsn(D2L);
    }
    
    public void imul() {
        getMethodVisitor().visitInsn(IMUL);
    }
    
    public void lmul() {
        getMethodVisitor().visitInsn(LMUL);
    }
    
    public void fmul() {
        getMethodVisitor().visitInsn(FMUL);
    }
    
    public void dmul() {
        getMethodVisitor().visitInsn(DMUL);
    }
    
    public void iinc(int arg0, int arg1) {
        getMethodVisitor().visitIincInsn(arg0, arg1);
    }

    public void iinc(LocalVariable arg0, int arg1) {
        getMethodVisitor().visitIincInsn(arg0.idx, arg1);
    }
    
    public void monitorenter() {
        getMethodVisitor().visitInsn(MONITORENTER);
    }
    
    public void monitorexit() {
        getMethodVisitor().visitInsn(MONITOREXIT);
    }
    
    public void jsr(Label branch) {
        getMethodVisitor().visitJumpInsn(JSR, branch);
    }
    
    public void ret(int arg0) {
        getMethodVisitor().visitVarInsn(RET, arg0);
    }
    
    public AnnotationVisitor visitAnnotationDefault() {
        return getMethodVisitor().visitAnnotationDefault();
    }

    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
        return getMethodVisitor().visitAnnotation(arg0, arg1);
    }

    public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1,
                                                      boolean arg2) {
        return getMethodVisitor().visitParameterAnnotation(arg0, arg1, arg2);
    }

    public void visitAnnotationWithFields(String name, boolean visible, Map<String,Object> fields) {
        AnnotationVisitor visitor = visitAnnotation(name, visible);
        visitAnnotationFields(visitor, fields);
        visitor.visitEnd();
    }

    public void visitParameterAnnotationWithFields(int param, String name, boolean visible, Map<String,Object> fields) {
        AnnotationVisitor visitor = visitParameterAnnotation(param, name, visible);
        visitAnnotationFields(visitor, fields);
        visitor.visitEnd();
    }

    public void visitAttribute(Attribute arg0) {
        getMethodVisitor().visitAttribute(arg0);
    }

    public void visitCode() {
        getMethodVisitor().visitCode();
    }

    public void visitInsn(int arg0) {
        getMethodVisitor().visitInsn(arg0);
    }

    public void visitIntInsn(int arg0, int arg1) {
        getMethodVisitor().visitIntInsn(arg0, arg1);
    }

    public void visitVarInsn(int arg0, int arg1) {
        getMethodVisitor().visitVarInsn(arg0, arg1);
    }

    public void visitTypeInsn(int arg0, String arg1) {
        getMethodVisitor().visitTypeInsn(arg0, arg1);
    }

    public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
        getMethodVisitor().visitFieldInsn(arg0, arg1, arg2, arg3);
    }

    public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
        getMethodVisitor().visitMethodInsn(arg0, arg1, arg2, arg3);
    }

    public void visitJumpInsn(int arg0, Label arg1) {
        getMethodVisitor().visitJumpInsn(arg0, arg1);
    }

    public void visitLabel(Label arg0) {
        getMethodVisitor().visitLabel(arg0);
    }

    public void visitLdcInsn(Object arg0) {
        getMethodVisitor().visitLdcInsn(arg0);
    }

    public void visitIincInsn(int arg0, int arg1) {
        getMethodVisitor().visitIincInsn(arg0, arg1);
    }

    public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
                                     Label[] arg3) {
        getMethodVisitor().visitTableSwitchInsn(arg0, arg1, arg2, arg3);
    }

    public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
        getMethodVisitor().visitLookupSwitchInsn(arg0, arg1, arg2);
    }

    public void visitMultiANewArrayInsn(String arg0, int arg1) {
        getMethodVisitor().visitMultiANewArrayInsn(arg0, arg1);
    }

    public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
                                   String arg3) {
        getMethodVisitor().visitTryCatchBlock(arg0, arg1, arg2, arg3);
    }

    public void visitLocalVariable(String arg0, String arg1, String arg2,
                                   Label arg3, Label arg4, int arg5) {
        getMethodVisitor().visitLocalVariable(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public void visitLineNumber(int arg0, Label arg1) {
        getMethodVisitor().visitLineNumber(arg0, arg1);
    }

    public void visitMaxs(int arg0, int arg1) {
        if (DEBUG) {
            dump();
        }
        getMethodVisitor().visitMaxs(arg0, arg1);
    }

    public void visitEnd() {
        getMethodVisitor().visitEnd();
    }
    
    public void tableswitch(int min, int max, Label defaultLabel, Label[] cases) {
        getMethodVisitor().visitTableSwitchInsn(min, max, defaultLabel, cases);
    }

    public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
        getMethodVisitor().visitFrame(arg0, arg1, arg2, arg3, arg4);
    }
}
