package com.cpb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Type:<p>
 * 1. insert<p>
 * 2. delete<p>
 * 3. init(Resend all operations)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation implements Comparable<Operation>, Serializable {
    private String type;
    private int position;
    private String content;
    private long timestamp;
    private long latestTimestamp;

    @Override
    public int compareTo(Operation o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}
