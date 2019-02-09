package com.zj.basicform.common.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author zj
 * @since 2019/2/9
 */
public class SimpleSocketClient {
    private static Logger logger = LoggerFactory.getLogger(SimpleSocketClient.class);

    public static void write(String host, int port, String content) {
        Socket socket = null;
        BufferedReader reader = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(content.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.flush();
            InputStream inputStream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
