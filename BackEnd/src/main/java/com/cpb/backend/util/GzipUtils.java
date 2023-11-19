package com.cpb.backend.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

    public static byte[] compressString(String obj) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);

        gzipOutputStream.write(obj.getBytes(StandardCharsets.UTF_8));

        gzipOutputStream.close();

        return outputStream.toByteArray();
    }
}
