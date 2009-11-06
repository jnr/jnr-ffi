package com.kenai.jaffl.provider.jffi;

final class AsmLoader extends ClassLoader {

    static final AsmLoader INSTANCE = new AsmLoader();

    public AsmLoader() {
    }

    public AsmLoader(ClassLoader parent) {
        super(parent);
    }


    public Class defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
