package cn.zj.basicform.mq;

import com.zj.basicform.common.property.PropertiesLoader;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author zj
 * @since 2019/2/6
 */
public class KafkaProducer<K, V> implements Producer {

    private org.apache.kafka.clients.producer.KafkaProducer<K, V> _kafkaProducer;

    public KafkaProducer(Properties properties) {
        _kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(properties);
    }

    public KafkaProducer(PropertiesLoader loader) {
        Properties p;
        try {
            p = loader.load();
        } catch (Exception e) {
            throw new RuntimeException("load properties failed when init kafka producer.");
        }
        _kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(p);
    }

    public KafkaProducer(PropertiesLoader loader, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        Properties p;
        try {
            p = loader.load();
        } catch (Exception e) {
            throw new RuntimeException("load properties failed when init kafka producer.");
        }
        _kafkaProducer = new org.apache.kafka.clients.producer.KafkaProducer<>(p, keySerializer, valueSerializer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<RecordMetadata> sendInAsync(ProducerRecord record, Callback callback) {
        return _kafkaProducer.send(record, callback);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object sendInSync(ProducerRecord record) throws ExecutionException, InterruptedException {
        return _kafkaProducer.send(record).get();
    }

    @Override
    public void close() {
        if (_kafkaProducer != null) {
            _kafkaProducer.close();
        }
    }
}
