package com.zj.basicform.common.server;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zj
 * @since 2019/2/9
 */
public abstract class ShutdownSocketServer {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static AtomicBoolean isRunning = new AtomicBoolean();
    private String name;
    private int port;
    private String secretKey;

    public ShutdownSocketServer(String name, int port, String secretKey) {
        this.name = name;
        this.port = port;
        this.secretKey = secretKey;
    }

    protected abstract void shutdown();

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            new Thread(new Runnable() {
                private boolean isClosed = false;

                @Override
                public void run() {
                    ServerSocket serverSocket = null;
                    BufferedReader br = null;
                    try {
                        serverSocket = new ServerSocket(port);
                        while (!isClosed) {
                            Socket socket = serverSocket.accept();
                            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String data = br.readLine();
                            if (StringUtils.isNotBlank(data) && data.equals(secretKey)) {
                                isClosed = true;
                                shutdown();
                            }
                        }
                    } catch (IOException e) {
                        logger.error("shutdown server socket init failed.", e);
                    } finally {
                        if (serverSocket != null) {
                            try {
                                serverSocket.close();
                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }

                }
            }, name).start();
            logger.info("{} Socket Server start, {}:{}", name, "127.0.0.1", port);
        }
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }
}
