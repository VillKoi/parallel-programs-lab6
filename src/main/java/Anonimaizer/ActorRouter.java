package Anonimaizer;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ActorRouter {
    private ActorRef storeActor;

    public void setStoreActor(ActorRef storeActor) {
        this.storeActor = storeActor;
    }

    private static final String URL_QUERY = "url";
    private static final String REQUEST_NUMBER_QUERY = "count";

    private final static int TIMEOUT = 5000;
    private final static Duration TIMEOUT_DURATION = Duration.ofMillis(TIMEOUT);

    public Flow<HttpRequest, HttpResponse, NotUsed> createFlow(ActorMaterializer materializer) {
        return Flow.of(HttpRequest.class)
                .map(request -> {
                    Query query = request.getUri().query();
                    Optional<String> url = query.get(URL_QUERY);
                    Optional<String> count = query.get(REQUEST_NUMBER_QUERY);

                    return new Pair<>(url.get(), count.get());
                })
                .mapAsync(4, param -> {
                    TestInformation information = new TestInformation(param.first(), param.second());
                    return Patterns.ask(storeActor, information, TIMEOUT_DURATION)
                            .thenCompose(response -> {
                                TestResult result = (TestResult) response;
                                if (result.isReady()) {
                                    return CompletableFuture.completedFuture(result.getAvrTime());
                                }

                                Sink<Pair<String, Integer>, CompletionStage<Long>> testSink = createFlow();
                                return Source.from(Collections.singletonList(param))
                                        .toMat(testSink, Keep.right())
                                        .run(materializer);
                            });
                }).map(param ->
                        HttpResponse.create().withEntity(HttpEntities.create(param.toString()))
                );
    }

    private Sink<Pair<String, Integer>, CompletionStage<Long>> createFlow() {
        return Flow.<Pair<String, Integer>>create()
                .mapConcat(pair ->
                        new ArrayList<>(Collections.nCopies(pair.second(), pair))
                )
                .mapAsync(10, param -> {
                    long startTime = System.currentTimeMillis();
                    AsyncHttpClient client = asyncHttpClient();
                    return client.prepareGet(param.first()).execute().toCompletableFuture().thenCompose(response -> {
                        long endTime = System.currentTimeMillis();
                        return CompletableFuture.completedFuture(new TestResult(param.first(), endTime - startTime));
                    });
                }).fold(new TestResult(), (res, element) ->
                        res.add(element)
                ).map(param -> {
                    storeActor.tell(param, ActorRef.noSender());
                    return param.getAvrTime();
                }).toMat(Sink.head(), Keep.right());
    }
}
