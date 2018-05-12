package com.revolut.test.db;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRepository<T> {

    private final long lockTimeout;
    protected Map<Long, T> store = new ConcurrentHashMap<>();
    private Map<Long, ReentrantLock> rowLock = new ConcurrentHashMap<>();

    protected AbstractRepository(long lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

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
            rowLock.put(id, new ReentrantLock());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Object should have getId method", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Object should have getId method", e);
        }
    }

    protected boolean lock(Long id) {
        try {
            return rowLock.get(id).tryLock(lockTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Lock operation has been interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    protected void unlock(Long id) {
        ReentrantLock reentrantLock = rowLock.get(id);
        if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
            reentrantLock.unlock();
        }
    }

    public Set<Long> getAll() {
        return store.keySet();
    }

}
