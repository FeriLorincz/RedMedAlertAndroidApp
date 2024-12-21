package com.feri.redmedalertandroidapp.repository;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String error);
}
