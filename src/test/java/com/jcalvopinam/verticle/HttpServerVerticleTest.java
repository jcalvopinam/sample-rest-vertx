/*
 * MIT License
 *
 * Copyright (c) 2019 JUAN CALVOPINA M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.jcalvopinam.verticle;

import com.jcalvopinam.repository.ReleaseDBVerticleTest;
import com.jcalvopinam.utils.Constants;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Juan Calvopina
 */
@RunWith(VertxUnitRunner.class)
public class HttpServerVerticleTest {

    private static final int AWAIT = 5000;
    private static final int DEFAULT_PORT = 8080;

    private static final String LOCALHOST = "localhost";
    private static final String API_RELEASES = "/api/releases";
    private static final String API_RELEASE_ID = "/api/releases/0";

    private Vertx vertx;
    private WebClient webClient;

    @Before
    public void prepare(TestContext context) {

        vertx = Vertx.vertx();

        vertx.deployVerticle(new ReleaseDBVerticle(),
                             new DeploymentOptions().setConfig(ReleaseDBVerticleTest.getConf()),
                             context.asyncAssertSuccess());

        vertx.deployVerticle(new HttpServerVerticle(), context.asyncAssertSuccess());

        webClient = WebClient.create(vertx, new WebClientOptions().setDefaultHost(LOCALHOST)
                                                                  .setDefaultPort(DEFAULT_PORT));
    }

    @After
    public void finish(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testApi(TestContext context) {
        Async async = context.async();

        Future<String> future = Future.future();
        webClient.get("/")
                 .send(response -> {
                     if (response.succeeded()) {
                         future.complete();
                     } else {
                         context.fail(response.cause());
                     }
                 });

        Future<JsonObject> postRequest = Future.future();
        future.compose(handler -> webClient.post(API_RELEASES)
                                           .as(BodyCodec.jsonObject())
                                           .sendJsonObject(createRelease(),
                                                           response -> buildResponse(context, postRequest, response)),
                       postRequest);

        Future<JsonObject> getRequest = Future.future();
        postRequest.compose(h -> webClient.get(API_RELEASES)
                                          .as(BodyCodec.jsonObject())
                                          .send(response -> buildResponse(context, getRequest, response)), getRequest);

        Future<JsonObject> putRequest = Future.future();
        getRequest.compose(jsonObject -> {
            JsonArray array = jsonObject.getJsonArray(Constants.RELEASE);
            Integer id = array.getJsonObject(0).getInteger(Constants.ID);
            context.assertEquals(1, array.size());
            context.assertEquals(0, id);

            webClient.put(API_RELEASE_ID)
                     .as(BodyCodec.jsonObject())
                     .sendJsonObject(new JsonObject().put(Constants.ID, id)
                                                     .put(Constants.CONTENT, "The content was updated!"),
                                     response -> buildResponse(context, putRequest, response));
        }, putRequest);

        Future<JsonObject> deleteRequest = Future.future();
        putRequest.compose(jsonObject -> {
            context.assertTrue(jsonObject.getBoolean(Constants.SUCCESS));

            webClient.delete(API_RELEASE_ID)
                     .as(BodyCodec.jsonObject())
                     .send(response -> buildResponse(context, deleteRequest, response));
        }, deleteRequest);

        deleteRequest.compose(response -> {
            context.assertTrue(response.getBoolean(Constants.SUCCESS));
            async.complete();
        }, Future.failedFuture(Constants.ERROR));

        async.awaitSuccess(AWAIT);
    }

    private JsonObject createRelease() {
        return new JsonObject()
                .put(Constants.APPLICATION_NAME, "Sample")
                .put(Constants.VERSION, "1")
                .put(Constants.CONTENT, "Content releases");
    }

    private void buildResponse(TestContext context, Future<JsonObject> actionRequest,
                               AsyncResult<HttpResponse<JsonObject>> response) {
        if (response.succeeded()) {
            actionRequest.complete(response.result()
                                           .body());
        } else {
            context.fail(response.cause());
        }
    }

}