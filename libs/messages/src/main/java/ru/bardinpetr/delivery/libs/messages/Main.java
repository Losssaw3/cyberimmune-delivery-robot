package ru.bardinpetr.delivery.libs.messages;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaConsumerFactory;
import ru.bardinpetr.delivery.libs.messages.kafka.consumers.MonitoredKafkaRequesterService;
import ru.bardinpetr.delivery.libs.messages.kafka.producers.MonitoredKafkaProducerFactory;
import ru.bardinpetr.delivery.libs.messages.models.motion.GetRestrictionsReply;
import ru.bardinpetr.delivery.libs.messages.models.motion.GetRestrictionsRequest;

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
//        var producer = new MonitoredKafkaProducerService("test", producerFactory);
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
                List.of(GetRestrictionsReply.class),//, GetMotionDataReply.class),
                producerFactory,
                kafkaConsumerFactory
        );

        rep.start();

        Thread.sleep(15000);
        System.out.println("started");
        var res = rep.request("motion", new GetRestrictionsRequest());


        System.out.println(res.get());

//        var res2 = rep.request("motion", new GetMotionDataRequest()).get();
//        System.out.println(res2);


//
//        var sched = Executors.newSingleThreadScheduledExecutor();
//        sched.scheduleWithFixedDelay(() -> {
//                    try {
//                        System.out.println(rep.request("test2", new ReplyableMessageRequest()).get());
//                    } catch (InterruptedException | ExecutionException e) {
//                        throw new RuntimeException(e);
//                    }
//                },
//                10, 30, TimeUnit.SECONDS);
//        consumer.start();

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
