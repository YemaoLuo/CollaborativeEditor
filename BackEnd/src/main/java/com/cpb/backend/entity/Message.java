package com.cpb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * For MDHandler type code:<p>
 * 1. Online count<p>
 * 2. OnOpen or Not valid Operation<p>
 * 3. Broadcast new Operation<p>
 * 4. Client request Initial Data<p>
 * <p>
 * For DrawingHandler type code:<p>
 * 1. Online count<p>
 * 2. Broadcast new Drawing data
 */
@Data
@AllArgsConstructor
public class Message {

    private int type;

    private Object message;
}
