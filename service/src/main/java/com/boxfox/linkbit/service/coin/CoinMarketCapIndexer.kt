package com.boxfox.linkbit.service.coin

import com.boxfox.vertx.data.Config
import com.boxfox.vertx.service.AsyncService
import com.mashape.unirest.http.Unirest
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.WorkerExecutor
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.json.JSONObject
import rx.Observable
import rx.Subscriber
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class CoinMarketCapIndexer(
        private val vertx: Vertx,
        private val executor: WorkerExecutor = vertx.createSharedWorkerExecutor("indexer")) {
    private val COINMARKETCAP_URL = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?limit=5000"
    private var indexing = false

    fun indexing(): Observable<HashMap<String, Double>> {
        indexing = true
        return Observable.create { scheduling(it) }
    }

    private fun scheduling(sub: Subscriber<in HashMap<String, Double>>) {
        CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS).execute {
            if (indexing) {
                executor.executeBlocking({ future: Future<Void> ->
                    sub.onNext(loadPrice())
                    scheduling(sub)
                    future.complete()
                }, {
                    if (it.failed()) {
                        sub.onError(it.cause())
                    }
                })
            } else {
                sub.onCompleted()
            }
        }
    }

    private fun loadPrice(): HashMap<String, Double> {
        val apiKey = Config.getDefaultInstance().getString("coinMarketCapApiKey")
        val priceMap = HashMap<String, Double>()
        val request = Unirest.get(COINMARKETCAP_URL).header("X-CMC_PRO_API_KEY", apiKey)
        val array = request.asJson().body.getObject().getJSONArray("data")
        array.forEach { obj ->
            val coin = obj as JSONObject
            val symbol = coin.getString("symbol")
            val price = coin.getJSONObject("quote").getJSONObject("USD").getDouble("price")
            priceMap.put(symbol, price)
        }
        return priceMap
    }

    fun stopIndexing() {
        this.indexing = false
    }

}