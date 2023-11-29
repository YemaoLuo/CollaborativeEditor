package com.cpb.backend.util;

import com.cpb.backend.entity.Operation;

import java.util.SortedSet;
import java.util.TreeSet;

public class OperationUtil {

    public static boolean addIfOperationValid(Operation operation, SortedSet<Operation> operationSet) {
        if (operationSet == null) {
            return true;
        }

        for (Operation op : operationSet) {
            if (op.getLatestTimestamp() > operation.getTimestamp()) {
                return false;
            }
        }

        SortedSet<Operation> backupSet = new TreeSet<>(operationSet);

        StringBuilder sb = new StringBuilder();
        for (Operation op : backupSet) {
            String type = op.getType();
            long timestamp = op.getTimestamp();
            long latestTimestamp = op.getLatestTimestamp();

            if (timestamp < latestTimestamp) {
                operationSet.remove(op);
            }

            if (type.equals("insert")) {
                int position = op.getPosition();
                if (position > sb.length()) {
                    operationSet.remove(operation);
                    return false;
                }
                if (timestamp < latestTimestamp) {
                    position -= sb.substring(0, position).length();
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
                if (timestamp < latestTimestamp) {
                    pos -= sb.substring(0, pos).length();
                    endIndex -= sb.substring(0, endIndex).length();
                }
                sb.delete(pos, endIndex);
            }
        }

        operationSet.add(operation);
        return true;
    }
}
