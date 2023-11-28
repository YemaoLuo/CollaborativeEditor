package com.cpb.backend.data;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiringMap<K, V> {
    private static final Map<Object, ExpiringValue<?>> map = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public ExpiringMap() {
        scheduleCleanupTask();
    }

    public void put(K key, V value, long expirationTime, TimeUnit timeUnit) {
        ExpiringValue<V> expiringValue = new ExpiringValue<>(value, expirationTime, timeUnit);
        map.put(key, expiringValue);
    }

    public V get(K key) {
        ExpiringValue<?> expiringValue = map.get(key);
        if (expiringValue != null && !expiringValue.isExpired()) {
            return (V) expiringValue.getValue();
        }
        return null;
    }

    private void scheduleCleanupTask() {
        executorService.scheduleWithFixedDelay(this::cleanupExpiredEntries, 1, 1, TimeUnit.SECONDS);
    }

    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        map.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
    }

    private static class ExpiringValue<T> {
        @Getter
        private final T value;
        private final long expirationTimeMillis;

        public ExpiringValue(T value, long expirationTime, TimeUnit timeUnit) {
            this.value = value;
            this.expirationTimeMillis = System.currentTimeMillis() + timeUnit.toMillis(expirationTime);
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long currentTime) {
            return currentTime > expirationTimeMillis;
        }
    }
}