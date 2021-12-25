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
    public AbstractActor.Receive createReceive() {
        return receiveBuilder(
        ).match(ServerList.class, servers -> {
                    System.out.println(servers.getServers());
                    this.servers = servers.getServers();
                }
        ).match(RandomInt.class, random -> {
                    System.out.println(servers.toString());
                    getSender().tell(servers.get(random.getInt(servers.size())), ActorRef.noSender());
                }
        ).build();
    }
}
