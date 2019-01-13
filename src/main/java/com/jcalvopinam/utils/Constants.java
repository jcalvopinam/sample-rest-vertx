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

package com.jcalvopinam.utils;

/**
 * @author Juan Calvopina
 */
public final class Constants {

    public static final int STATUS_CODE_OK = 200;
    public static final int STATUS_CODE_CREATED = 201;
    public static final int STATUS_CODE_NO_CONTENT = 204;
    public static final int STATUS_CODE_BAD_REQUEST = 400;
    public static final int STATUS_CODE_NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int DEFAULT_PORT = 8080;

    public static final String APPLICATION_JSON_UTF_8 = "application/json; charset=utf-8";
    public static final String CONFIG_HTTP_PORT = "verticle.port";
    public static final String CONTENT_TYPE = "content-type";
    public static final String RELEASE_SERVICE_ADDRESS = "release.repository.address";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    public static final String RELEASE = "release";
    public static final String ID = "id";
    public static final String APPLICATION_NAME = "applicationName";
    public static final String VERSION = "version";
    public static final String CONTENT = "content";
    public static final String FOUND = "found";
    public static final String RELEASE_DATE = "releaseDate";

    public static final String CREATE_RELEASE_TABLE = "sql.create.release.table";
    public static final String GET_APPLICATION_NAME = "sql.application.name.releases";
    public static final String GET_ID_AND_APP_NAME = "sql.id.app.name.release";
    public static final String GET_RELEASE_BY_ID = "sql.get.release.by.id";
    public static final String GET_ALL_RELEASE = "sql.all.release.data";
    public static final String INSERT_RELEASE = "sql.insert.release";
    public static final String UPDATE_RELEASE = "sql.update.release";
    public static final String DELETE_RELEASE = "sql.delete.release";

    private Constants() {
    }

}
