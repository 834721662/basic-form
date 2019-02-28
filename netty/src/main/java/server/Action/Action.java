package server.Action;

import ch.qos.logback.classic.Level;

/**
 * @author zj
 * @since 2019/2/28
 */
public interface Action {

    void doAction();

    Level requestLogLevel();

}
