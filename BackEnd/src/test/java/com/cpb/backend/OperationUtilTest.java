package com.cpb.backend;

import com.cpb.backend.entity.Operation;
import com.cpb.backend.util.OperationUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

@Slf4j
public class OperationUtilTest {

    @Test
    public void testAddIfOperationValid() {
        SortedSet<Operation> operationSet = new TreeSet<>();
        operationSet.add(new Operation("insert", 0, "a", System.currentTimeMillis()));
        operationSet.add(new Operation("insert", 0, "b", System.currentTimeMillis() + 1));
        operationSet.add(new Operation("insert", 0, "c", System.currentTimeMillis() + 2));

        assert OperationUtil.addIfOperationValid(new Operation("insert", 0, "d", System.currentTimeMillis() + 3), operationSet);
        assert !OperationUtil.addIfOperationValid(new Operation("insert", 10, "d", System.currentTimeMillis() + 4), operationSet);
        assert OperationUtil.addIfOperationValid(new Operation("delete", 0, "d", System.currentTimeMillis() + 5), operationSet);
        assert !OperationUtil.addIfOperationValid(new Operation("delete", 0, "d", System.currentTimeMillis() + 6), operationSet);
    }

    @Test
    @SneakyThrows
    public void testAddIfOperationValidEfficiency() {
        log.info("Testing efficiency of addIfOperationValid");
        int count = 0, size = 10000;
        SortedSet<Operation> operationSet = new TreeSet<>();
        for (int i = 0; i < size; i++) {
            operationSet.add(new Operation("insert", 0, "abc", System.currentTimeMillis() + count++));
        }
        log.info("Operation set size: {}", operationSet.size());

        long start = System.currentTimeMillis();
        OperationUtil.addIfOperationValid(new Operation("insert", 0, "abc", System.currentTimeMillis() + count++), operationSet);
        log.info("Time taken: {} ms", System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        OperationUtil.addIfOperationValid(new Operation("delete", 0, "abc", System.currentTimeMillis()), operationSet);
        log.info("Time taken: {} ms", System.currentTimeMillis() - start);
    }
}
