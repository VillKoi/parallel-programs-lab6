package Anonimaizer;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreActor extends AbstractActor {
// создаем актор хранилище конфигурации.
// Он принимает две команды
// -	список серверов (который отправит zookeeper watcher)
// -	запрос на получение случайного сервера
    private List<String> servers;

    @Override
    public AbstractActor.Receive createReceive(){
        return receiveBuilder(
        ).match(
                ServerList.class, servers -> {
                    this.servers = servers.getServers();
                }
        ).match(
                Random.class, random -> {
                    sender().tell(servers.get(random.getInt(servers.size())), ActorRef.noSender());
                }
        ).build();
    }
}
