package cn.zj.basicform.mq;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author zj
 * @since 2019/2/6
 */
public interface Producer<K, V> {

    Future<RecordMetadata> sendInAsync(ProducerRecord record, Callback callback);

    Object sendInSync(ProducerRecord record) throws ExecutionException, InterruptedException;

    void close();

}
