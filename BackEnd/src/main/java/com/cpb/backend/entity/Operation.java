package com.cpb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Type:<p>
 * 1. insert<p>
 * 2. delete<p>
 * 3. init(Resend all operations)
 */
@Data
@AllArgsConstructor
public class Operation implements Comparable<Operation> {
    private String type;
    private int position;
    private String content;
    private long timestamp;

    @Override
    public int compareTo(@NotNull Operation o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}
