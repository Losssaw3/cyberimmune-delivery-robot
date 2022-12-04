package ru.bardinpetr.cyberimmune_delivery.monitor;

import ru.bardinpetr.cyberimmune_delivery.monitor.kafka.MonitorTopicConsumer;

public class Main {

    public static void main(String[] args) {
        var consumer = new MonitorTopicConsumer();
        consumer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
    }
}
