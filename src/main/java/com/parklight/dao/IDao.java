package com.parklight.dao;

import java.util.List;

/**
 * Generic data access interface for persisting domain objects.
 * Implementations decide where the data lives (file, database, memory, ...).
 *
 * @param <K> the type of the unique key (e.g. ticket id, license plate)
 * @param <V> the type of the stored object
 */
public interface IDao<K, V> {

    // Saves a new record or replaces an existing one with the same key.
    void save(K key, V value);

    // Returns the record with the given key, or null if no such record exists.
    V get(K key);

    // Removes the record with the given key. Returns true if something was removed.
    boolean delete(K key);

    // Returns all stored records as a list (order is not guaranteed).
    List<V> getAll();
}
