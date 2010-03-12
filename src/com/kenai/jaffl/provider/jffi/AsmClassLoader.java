package com.kenai.jaffl.provider.jffi;

final class AsmClassLoader extends ClassLoader {

    public AsmClassLoader() {
    }

    public AsmClassLoader(ClassLoader parent) {
        super(parent);
    }


    public Class defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
