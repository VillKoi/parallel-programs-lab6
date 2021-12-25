package Anonimaizer;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZooK implements Watcher {
    private ZooKeeper zooKeeper;

    private final String PATH = "/servers";
    private final String SERVER = "http://localhost";

    private ActorRef storeActor;

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void setStoreActor(ActorRef storeActor) {
        this.storeActor = storeActor;
    }

    public void createConnection(String port) throws KeeperException, InterruptedException {
        try {
            this.zooKeeper.create(PATH,
                    (SERVER + port).getBytes(StandardCharsets.UTF_8),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        this.storeActor.tell(new ServerList(SERVER + ':' + port), ActorRef.noSender());
    }

    public void sendServers() throws InterruptedException, KeeperException {
        List<String> servers = zooKeeper.getChildren(PATH, this);
        System.out.println(servers);
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
