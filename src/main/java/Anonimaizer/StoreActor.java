package Anonimaizer;

import akka.actor.AbstractActor;

import java.util.HashMap;
import java.util.Map;

public class StoreActor extends AbstractActor {
// создаем актор хранилище конфигурации.
// Он принимает две команды
// -	список серверов (который отправит zookeeper watcher)
// -	запрос на получение случайного сервера
    private Map<String, String> storage = new HashMap<>();

    @Override
    public AbstractActor.Receive createReceive(){
        return receiveBuilder(
        ).match(
                TestInformation.class, this::setTestResult
        ).match(
                String.class, packageID -> {
                    sender().tell(getResult(packageID), self());
                }
        ).build();
    }
}
