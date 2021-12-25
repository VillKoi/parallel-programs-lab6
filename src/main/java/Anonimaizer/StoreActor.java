package Anonimaizer;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.List;

public class StoreActor extends AbstractActor {
// создаем актор хранилище конфигурации.
// Он принимает две команды
// -	список серверов (который отправит zookeeper watcher)
// -	запрос на получение случайного сервера
    private List<String> servers;

    @Override
    public AbstractActor.Receive createReceive(){
        return receiveBuilder(
        ).match(ServerList.class, servers -> {
                    this.servers = servers.getServers();
                }
        ).match(RandomInt.class, random -> {
                    getSender().tell(servers.get(random.getInt(servers.size() - 1)), ActorRef.noSender());
                }
        ).build();
    }
}
