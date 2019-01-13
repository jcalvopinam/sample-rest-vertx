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

import com.jcalvopinam.repository.reactivex.ReleaseRepository;
import com.jcalvopinam.service.HttpService;
import com.jcalvopinam.service.HttpServiceImpl;
import com.jcalvopinam.utils.Constants;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Juan Calvopina
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private static final String API_RELEASES = "/api/releases";
    private static final String API_RELEASES_ID = "/api/releases/:id";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        String serviceAddress = config()
                .getString(Constants.RELEASE_SERVICE_ADDRESS, Constants.RELEASE_SERVICE_ADDRESS);
        ReleaseRepository dbService = com.jcalvopinam.repository.ReleaseRepository
                .createProxy(vertx.getDelegate(), serviceAddress);
        Router router = initRouter(new HttpServiceImpl(dbService));
        initHttpServer(startFuture, router);
    }

    private Router initRouter(HttpService httpService) {
        Router router = Router.router(vertx);
        router.route()
              .handler(CookieHandler.create())
              .handler(BodyHandler.create())
              .handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        router.get("/").handler(context -> context.reroute(API_RELEASES));
        router.get(API_RELEASES).handler(httpService::getAllReleases);
        router.get(API_RELEASES_ID).handler(httpService::getReleaseById);

        router.post().handler(BodyHandler.create());
        router.post(API_RELEASES).handler(httpService::saveRelease);

        router.put().handler(BodyHandler.create());
        router.put(API_RELEASES_ID).handler(httpService::updateRelease);

        router.delete(API_RELEASES_ID).handler(httpService::deleteRelease);
        return router;
    }

    private void initHttpServer(Future<Void> startFuture, Router router) {
        int portNumber = config().getInteger(Constants.CONFIG_HTTP_PORT, Constants.DEFAULT_PORT);

        vertx.createHttpServer()
             .requestHandler(router)
             .rxListen(portNumber)
             .subscribe(s -> {
                 LOGGER.info("HTTP server running on port: {} ", portNumber);
                 startFuture.complete();
             }, t -> {
                 LOGGER.error("Could not start a HTTP server: {}", t.getMessage());
                 startFuture.fail(t);
             });
    }

}
