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

package com.jcalvopinam.repository;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.List;
import java.util.Map;

/**
 * @author Juan Calvopina
 */
@ProxyGen
@VertxGen
public interface ReleaseRepository {

    @GenIgnore
    static ReleaseRepository create(JDBCClient dbClient, Map<String, String> sqlQueries,
                                    Handler<AsyncResult<ReleaseRepository>> readyHandler) {
        return new ReleaseRepositoryImpl(dbClient, sqlQueries, readyHandler);
    }

    @GenIgnore
    static com.jcalvopinam.repository.reactivex.ReleaseRepository createProxy(Vertx vertx, String address) {
        return new com.jcalvopinam.repository.reactivex.ReleaseRepository(
                new ReleaseRepositoryVertxEBProxy(vertx, address));
    }

    @Fluent
    ReleaseRepository fetchAllReleases(Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    ReleaseRepository fetchReleaseById(int id, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    ReleaseRepository insertRelease(String applicationName, String version, String content, String releaseDate,
                                    Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    ReleaseRepository updateRelease(int id, String content, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    ReleaseRepository deleteRelease(int id, Handler<AsyncResult<Void>> resultHandler);

}
