package Anonimaizer;

import akka.actor.ActorRef;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

public class ZooK {
    private ZooKeeper zooKeeper;

    private final String PATH = '/servers';

    private ActorRef storeActor;

    public void sendServers() {
        List<String> servers = zooKeeper.getChildren(PATH, this);
        this.storeActor.tell(new ServerList(servers));
    }
}
