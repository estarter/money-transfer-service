package com.revolut.test.db.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class RepositoryItem<T> {

    private String data;

    public RepositoryItem(T object) {
        setObject(object);
    }

    public T getObject() {
        byte[] rawData = Base64.getDecoder().decode(data);
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(rawData))) {
            return (T) objIn.readObject();
        } catch (IOException e) {
            throw new IllegalStateException("Can't read an object", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Can't deserialize an object", e);
        }
    }

    void setObject(T object) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream objOut = new ObjectOutputStream(out);) {
            objOut.writeObject(object);
            objOut.flush();
            out.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Can't write an object", e);
        }
        data = Base64.getEncoder().encodeToString(out.toByteArray());
    }
}