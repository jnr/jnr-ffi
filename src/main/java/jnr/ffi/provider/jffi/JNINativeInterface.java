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

/**
 *
 * WARNING:  Highly experimental!!!
 *
 * This class contains constants that are the indexes withing the JNINativeInterface_
 * struct of each of the JNIEnv functions.  To invoke one of the functions, the
 * index _must_ be multiplied by sizeof(void *), then used as an index into the table.
 *
 * These indexes are valid on both 32bit and 64bit linux and macos.  Win32 also
 * seems to be the same.  Other platforms may be completely different.
 */
public final class JNINativeInterface {

    private JNINativeInterface() {
    }

    public static final int GetVersion = 4;
    public static final int DefineClass = 5;
    public static final int FindClass = 6;
    public static final int FromReflectedMethod = 7;
    public static final int FromReflectedField = 8;
    public static final int ToReflectedMethod = 9;
    public static final int GetSuperclass = 10;
    public static final int IsAssignableFrom = 11;
    public static final int ToReflectedField = 12;
    public static final int Throw = 13;
    public static final int ThrowNew = 14;
    public static final int ExceptionOccurred = 15;
    public static final int ExceptionDescribe = 16;
    public static final int ExceptionClear = 17;
    public static final int FatalError = 18;
    public static final int PushLocalFrame = 19;
    public static final int PopLocalFrame = 20;
    public static final int NewGlobalRef = 21;
    public static final int DeleteGlobalRef = 22;
    public static final int DeleteLocalRef = 23;
    public static final int IsSameObject = 24;
    public static final int NewLocalRef = 25;
    public static final int EnsureLocalCapacity = 26;
    public static final int AllocObject = 27;
    public static final int NewObject = 28;
    public static final int NewObjectV = 29;
    public static final int NewObjectA = 30;
    public static final int GetObjectClass = 31;
    public static final int IsInstanceOf = 32;
    public static final int GetMethodID = 33;
    public static final int CallObjectMethod = 34;
    public static final int CallObjectMethodV = 35;
    public static final int CallObjectMethodA = 36;
    public static final int CallBooleanMethod = 37;
    public static final int CallBooleanMethodV = 38;
    public static final int CallBooleanMethodA = 39;
    public static final int CallByteMethod = 40;
    public static final int CallByteMethodV = 41;
    public static final int CallByteMethodA = 42;
    public static final int CallCharMethod = 43;
    public static final int CallCharMethodV = 44;
    public static final int CallCharMethodA = 45;
    public static final int CallShortMethod = 46;
    public static final int CallShortMethodV = 47;
    public static final int CallShortMethodA = 48;
    public static final int CallIntMethod = 49;
    public static final int CallIntMethodV = 50;
    public static final int CallIntMethodA = 51;
    public static final int CallLongMethod = 52;
    public static final int CallLongMethodV = 53;
    public static final int CallLongMethodA = 54;
    public static final int CallFloatMethod = 55;
    public static final int CallFloatMethodV = 56;
    public static final int CallFloatMethodA = 57;
    public static final int CallDoubleMethod = 58;
    public static final int CallDoubleMethodV = 59;
    public static final int CallDoubleMethodA = 60;
    public static final int CallVoidMethod = 61;
    public static final int CallVoidMethodV = 62;
    public static final int CallVoidMethodA = 63;
    public static final int CallNonvirtualObjectMethod = 64;
    public static final int CallNonvirtualObjectMethodV = 65;
    public static final int CallNonvirtualObjectMethodA = 66;
    public static final int CallNonvirtualBooleanMethod = 67;
    public static final int CallNonvirtualBooleanMethodV = 68;
    public static final int CallNonvirtualBooleanMethodA = 69;
    public static final int CallNonvirtualByteMethod = 70;
    public static final int CallNonvirtualByteMethodV = 71;
    public static final int CallNonvirtualByteMethodA = 72;
    public static final int CallNonvirtualCharMethod = 73;
    public static final int CallNonvirtualCharMethodV = 74;
    public static final int CallNonvirtualCharMethodA = 75;
    public static final int CallNonvirtualShortMethod = 76;
    public static final int CallNonvirtualShortMethodV = 77;
    public static final int CallNonvirtualShortMethodA = 78;
    public static final int CallNonvirtualIntMethod = 79;
    public static final int CallNonvirtualIntMethodV = 80;
    public static final int CallNonvirtualIntMethodA = 81;
    public static final int CallNonvirtualLongMethod = 82;
    public static final int CallNonvirtualLongMethodV = 83;
    public static final int CallNonvirtualLongMethodA = 84;
    public static final int CallNonvirtualFloatMethod = 85;
    public static final int CallNonvirtualFloatMethodV = 86;
    public static final int CallNonvirtualFloatMethodA = 87;
    public static final int CallNonvirtualDoubleMethod = 88;
    public static final int CallNonvirtualDoubleMethodV = 89;
    public static final int CallNonvirtualDoubleMethodA = 90;
    public static final int CallNonvirtualVoidMethod = 91;
    public static final int CallNonvirtualVoidMethodV = 92;
    public static final int CallNonvirtualVoidMethodA = 93;
    public static final int GetFieldID = 94;
    public static final int GetObjectField = 95;
    public static final int GetBooleanField = 96;
    public static final int GetByteField = 97;
    public static final int GetCharField = 98;
    public static final int GetShortField = 99;
    public static final int GetIntField = 100;
    public static final int GetLongField = 101;
    public static final int GetFloatField = 102;
    public static final int GetDoubleField = 103;
    public static final int SetObjectField = 104;
    public static final int SetBooleanField = 105;
    public static final int SetByteField = 106;
    public static final int SetCharField = 107;
    public static final int SetShortField = 108;
    public static final int SetIntField = 109;
    public static final int SetLongField = 110;
    public static final int SetFloatField = 111;
    public static final int SetDoubleField = 112;
    public static final int GetStaticMethodID = 113;
    public static final int CallStaticObjectMethod = 114;
    public static final int CallStaticObjectMethodV = 115;
    public static final int CallStaticObjectMethodA = 116;
    public static final int CallStaticBooleanMethod = 117;
    public static final int CallStaticBooleanMethodV = 118;
    public static final int CallStaticBooleanMethodA = 119;
    public static final int CallStaticByteMethod = 120;
    public static final int CallStaticByteMethodV = 121;
    public static final int CallStaticByteMethodA = 122;
    public static final int CallStaticCharMethod = 123;
    public static final int CallStaticCharMethodV = 124;
    public static final int CallStaticCharMethodA = 125;
    public static final int CallStaticShortMethod = 126;
    public static final int CallStaticShortMethodV = 127;
    public static final int CallStaticShortMethodA = 128;
    public static final int CallStaticIntMethod = 129;
    public static final int CallStaticIntMethodV = 130;
    public static final int CallStaticIntMethodA = 131;
    public static final int CallStaticLongMethod = 132;
    public static final int CallStaticLongMethodV = 133;
    public static final int CallStaticLongMethodA = 134;
    public static final int CallStaticFloatMethod = 135;
    public static final int CallStaticFloatMethodV = 136;
    public static final int CallStaticFloatMethodA = 137;
    public static final int CallStaticDoubleMethod = 138;
    public static final int CallStaticDoubleMethodV = 139;
    public static final int CallStaticDoubleMethodA = 140;
    public static final int CallStaticVoidMethod = 141;
    public static final int CallStaticVoidMethodV = 142;
    public static final int CallStaticVoidMethodA = 143;
    public static final int GetStaticFieldID = 144;
    public static final int GetStaticObjectField = 145;
    public static final int GetStaticBooleanField = 146;
    public static final int GetStaticByteField = 147;
    public static final int GetStaticCharField = 148;
    public static final int GetStaticShortField = 149;
    public static final int GetStaticIntField = 150;
    public static final int GetStaticLongField = 151;
    public static final int GetStaticFloatField = 152;
    public static final int GetStaticDoubleField = 153;
    public static final int SetStaticObjectField = 154;
    public static final int SetStaticBooleanField = 155;
    public static final int SetStaticByteField = 156;
    public static final int SetStaticCharField = 157;
    public static final int SetStaticShortField = 158;
    public static final int SetStaticIntField = 159;
    public static final int SetStaticLongField = 160;
    public static final int SetStaticFloatField = 161;
    public static final int SetStaticDoubleField = 162;
    public static final int NewString = 163;
    public static final int GetStringLength = 164;
    public static final int GetStringChars = 165;
    public static final int ReleaseStringChars = 166;
    public static final int NewStringUTF = 167;
    public static final int GetStringUTFLength = 168;
    public static final int GetStringUTFChars = 169;
    public static final int ReleaseStringUTFChars = 170;
    public static final int GetArrayLength = 171;
    public static final int NewObjectArray = 172;
    public static final int GetObjectArrayElement = 173;
    public static final int SetObjectArrayElement = 174;
    public static final int NewBooleanArray = 175;
    public static final int NewByteArray = 176;
    public static final int NewCharArray = 177;
    public static final int NewShortArray = 178;
    public static final int NewIntArray = 179;
    public static final int NewLongArray = 180;
    public static final int NewFloatArray = 181;
    public static final int NewDoubleArray = 182;
    public static final int GetBooleanArrayElements = 183;
    public static final int GetByteArrayElements = 184;
    public static final int GetCharArrayElements = 185;
    public static final int GetShortArrayElements = 186;
    public static final int GetIntArrayElements = 187;
    public static final int GetLongArrayElements = 188;
    public static final int GetFloatArrayElements = 189;
    public static final int GetDoubleArrayElements = 190;
    public static final int ReleaseBooleanArrayElements = 191;
    public static final int ReleaseByteArrayElements = 192;
    public static final int ReleaseCharArrayElements = 193;
    public static final int ReleaseShortArrayElements = 194;
    public static final int ReleaseIntArrayElements = 195;
    public static final int ReleaseLongArrayElements = 196;
    public static final int ReleaseFloatArrayElements = 197;
    public static final int ReleaseDoubleArrayElements = 198;
    public static final int GetBooleanArrayRegion = 199;
    public static final int GetByteArrayRegion = 200;
    public static final int GetCharArrayRegion = 201;
    public static final int GetShortArrayRegion = 202;
    public static final int GetIntArrayRegion = 203;
    public static final int GetLongArrayRegion = 204;
    public static final int GetFloatArrayRegion = 205;
    public static final int GetDoubleArrayRegion = 206;
    public static final int SetBooleanArrayRegion = 207;
    public static final int SetByteArrayRegion = 208;
    public static final int SetCharArrayRegion = 209;
    public static final int SetShortArrayRegion = 210;
    public static final int SetIntArrayRegion = 211;
    public static final int SetLongArrayRegion = 212;
    public static final int SetFloatArrayRegion = 213;
    public static final int SetDoubleArrayRegion = 214;
    public static final int RegisterNatives = 215;
    public static final int UnregisterNatives = 216;
    public static final int MonitorEnter = 217;
    public static final int MonitorExit = 218;
    public static final int GetJavaVM = 219;
    public static final int GetStringRegion = 220;
    public static final int GetStringUTFRegion = 221;
    public static final int GetPrimitiveArrayCritical = 222;
    public static final int ReleasePrimitiveArrayCritical = 223;
    public static final int GetStringCritical = 224;
    public static final int ReleaseStringCritical = 225;
    public static final int NewWeakGlobalRef = 226;
    public static final int DeleteWeakGlobalRef = 227;
    public static final int ExceptionCheck = 228;
    public static final int NewDirectByteBuffer = 229;
    public static final int GetDirectBufferAddress = 230;
    public static final int GetDirectBufferCapacity = 231;
    public static final int GetObjectRefType = 232;
}
