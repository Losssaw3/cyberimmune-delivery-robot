package ru.bardinpetr.delivery.backend.flightmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import ru.bardinpetr.delivery.libs.messages.MessageRequest;

@SpringBootApplication
public class FlightManagerApplication {


    @Qualifier("messageTemplate")
    @Autowired
    private KafkaTemplate<String, MessageRequest> messageTemplate;

//    @Qualifier("replyingMessageTemplate")
//    @Autowired
//    private ReplyingKafkaTemplate<String, MessageRequest, MessageRequest> replyingMessageTemplate;


    public static void main(String[] args) {
        SpringApplication.run(FlightManagerApplication.class, args);
    }

//    @KafkaListener(id = "main")
//    public void listen(Action1Request data) {
//        System.out.println(data);
//    }

    @Bean
    public ApplicationRunner runner() {
        return args -> {
            System.out.println("started");

            Thread.sleep(1000);

//            var msg = new Action1Request("fms", "fms", "q");
//            ProducerRecord<String, MessageRequest> rec = new ProducerRecord<>(msg.getIncomingTopic(), msg);
////            rec.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, msg.createReply().getIncomingTopic().getBytes()));
//
//            var req = replyingMessageTemplate.sendAndReceive(rec);
//
//            SendResult<String, MessageRequest> sendResult = req.getSendFuture().get(10, TimeUnit.SECONDS);
//            System.out.println("Sent ok: " + sendResult.getRecordMetadata());
//            ConsumerRecord<String, MessageRequest> consumerRecord = req.get(10, TimeUnit.SECONDS);
//            System.out.println("Return value: " + consumerRecord.value());

//            System.out.println(messageTemplate.send("request_fms_q", new Action1Request("a", "b", "c")).get());
        };
    }

    @KafkaListener(id = "server", topics = "fms_Action1Request")
    @SendTo
    public MessageRequest listen(MessageRequest in) {
        System.out.println("Server received: " + in);
        return new MessageRequest("a", "b");
    }


    //    @KafkaListener(topics = "fms_Action1Request", containerFactory = "requestListenerContainerFactory")
//    @KafkaListener(topics = "fms_Action1Request", groupId = "main", containerFactory ="repliesContainer")
//    @SendTo
//    public Message<?> listen(ConsumerRecord<String, MessageRequest> consumerRecord) {
//        return MessageBuilder.withPayload(new MessageRequest("a", "b"))
//                .build();
//    }

//    public MessageRequest Action1RequestListener(MessageRequest in) {
//        System.out.println("Server received: " + in);
//        var rep = (Action1Request.Reply) in.createReply();
//        rep.setResult(in.getQuery().repeat(3));
//        return new MessageRequest("a", "b");
//    }
//    @KafkaListener(id="fms", topics = "fms_Action1Request")
//    @SendTo
//    public Message<Action1Request.Reply> Action1RequestListener(Action1Request in) {
//        System.out.println("Server received: " + in);
//        var rep = (Action1Request.Reply) in.createReply();
//        rep.setResult(in.getQuery().repeat(3));
//        return MessageBuilder
//                .withPayload(rep)
//                .setHeader(KafkaHeaders.TOPIC, rep.getIncomingTopic())
//                .build();
//    }
}
