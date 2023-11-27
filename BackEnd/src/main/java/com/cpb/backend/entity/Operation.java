package com.cpb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Type:<p>
 * 1. insert<p>
 * 2. delete<p>
 * 3. init(Resend all operations)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation implements Comparable<Operation> {
    private String type;
    private int position;
    private String content;
    private long timestamp;

    @Override
    public int compareTo(Operation o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}
