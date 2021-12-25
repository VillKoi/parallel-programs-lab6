package Anonimaizer;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZooK implements Watcher {
    private ZooKeeper zooKeeper;

    private final String PATH = "/servers";
    private final String SERVER = "localhost";

    private ActorRef storeActor;

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void setStoreActor(ActorRef storeActor) {
        this.storeActor = storeActor;
    }

    public void createConnection(String port) throws KeeperException, InterruptedException {
        this.zooKeeper.create(PATH ,
                port.getBytes(StandardCharsets.UTF_8),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);
        this.storeActor.tell(SERVER + ':' + port, ActorRef.noSender());
    }

    public void sendServers() throws InterruptedException, KeeperException {
        List<String> servers = zooKeeper.getChildren(PATH, this);
        this.storeActor.tell(new ServerList(servers), ActorRef.noSender());
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            sendServers();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }




}
