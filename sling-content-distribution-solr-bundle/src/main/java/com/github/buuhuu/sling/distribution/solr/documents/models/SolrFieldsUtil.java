package com.github.buuhuu.sling.distribution.solr.documents.models;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.buuhuu.sling.distribution.solr.documents.SolrField;

/**
 * Utilities used for the default implementation of {@link HasSolrFields}.
 */
public class SolrFieldsUtil {

    private SolrFieldsUtil() {
        super();
    }

    /**
     * Returns an {@link Iterator} of {@link SolrField} for the given instance of {@link HasSolrFields}. The {@link Iterator} looks up
     * any public method not returning void, not accepting parameters and being annotated with {@link SolrFieldGetter} and returns them
     * mapped to the value the invocation of the method returns on the given {@link Object}.
     *
     * @param target
     * @return
     */
    public static Iterator<SolrField> getSolrFields(HasSolrFields target) {
        return new SolrFieldsIterator(target);
    }

    private static List<Method> readSolrFieldGetters(Class<?> target, List<Method> methods) {
        if (target == null || target.equals(Object.class)) {
            return methods;
        }

        for (Method method : target.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.isAnnotationPresent(SolrFieldGetter.class)
                    && method.getReturnType() != Void.TYPE && method.getParameterCount() == 0) {
                methods.add(method);
            }
        }

        return readSolrFieldGetters(target.getSuperclass(), methods);
    }

    private static class SolrFieldsIterator implements Iterator<SolrField> {

        private final Iterator<Method> methods;
        private final Object target;
        private SolrField next;

        SolrFieldsIterator(Object target) {
            this.target = target;
            this.methods = readSolrFieldGetters(target.getClass(), new LinkedList<>()).iterator();
            seek();
        }

        private void seek() {
            if (methods.hasNext()) {
                Method nextGetter = methods.next();
                SolrFieldGetter meta = nextGetter.getAnnotation(SolrFieldGetter.class);
                next = new SolrField() {
                    @Override public String getName() {
                        return meta.name();
                    }

                    @Override public Object getValue() {
                        try {
                            return nextGetter.invoke(target);
                        } catch (InvocationTargetException | IllegalAccessException ex) {
                            throw new IllegalArgumentException("Could call " + nextGetter.getName() + " on " + target, ex);
                        }
                    }
                };
            } else {
                next = null;
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public SolrField next() {
            SolrField current = next;
            seek();
            return current;
        }
    }
}
