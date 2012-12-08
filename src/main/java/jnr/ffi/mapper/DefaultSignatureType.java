package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import static jnr.ffi.util.Annotations.sortedAnnotationCollection;

/**
*
*/
public final class DefaultSignatureType implements SignatureType {
    private final Class declaredClass;
    private final Collection<Annotation> annotations;
    private final Type genericType;

    public DefaultSignatureType(Class declaredClass, Collection<Annotation> annotations, Type genericType) {
        this.declaredClass = declaredClass;
        this.annotations = sortedAnnotationCollection(annotations);
        this.genericType = genericType;
    }

    public Class getDeclaredType() {
        return declaredClass;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }

    public Type getGenericType() {
        return genericType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultSignatureType signature = (DefaultSignatureType) o;

        return declaredClass == signature.declaredClass
                && genericType.equals(signature.genericType)
                && annotations.equals(signature.annotations)
                ;
    }

    @Override
    public int hashCode() {
        int result = declaredClass.hashCode();
        result = 31 * result + annotations.hashCode();
        if (genericType != null) result = 31 * result + genericType.hashCode();
        return result;
    }

    public static DefaultSignatureType create(Class type, FromNativeContext context) {
        Type genericType = !type.isPrimitive() && context instanceof MethodResultContext
                ? ((MethodResultContext) context).getMethod().getGenericReturnType() : type;
        return new DefaultSignatureType(type, context.getAnnotations(), genericType);
    }

    public static DefaultSignatureType create(Class type, ToNativeContext context) {
        Type genericType = type;
        if (!type.isPrimitive() && context instanceof MethodParameterContext) {
            MethodParameterContext methodParameterContext = (MethodParameterContext) context;
            genericType = methodParameterContext.getMethod().getGenericParameterTypes()[methodParameterContext.getParameterIndex()];
        }

        return new DefaultSignatureType(type, context.getAnnotations(), genericType);
    }
}
