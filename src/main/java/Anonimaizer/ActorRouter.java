package Anonimaizer;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import scala.concurrent.Future;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;

public class ActorRouter {
    private ActorRef storeActor;
    private static Http client;

    public void setStoreActor(ActorRef storeActor) {
        this.storeActor = storeActor;
    }

    public static void setClient(Http client) {
        ActorRouter.client = client;
    }

    private static final String URL_QUERY = "url";
    private static final String REQUEST_NUMBER_QUERY = "count";

    private final static int TIMEOUT = 5000;
    private final static Duration TIMEOUT_DURATION = Duration.ofMillis(TIMEOUT);


    //    создаем с помощью api route в акка http сервер который принимает два параметра, и если счетчик не равен 0,
//    то сначала получает новый урл сервера (от актора хранилища конфигурации)
//    и делает запрос к нему с аналогичными query параметрами (url, counter) но счетчиком на 1 меньше.
//    Либо осуществляет  запрос по url из параметра

    private static Route createRouter(ActorRef storeActor, ActorRef testActor) {
        return route(
                get(() -> parameter(URL_QUERY, url ->
                        parameter(REQUEST_NUMBER_QUERY, count -> {
                                    int requestNumber = Integer.parseInt(count);
                                    if (requestNumber == 0) {
                                        return completeWithFuture(makeRequest(url));
                                    }

                                    String newUrl = getNewUrl(url, requestNumber - 1);

                                    return completeWithFuture(
                                            Patterns.ask(storeActor, new RandomInt(), TIMEOUT)
                                                    .thenApply()
                                    );
                                }
                        ))));
    }


    private static CompletionStage<HttpResponse> makeRequest(String url) {
        return client.singleRequest(HttpRequest.create(url));
    }

    private static final String SERVER_URL = "localhosl:8000";

    private static String getNewUrl(String url, Integer requestNumber) {
        return Uri.create(SERVER_URL).query(Query.create(
                        new Pair[]{
                                Pair.create(URL_QUERY, url),
                                Pair.create(REQUEST_NUMBER_QUERY, requestNumber)
                        }
                )
        ).toString();
    }
}
