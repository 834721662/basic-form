package server.Action;

import ch.qos.logback.classic.Level;

/**
 * 抽象行为，可以定义一些通用方法
 * @author zj
 * @since 2019/2/28
 */
public abstract class AbstractAction implements Action {

    @Override
    public Level requestLogLevel() {
        return Level.INFO;
    }

}
