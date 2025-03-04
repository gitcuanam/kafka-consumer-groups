package com.wordpress.simplydistributed.kafka.scale;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;

public class KafkaConsumerTest implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(KafkaConsumerTest.class.getName());
    private static final String TOPIC_NAME = "test-topic";
    private static final String CONSUMER_GROUP = "test-group";
    private final AtomicBoolean CONSUMER_STOPPED = new AtomicBoolean(false);
    private KafkaConsumer<String, String> consumer = null;

    /**
     * c'tor
     */
    public KafkaConsumerTest() {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());

        this.consumer = new KafkaConsumer<>(kafkaProps);
    }

    /**
     * invoke this to stop this consumer
     */
    public void stop() {
        LOGGER.log(Level.INFO, "signalling shut down for consumer");
        if (consumer != null) {
            CONSUMER_STOPPED.set(true);
            consumer.wakeup();
        }

        LOGGER.log(Level.INFO, "initiating shut down for consumer");
    }

    @Override
    public void run() {
        consume();
    }

    /**
     * poll the topic
     */
    private void consume() {

        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        LOGGER.log(Level.INFO, "Subcribed to: {0}", TOPIC_NAME);
        try {
            while (!CONSUMER_STOPPED.get()) {
                LOGGER.log(Level.INFO, "Polling broker");
                // https://stackoverflow.com/questions/59943786/kafka-what-are-the-better-alternatives-than-poll-to-listen-to-a-topic-in-jav
                ConsumerRecords<String, String> msg = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : msg) {
                    LOGGER.log(Level.INFO, "Key: {0}", record.key());
                    LOGGER.log(Level.INFO, "Value: {0}", record.value());
                    LOGGER.log(Level.INFO, "Partition: {0}", record.partition());
                    LOGGER.log(Level.INFO, "---------------------------------------");
                }

            }
            LOGGER.log(Level.INFO, "Poll loop interrupted");
        } catch (Exception e) {
            //LOGGER.log(Level.SEVERE, e.getMessage(), e);
            if (!CONSUMER_STOPPED.get()) {
                throw e;
            }
        } finally {
            consumer.close();
            LOGGER.log(Level.INFO, "consumer shut down complete");
        }

    }

}
