package com.cpb.backend;

import com.cpb.backend.data.ExpiringMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ExpiringMapTest {

    @Test
    @SneakyThrows
    public void test() {
        ExpiringMap<String, Object> expiringMap = new ExpiringMap<>();
        expiringMap.put("key1", "key1value", 5, TimeUnit.SECONDS);
        Object value = expiringMap.get("key1");
        log.info("value: {}", value);
        Thread.sleep(6000);
        value = expiringMap.get("key1");
        log.info("value: {}", value);
        value = expiringMap.get("key2");
        log.info("value: {}", value);
    }
}
