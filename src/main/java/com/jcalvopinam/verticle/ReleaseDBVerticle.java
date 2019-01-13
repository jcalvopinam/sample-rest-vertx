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

import com.jcalvopinam.repository.ReleaseRepository;
import com.jcalvopinam.utils.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Juan Calvopina
 */
public class ReleaseDBVerticle extends AbstractVerticle {

    private static final String JDBC_URL = "jdbc.url";
    private static final String JDBC_DRIVER_CLASS = "jdbc.driver.class";
    private static final String JDBC_MAX_POOL_SIZE = "jdbc.max.pool.size";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        connectToDatabase(startFuture, loadSqlQueries());
    }

    private void connectToDatabase(Future<Void> startFuture, Map<String, String> sqlQueries) {
        JDBCClient dbClient = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", config().getString(JDBC_URL))
                .put("driver_class", config().getString(JDBC_DRIVER_CLASS))
                .put("max_pool_size", config().getInteger(JDBC_MAX_POOL_SIZE)));

        ReleaseRepository.create(dbClient, sqlQueries, ready -> {
            if (ready.succeeded()) {
                ServiceBinder binder = new ServiceBinder(vertx);
                binder.setAddress(Constants.RELEASE_SERVICE_ADDRESS).register(ReleaseRepository.class, ready.result());
                startFuture.complete();
            } else {
                startFuture.fail(ready.cause());
            }
        });
    }

    private Map<String, String> loadSqlQueries() {
        HashMap<String, String> sqlQueries = new HashMap<>();
        sqlQueries.put(Constants.CREATE_RELEASE_TABLE, config().getString(Constants.CREATE_RELEASE_TABLE));
        sqlQueries.put(Constants.GET_APPLICATION_NAME, config().getString(Constants.GET_APPLICATION_NAME));
        sqlQueries.put(Constants.GET_ID_AND_APP_NAME, config().getString(Constants.GET_ID_AND_APP_NAME));
        sqlQueries.put(Constants.INSERT_RELEASE, config().getString(Constants.INSERT_RELEASE));
        sqlQueries.put(Constants.UPDATE_RELEASE, config().getString(Constants.UPDATE_RELEASE));
        sqlQueries.put(Constants.DELETE_RELEASE, config().getString(Constants.DELETE_RELEASE));
        sqlQueries.put(Constants.GET_ALL_RELEASE, config().getString(Constants.GET_ALL_RELEASE));
        sqlQueries.put(Constants.GET_RELEASE_BY_ID, config().getString(Constants.GET_RELEASE_BY_ID));
        return sqlQueries;
    }

}
