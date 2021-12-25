package Anonimaizer;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.javadsl.Flow;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import static akka.actor.TypedActor.context;

public class AnonimaizerApp {
    private final static String HOST = "localhost";
    private final static int PORT = 8080;

    private final static String ZOOK_CONNECT = "localhosl:2181";
    private final static int ZOOK_TIMEOUT = 2000;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage: AnonimaizerApp <1: input port>");
            System.exit(-1);
        }

        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class));

        ActorRouter router = new ActorRouter();
        router.setStoreActor(storeActor);

        final ZooK zookeeper = new ZooK();
        zookeeper.setZooKeeper(new ZooKeeper(ZOOK_CONNECT, ZOOK_TIMEOUT, null));
        zookeeper.setStoreActor(storeActor);
        zookeeper.createConnection(args[0]);
        zookeeper.sendServers();

        final Http http = Http.get(context().system());
        router.setClient(http);

        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = router.createRouter().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(HOST, PORT),
                materializer
        );

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }
}
