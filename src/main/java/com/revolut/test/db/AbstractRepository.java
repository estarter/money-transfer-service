package com.revolut.test.db;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.revolut.test.api.DataObject;
import com.revolut.test.db.support.ObjectNotFoundException;
import com.revolut.test.db.support.OptimisticLockException;
import com.revolut.test.db.support.RepositoryItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRepository<T extends DataObject<ID>, ID extends Comparable<ID>> {

    private final long lockTimeout;
    /* object storage id -> object */
    final Map<ID, RepositoryItem<T>> store = new ConcurrentHashMap<>();
    /* each object can be independently locked, therefore id -> lock map */
    final Map<ID, ReentrantLock> rowLock = new ConcurrentHashMap<>();

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
    public T get(ID id) {
        RepositoryItem<T> item = store.get(id);
        if (item == null) {
            throw new ObjectNotFoundException("Can't find account with id '" + id + "'");
        }
        return item.getObject();
    }

    public T getLatest() {
        return store.keySet().stream().max(Comparator.naturalOrder()).map(this::get).orElse(null);
    }

    /**
     * Persist the object.
     * <p>
     * Object must have getId method that returns its unique identifier.
     */
    public T save(T object) {
        ID id = object.getId();
        if (store.containsKey(id)) {
            Long version = object.getVersion();
            Long origVersion = get(id).getVersion();
            if (!version.equals(origVersion)) {
                throw new OptimisticLockException(
                        "Object " + object.getClass().getSimpleName() + " #" + id + " is out of sync.");
            }
            object.setVersion(version + 1);
        } else {
            object.setVersion(1L);
        }
        store.put(id, new RepositoryItem<>(object));
        rowLock.putIfAbsent(id, new ReentrantLock());
        return object;
    }

    /**
     * Acquires the lock for the object.
     *
     * @return whether the lock is acquired
     * @throws ObjectNotFoundException if object is not found
     */
    boolean lock(ID id) {
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
    void unlock(ID id) {
        ReentrantLock reentrantLock = rowLock.get(id);
        if (reentrantLock != null && reentrantLock.isHeldByCurrentThread()) {
            reentrantLock.unlock();
        }
    }

    /**
     * @return id of all stored objects
     */
    public Set<ID> getAll() {
        return store.keySet();
    }

}
