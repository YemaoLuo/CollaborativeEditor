package com.cpb.backend.util;

import com.cpb.backend.entity.Operation;

import java.util.SortedSet;

public class OperationUtil {

    public static boolean addIfOperationValid(Operation operation, SortedSet<Operation> operationSet) {
        if (operationSet == null) {
            return true;
        }

        operationSet.add(operation);
        StringBuilder sb = new StringBuilder();
        for (Operation op : operationSet) {
            String type = op.getType();
            if (type.equals("insert")) {
                int position = op.getPosition();
                if (position > sb.length()) {
                    operationSet.remove(operation);
                    return false;
                }
                sb.insert(position, op.getContent());
            } else if (type.equals("delete")) {
                int pos = op.getPosition();
                String content = op.getContent();
                int endIndex = pos + content.length();
                if (endIndex > sb.length() || !content.equals(sb.substring(pos, endIndex))) {
                    operationSet.remove(operation);
                    return false;
                }
                sb.delete(pos, endIndex);
            }
        }
        return true;
    }
}
