package com.revolut.test.db;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRepository<T> {

    private Map<Long, T> store = new HashMap<>();

    public T get(Long id) {
        T result = store.get(id);
        if (result == null)
            throw new ObjectNotFoundException("Can't find account with id '" + id + "'");
        return result;
    }

    public void save(T object) {
        // todo validate object before save
        try {
            long id = (Long) object.getClass().getMethod("getId").invoke(object);
            store.put(id, object);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Object should have getId method", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Object should have getId method", e);
        }
    }

    public Set<Long> getAll() {
        return store.keySet();
    }

}
