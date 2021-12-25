package Anonimaizer;

import akka.actor.ActorRef;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZooK implements Watcher {
    private ZooKeeper zooKeeper;

    private final String PATH = "/servers";
    private final String SERVER = "http://localhost";

    private final static String ZOOK_CONNECT = "127.0.0.1:2181";
    private final static int ZOOK_TIMEOUT = 2000;

    private ActorRef storeActor;

    public ZooK() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOK_CONNECT, ZOOK_TIMEOUT, null);
    }

    public void setStoreActor(ActorRef storeActor) {
        this.storeActor = storeActor;
    }

    public void createConnection(String port) throws KeeperException, InterruptedException {
        try {
            this.zooKeeper.create(PATH + "/"  + port,
                    (SERVER + ":" + port).getBytes(StandardCharsets.UTF_8),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        this.storeActor.tell(new ServerList(SERVER + ':' + port), ActorRef.noSender());
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            System.out.println("process");
            List<String> servers = zooKeeper.getChildren(PATH, this);
            System.out.println(servers);
            System.out.println("Data"+ zooKeeper.getData(PATH, false, null));
            this.storeActor.tell(new ServerList(servers), ActorRef.noSender());
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
