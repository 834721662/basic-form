package server;


/**
 * @author zj
 * @since 2019/2/28
 */
public class TestBootstrap {

    public static void main(String[] args) throws InterruptedException {
        HttpServer server = new HttpServer();
        server.start();
    }

}
