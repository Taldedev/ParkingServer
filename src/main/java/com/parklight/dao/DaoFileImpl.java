package com.parklight.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File-backed implementation of IDao using Java Object Serialization.
 * The whole key-value map is serialized to a single file on every write.
 * This is simple and sufficient for a course project; not optimized for scale.
 *
 * @param <K> the key type (must be Serializable)
 * @param <V> the value type (must be Serializable)
 */
public class DaoFileImpl<K extends Serializable, V extends Serializable> implements IDao<K, V> {

    private final String filePath;
    private Map<K, V> data;

    public DaoFileImpl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        this.filePath = filePath;
        this.data = loadFromFile();
    }

    @Override
    public void save(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        data.put(key, value);
        writeToFile();
    }

    @Override
    public V get(K key) {
        return data.get(key);
    }

    @Override
    public boolean delete(K key) {
        if (!data.containsKey(key)) {
            return false;
        }
        data.remove(key);
        writeToFile();
        return true;
    }

    @Override
    public List<V> getAll() {
        return new ArrayList<>(data.values());
    }

    // Reads the map from disk. An empty/missing file means "no data yet".
    @SuppressWarnings("unchecked")
    private Map<K, V> loadFromFile() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof Map) {
                return (Map<K, V>) obj;
            }
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to read DAO file: " + filePath, e);
        }
    }

    // Writes the full map back to disk.
    private void writeToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write DAO file: " + filePath, e);
        }
    }
}
