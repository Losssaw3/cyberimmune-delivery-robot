package ru.bardinpetr.delivery.libs.messages;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerService;
import ru.bardinpetr.delivery.libs.messages.msg.motion.GetMotionDataReply;
import ru.bardinpetr.delivery.libs.messages.msg.motion.GetRestrictionsReply;
import ru.bardinpetr.delivery.libs.messages.msg.motion.SetSpeedRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class Main {


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", "localhost:9092");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test-unit");

        var producerFactory = new MonitoredKafkaProducerFactory(configs);
        var kafkaConsumerFactory = new MonitoredKafkaConsumerFactory(configs);
        var producer = new MonitoredKafkaProducerService("test", producerFactory);
//        var consumer = new MonitoredKafkaConsumerServiceBuilder("test2")
//                .setConsumerFactory(kafkaConsumerFactory)
//                .subscribe(GetRestrictionsReply.class, i -> {
//                    System.out.printf("OK %s", i);
//                })
//                .build();
//        consumer.start();
//        consumer.run();

        var rep = new MonitoredKafkaRequesterService(
                "test",
                List.of(GetRestrictionsReply.class, GetMotionDataReply.class),
                producerFactory,
                kafkaConsumerFactory
        );

        rep.start();

//        Thread.sleep(15000);
        System.out.println("started");

//        System.out.println(rep.request("motion", new GetRestrictionsRequest()).get());
//        System.err.println(rep.request("motion", new GetMotionDataRequest()).get());
//        Thread.sleep(500);
//
        producer.sendMessage("motion", new SetSpeedRequest(10, Math.PI / 4));
//
//        Thread.sleep(5000);
//        System.err.println(rep.request("motion", new GetMotionDataRequest()).get());
//        producer.sendMessage("motion", new SetSpeedRequest(-2, Math.PI/4));
//
//
//        Thread.sleep(5000);
//        System.err.println(rep.request("motion", new GetMotionDataRequest()).get());


//1670775694
//        var sched = Executors.newSingleThreadScheduledExecutor();
//        sched.scheduleWithFixedDelay(() -> {
//                    try {
//                        System.err.println(rep.request("motion", new GetRestrictionsRequest()).get());
//                    } catch (InterruptedException | ExecutionException e) {
//                        throw new RuntimeException(e);
//                    }
//                },
//                10, 5, TimeUnit.SECONDS);

//
//        var sched = Executors.newSingleThreadScheduledExecutor();
//        sched.scheduleWithFixedDelay(() -> producer.sendMessage(
//                "authentication",
//                new CreatePINRequest("test")
//        ), 0, 10, TimeUnit.SECONDS);

//        producer.sendMessage("authentication", new CreatePINRequest("test"));

//

    }
}
