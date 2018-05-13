package com.revolut.test.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.revolut.test.db.support.ObjectNotFoundException;
import com.revolut.test.db.support.OptimisticLockException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRepository<T> {

    private final long lockTimeout;
    /* object storage id -> object */
    final Map<Long, T> store = new ConcurrentHashMap<>();
    /* each object can be independently locked, therefore id -> lock map */
    private final Map<Long, ReentrantLock> rowLock = new ConcurrentHashMap<>();

    AbstractRepository(long lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    /**
     * method returns an object by id.
     * <p>
     * Implementation details.
     * To isolate modifications on the object this method would return a copy of the object. #save() method should
     * be explicitly called to persist the object.
     * <p>
     * Object must have a copy constructor.
     *
     * @throws ObjectNotFoundException if object is not found
     */
    public T get(Long id) {
        T result = store.get(id);
        if (result == null) {
            throw new ObjectNotFoundException("Can't find account with id '" + id + "'");
        }

        return copyObject(result);
    }

    /**
     * Persist the object.
     * <p>
     * Object must have getId method that returns its unique identifier.
     */
    public void save(T object) {
        try {
            long id = (Long) object.getClass().getMethod("getId").invoke(object);
            if (store.containsKey(id)) {
                T origObject = store.get(id);
                Long version = (Long) object.getClass().getMethod("getVersion").invoke(object);
                Long origVersion = (Long) origObject.getClass().getMethod("getVersion").invoke(object);
                if (!version.equals(origVersion)) {
                    throw new OptimisticLockException(
                            "Object " + object.getClass().getName() + " #" + id + " is out of sync.");
                }
                object.getClass().getMethod("setVersion", Long.class).invoke(object, version + 1);
            } else {
                object.getClass().getMethod("setVersion", Long.class).invoke(object, 1L);
            }
            store.put(id, copyObject(object));
            rowLock.putIfAbsent(id, new ReentrantLock());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Object should have getId method", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Error invocation getId method", e);
        }
    }

    /**
     * Acquires the lock for the object.
     *
     * @return whether the lock is acquired
     * @throws ObjectNotFoundException if object is not found
     */
    boolean lock(Long id) {
        try {
            if (!rowLock.containsKey(id)) {
                throw new ObjectNotFoundException("Can't find account with id '" + id + "'");
            }
            return rowLock.get(id).tryLock(lockTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Lock operation has been interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Release the lock for the object
     */
    void unlock(Long id) {
        ReentrantLock reentrantLock = rowLock.get(id);
        if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
            reentrantLock.unlock();
        }
    }

    /**
     * @return id of all stored objects
     */
    public Set<Long> getAll() {
        return store.keySet();
    }

    private T copyObject(T result) {
        try {
            Constructor constructor = result.getClass().getConstructor(result.getClass());
            return (T) constructor.newInstance(result);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Object should implement a copy constructor", e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Error invocation copy constructor", e);
        }
    }
}
