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

import com.jcalvopinam.utils.Constants;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClientHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author Juan Calvopina
 */
public class ReleaseRepositoryImpl implements ReleaseRepository {

    private final Map<String, String> sqlQueries;
    private final JDBCClient dbClient;

    ReleaseRepositoryImpl(io.vertx.ext.jdbc.JDBCClient dbClient, Map<String, String> sqlQueries,
                          Handler<AsyncResult<ReleaseRepository>> readyHandler) {
        this.dbClient = new JDBCClient(dbClient);
        this.sqlQueries = sqlQueries;

        SQLClientHelper.usingConnectionSingle(this.dbClient,
                                              conn -> conn.rxExecute(sqlQueries.get(Constants.CREATE_RELEASE_TABLE))
                                                          .andThen(Single.just(this)))
                       .subscribe(SingleHelper.toObserver(readyHandler));
    }

    @Override
    public ReleaseRepository fetchAllReleases(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        dbClient.rxQuery(sqlQueries.get(Constants.GET_ALL_RELEASE))
                .map(ResultSet::getRows)
                .subscribe(SingleHelper.toObserver(resultHandler));
        return this;
    }

    @Override
    public ReleaseRepository fetchReleaseById(int id, Handler<AsyncResult<JsonObject>> resultHandler) {
        dbClient.rxQueryWithParams(sqlQueries.get(Constants.GET_RELEASE_BY_ID),
                                   new JsonArray().add(id))
                .map(result -> {
                    if (result.getNumRows() > 0) {
                        JsonObject release = result.getRows().get(0);
                        return new JsonObject().put(Constants.FOUND, true)
                                               .put(Constants.ID, release.getInteger(Constants.ID.toUpperCase()))
                                               .put(Constants.APPLICATION_NAME,
                                                    release.getString(Constants.APPLICATION_NAME.toUpperCase()))
                                               .put(Constants.VERSION,
                                                    release.getString(Constants.VERSION.toUpperCase()))
                                               .put(Constants.CONTENT,
                                                    release.getString(Constants.CONTENT.toUpperCase()))
                                               .put(Constants.RELEASE_DATE,
                                                    release.getString(Constants.RELEASE_DATE.toUpperCase()));
                    } else {
                        return new JsonObject().put(Constants.FOUND, false);
                    }
                })
                .subscribe(SingleHelper.toObserver(resultHandler));
        return this;
    }

    @Override
    public ReleaseRepository insertRelease(String applicationName, String version, String content, String releaseDate,
                                           Handler<AsyncResult<Void>> resultHandler) {
        dbClient.rxUpdateWithParams(sqlQueries.get(Constants.INSERT_RELEASE),
                                    new JsonArray().add(applicationName)
                                                   .add(version)
                                                   .add(content)
                                                   .add(LocalDate.now().toString()))
                .toCompletable()
                .subscribe(CompletableHelper.toObserver(resultHandler));
        return this;
    }

    @Override
    public ReleaseRepository updateRelease(int id, String content, Handler<AsyncResult<Void>> resultHandler) {
        dbClient.rxUpdateWithParams(sqlQueries.get(Constants.UPDATE_RELEASE),
                                    new JsonArray().add(content).add(id))
                .toCompletable()
                .subscribe(CompletableHelper.toObserver(resultHandler));
        return this;
    }

    @Override
    public ReleaseRepository deleteRelease(int id, Handler<AsyncResult<Void>> resultHandler) {
        dbClient.rxUpdateWithParams(sqlQueries.get(Constants.DELETE_RELEASE),
                                    new JsonArray().add(id))
                .toCompletable()
                .subscribe(CompletableHelper.toObserver(resultHandler));
        return this;
    }

}
