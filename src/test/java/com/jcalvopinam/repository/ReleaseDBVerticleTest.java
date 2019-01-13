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

import com.jcalvopinam.repository.reactivex.ReleaseRepository;
import com.jcalvopinam.utils.Constants;
import com.jcalvopinam.verticle.ReleaseDBVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Juan Calvopina
 */
@RunWith(VertxUnitRunner.class)
public class ReleaseDBVerticleTest {

    private static final int AWAIT = 5000;
    private static final String CURRENT_DATE = LocalDate.now().toString();
    private static final String CURRENT_VERSION = "1";

    private Vertx vertx;
    private ReleaseRepository service;

    public static JsonObject getConf() {
        return new JsonObject()
                .put("jdbc.url", "jdbc:hsqldb:mem:testdb;shutdown=true")
                .put("jdbc.driver.class", "org.hsqldb.jdbcDriver")
                .put("jdbc.max.pool.size", 4)
                .put("sql.application.name.releases", "select applicationName from Release")
                .put("sql.id.app.name.release", "select id, applicationName from Release where name = ?")
                .put("sql.get.release.by.id", "select * from Release where id = ?")
                .put("sql.insert.release", "insert into Release values (NULL, ?, ?, ?, ?)")
                .put("sql.update.release", "update Release set content = ? where id = ?")
                .put("sql.delete.release", "delete from Release where id = ?")
                .put("sql.all.release.data", "select * from Release")
                .put("sql.create.release.table",
                     "create table if not exists Release (id integer identity primary key, applicationName varchar(255), version varchar(5), content clob, releaseDate varchar(10))");
    }

    @Before
    public void prepare(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new ReleaseDBVerticle(),
                             new DeploymentOptions().setConfig(getConf()),
                             context.asyncAssertSuccess(
                                     id -> service = com.jcalvopinam.repository.ReleaseRepository
                                             .createProxy(vertx, Constants.RELEASE_SERVICE_ADDRESS)));
    }

    @After
    public void finish(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testCrud(TestContext context) {
        Async async = context.async();

        service.insertRelease("Test", "1", "Some content", CURRENT_DATE,
                              context.asyncAssertSuccess(save -> {
                                  service.fetchReleaseById(0, context.asyncAssertSuccess(getOne -> {
                                      checkInsert(context, getOne);
                                      service.deleteRelease(getOne.getInteger(Constants.ID), deleteOne -> {
                                          service.fetchAllReleases(context.asyncAssertSuccess(getAllList -> {
                                              context.assertTrue(getAllList.isEmpty());
                                              async.complete();
                                          }));
                                      });
                                  }));
                              }));
        async.awaitSuccess(AWAIT);
    }

    @Test
    public void testFetchAllReleases(TestContext context) {
        Async async = context.async();

        service.insertRelease("Test 1", CURRENT_VERSION, "Some content 1", CURRENT_DATE,
                              context.asyncAssertSuccess(p1 -> {
                                  service.insertRelease("Test 2", CURRENT_VERSION, "Some content 2", CURRENT_DATE,
                                                        context.asyncAssertSuccess(p2 -> {
                                                            service.fetchAllReleases(
                                                                    context.asyncAssertSuccess(data -> {
                                                                        context.assertEquals(2, data.size());

                                                                        checkRelease(context, data, 0, "Test 1",
                                                                                     "Some content 1");
                                                                        checkRelease(context, data, 1, "Test 2",
                                                                                     "Some content 2");

                                                                        async.complete();
                                                                    }));
                                                        }));
                              }));

        async.awaitSuccess(AWAIT);
    }

    private void checkInsert(TestContext context, JsonObject getOne) {
        context.assertTrue(getOne.getBoolean(Constants.FOUND));
        context.assertTrue(getOne.containsKey(Constants.ID));
        context.assertEquals(CURRENT_VERSION, getOne.getString(Constants.VERSION));
        context.assertEquals("Some content", getOne.getString(Constants.CONTENT));
        context.assertEquals(CURRENT_DATE, getOne.getString(Constants.RELEASE_DATE));
    }

    private void checkRelease(TestContext context, List<JsonObject> data, int id, String appName, String content) {
        JsonObject release = data.get(id);
        context.assertEquals(appName, release.getString(Constants.APPLICATION_NAME.toUpperCase()));
        context.assertEquals(CURRENT_VERSION, release.getString(Constants.VERSION.toUpperCase()));
        context.assertEquals(content, release.getString(Constants.CONTENT.toUpperCase()));
        context.assertEquals(CURRENT_DATE, release.getString(Constants.RELEASE_DATE.toUpperCase()));
    }

}
