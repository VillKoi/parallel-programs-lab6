package Anonimaizer;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class AnonimaizerApp {
    private final static String HOST = "localhost";

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage: AnonimaizerApp <1: input port>");
            System.exit(-1);
        }

        String port = args[0];

        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class));

        ActorRouter router = new ActorRouter();
        router.setStoreActor(storeActor);

        final ZooK zookeeper = new ZooK(storeActor, port);

        final Http http = Http.get(system);
        router.setClient(http);

        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = router.createRouter().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(HOST, Integer.parseInt(port)),
                materializer
        );

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }
}
