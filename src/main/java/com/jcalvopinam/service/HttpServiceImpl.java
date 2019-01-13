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

package com.jcalvopinam.service;

import com.jcalvopinam.repository.reactivex.ReleaseRepository;
import com.jcalvopinam.utils.Constants;
import io.reactivex.Flowable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Juan Calvopina
 */
public class HttpServiceImpl implements HttpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServiceImpl.class);

    private static final String MESSAGE_RELEASE_WITHOUT_ID = "There is no release with ID ";

    private ReleaseRepository dbService;

    public HttpServiceImpl(ReleaseRepository dbService) {
        this.dbService = dbService;
    }

    @Override
    public void getAllReleases(RoutingContext context) {
        dbService.rxFetchAllReleases()
                 .flatMapPublisher(Flowable::fromIterable)
                 .map(this::buildRelease)
                 .collect(JsonArray::new, JsonArray::add)
                 .subscribe(rls -> successfulResponse(context, Constants.STATUS_CODE_OK, Constants.RELEASE, rls),
                            t -> failedResponse(context, Constants.INTERNAL_SERVER_ERROR, t.getMessage()));
    }

    @Override
    public void getReleaseById(RoutingContext context) {
        int id = Integer.parseInt(context.request().getParam(Constants.ID));

        dbService.rxFetchReleaseById(id)
                 .subscribe(rls -> {
                     if (rls.getBoolean(Constants.FOUND)) {
                         JsonObject payload = new JsonObject()
                                 .put(Constants.ID, rls.getInteger(Constants.ID))
                                 .put(Constants.APPLICATION_NAME, rls.getString(Constants.APPLICATION_NAME))
                                 .put(Constants.VERSION, rls.getString(Constants.VERSION))
                                 .put(Constants.CONTENT, rls.getString(Constants.CONTENT))
                                 .put(Constants.RELEASE_DATE, rls.getString(Constants.RELEASE_DATE));
                         successfulResponse(context, Constants.STATUS_CODE_OK, Constants.RELEASE, payload);
                     } else {
                         failedResponse(context, Constants.STATUS_CODE_NOT_FOUND, MESSAGE_RELEASE_WITHOUT_ID + id);
                     }
                 }, t -> failedResponse(context, Constants.INTERNAL_SERVER_ERROR, t.getMessage()));
    }

    @Override
    public void saveRelease(RoutingContext context) {
        try {
            JsonObject release = context.getBodyAsJson();

            if (isRequestValid(context, release, Constants.APPLICATION_NAME, Constants.VERSION, Constants.CONTENT)) {
                dbService.rxInsertRelease(release.getString(Constants.APPLICATION_NAME),
                                          release.getString(Constants.VERSION),
                                          release.getString(Constants.CONTENT),
                                          release.getString(Constants.RELEASE_DATE))
                         .doOnComplete(
                                 () -> LOGGER
                                         .debug("The release was created successfully!\n{}", release.encodePrettily()))
                         .subscribe(() -> successfulResponse(context, Constants.STATUS_CODE_CREATED, null, null),
                                    t -> failedResponse(context, Constants.INTERNAL_SERVER_ERROR, t.getMessage()));
            }
        } catch (Exception e) {
            failedResponse(context, Constants.STATUS_CODE_BAD_REQUEST, "Does not exist body");
        }

    }

    @Override
    public void updateRelease(RoutingContext context) {
        int id = Integer.parseInt(context.request().getParam(Constants.ID));

        try {
            JsonObject release = context.getBodyAsJson();
            dbService.rxFetchReleaseById(id)
                     .subscribe(obj -> {
                         if (obj.getBoolean(Constants.FOUND)) {

                             if (isRequestValid(context, release, Constants.CONTENT)) {
                                 dbService.rxUpdateRelease(id, release.getString(Constants.CONTENT))
                                          .doOnComplete(() -> LOGGER.debug("The release was updated successfully!\n{}",
                                                                           release.encodePrettily()))
                                          .subscribe(() -> successfulResponse(context,
                                                                              Constants.STATUS_CODE_OK,
                                                                              null,
                                                                              null),
                                                     t -> failedResponse(context,
                                                                         Constants.INTERNAL_SERVER_ERROR,
                                                                         t.getMessage()));
                             } else {
                                 failedResponse(context, Constants.STATUS_CODE_BAD_REQUEST,
                                                "The body does not contains the CONTENT attribute" + id);
                             }
                         } else {
                             failedResponse(context, Constants.STATUS_CODE_NO_CONTENT,
                                            MESSAGE_RELEASE_WITHOUT_ID + id);
                         }
                     }, t -> failedResponse(context, Constants.INTERNAL_SERVER_ERROR, t.getMessage()));
        } catch (Exception e) {
            failedResponse(context, Constants.STATUS_CODE_BAD_REQUEST, "Does not exist body");
        }
    }

    @Override
    public void deleteRelease(RoutingContext context) {
        int id = Integer.parseInt(context.request().getParam(Constants.ID));

        dbService.rxFetchReleaseById(id)
                 .subscribe(obj -> {
                     if (obj.getBoolean(Constants.FOUND)) {
                         dbService.rxDeleteRelease(id)
                                  .doOnComplete(() -> LOGGER.debug("The release was deleted successfully!"))
                                  .subscribe(() -> successfulResponse(context, Constants.STATUS_CODE_OK, null, null),
                                             t -> failedResponse(context, Constants.INTERNAL_SERVER_ERROR,
                                                                 t.getMessage()));
                     } else {
                         failedResponse(context, Constants.STATUS_CODE_NO_CONTENT, MESSAGE_RELEASE_WITHOUT_ID + id);
                     }
                 }, t -> failedResponse(context, Constants.INTERNAL_SERVER_ERROR, t.getMessage()));
    }

    private JsonObject buildRelease(JsonObject release) {
        return new JsonObject()
                .put(Constants.ID, release.getInteger(Constants.ID.toUpperCase()))
                .put(Constants.APPLICATION_NAME, release.getString(Constants.APPLICATION_NAME.toUpperCase()))
                .put(Constants.VERSION, release.getString(Constants.VERSION.toUpperCase()))
                .put(Constants.CONTENT, release.getString(Constants.CONTENT.toUpperCase()))
                .put(Constants.RELEASE_DATE, release.getString(Constants.RELEASE_DATE.toUpperCase()));
    }

    private boolean isRequestValid(RoutingContext context, JsonObject release, String... expectedKeys) {
        if (Arrays.stream(expectedKeys).allMatch(release::containsKey)) {
            return true;
        }

        String payload = release.encodePrettily();
        LOGGER.error("Bad release creation JSON payload: {} from {}", payload, context.request().remoteAddress());

        context.response().setStatusCode(Constants.STATUS_CODE_BAD_REQUEST);
        context.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF_8);
        context.response().end(new JsonObject().put(Constants.SUCCESS, false)
                                               .put(Constants.ERROR, "Bad request payload").encode());
        return false;
    }

    private void successfulResponse(RoutingContext context, int statusCode, String jsonField, Object jsonData) {
        JsonObject wrapped = new JsonObject().put(Constants.SUCCESS, true);

        if (Optional.ofNullable(jsonField).isPresent() && Optional.ofNullable(jsonData).isPresent()) {
            wrapped.put(jsonField, jsonData);
        }

        context.response()
               .setStatusCode(statusCode)
               .putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF_8)
               .end(wrapped.encode());
    }

    private void failedResponse(RoutingContext context, int statusCode, String error) {
        context.response()
               .setStatusCode(statusCode)
               .putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF_8)
               .end(new JsonObject().put(Constants.SUCCESS, false)
                                    .put(Constants.ERROR, error)
                                    .encode());
    }

}
