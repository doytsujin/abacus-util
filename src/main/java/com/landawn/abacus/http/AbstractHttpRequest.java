/*
 * Copyright (C) 2019 HaiYang Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.landawn.abacus.http;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import com.landawn.abacus.exception.UncheckedIOException;
import com.landawn.abacus.util.ContinuableFuture;
import com.landawn.abacus.util.N;

/**
 * The Class AbstractHttpRequest.
 *
 * @author Haiyang Li
 * @param <S>
 * @since 1.3
 */
abstract class AbstractHttpRequest<S extends AbstractHttpRequest<S>> {

    /** The http client. */
    final AbstractHttpClient httpClient;

    /** The http method. */
    HttpMethod httpMethod;

    /** The settings. */
    HttpSettings settings;

    /** The request. */
    Object request;

    /**
     * Instantiates a new abstract http request.
     *
     * @param httpClient
     */
    AbstractHttpRequest(AbstractHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 
     * @param user
     * @param password
     * @return
     */
    public S basicAuth(String user, Object password) {
        checkSettings();

        settings.basicAuth(user, password);

        return (S) this;
    }

    /**
     *
     * @param name
     * @param value
     * @return
     */
    public S header(String name, Object value) {
        checkSettings();

        settings.header(name, value);

        return (S) this;
    }

    /**
     *
     * @param name1
     * @param value1
     * @param name2
     * @param value2
     * @return
     */
    public S headers(String name1, Object value1, String name2, Object value2) {
        checkSettings();

        settings.headers(name1, value1, name2, value2);

        return (S) this;
    }

    /**
     *
     * @param name1
     * @param value1
     * @param name2
     * @param value2
     * @param name3
     * @param value3
     * @return
     */
    public S headers(String name1, Object value1, String name2, Object value2, String name3, Object value3) {
        checkSettings();

        settings.headers(name1, value1, name2, value2, name3, value3);

        return (S) this;
    }

    /**
     *
     * @param headers
     * @return
     */
    public S headers(Map<String, ?> headers) {
        checkSettings();

        settings.headers(headers);

        return (S) this;
    }

    /**
     *
     * @param headers
     * @return
     */
    public S headers(HttpHeaders headers) {
        checkSettings();

        settings.headers(headers);

        return (S) this;
    }

    /**
     *
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public String get() throws UncheckedIOException {
        return get(String.class);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T get(final Class<T> resultClass) throws UncheckedIOException {
        return get(resultClass, null);
    }

    /**
     *
     * @param query
     * @return
     */
    public String get(Object query) {
        return get(String.class, query);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param query
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T get(final Class<T> resultClass, final Object query) throws UncheckedIOException {
        this.httpMethod = HttpMethod.GET;
        this.request = query;

        return execute(resultClass);
    }

    /**
     *
     * @param body
     * @return
     */
    public String post(Object body) {
        return post(String.class, body);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param body
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T post(final Class<T> resultClass, final Object body) throws UncheckedIOException {
        this.httpMethod = HttpMethod.POST;
        this.request = body;

        return execute(resultClass);
    }

    /**
     *
     * @param body
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public String put(Object body) throws UncheckedIOException {
        return put(String.class, body);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param body
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T put(final Class<T> resultClass, final Object body) throws UncheckedIOException {
        this.httpMethod = HttpMethod.PUT;
        this.request = body;

        return execute(resultClass);
    }

    /**
     *
     * @param body
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public String patch(Object body) throws UncheckedIOException {
        return patch(String.class, body);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param body
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T patch(final Class<T> resultClass, final Object body) throws UncheckedIOException {
        this.httpMethod = HttpMethod.PATCH;
        this.request = body;

        return execute(resultClass);
    }

    /**
     *
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public String delete() throws UncheckedIOException {
        return delete(String.class);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T delete(final Class<T> resultClass) throws UncheckedIOException {
        return delete(resultClass, null);
    }

    /**
     *
     * @param query
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public String delete(Object query) throws UncheckedIOException {
        return delete(String.class, query);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param query
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T delete(final Class<T> resultClass, final Object query) throws UncheckedIOException {
        this.httpMethod = HttpMethod.DELETE;
        this.request = query;

        return execute(resultClass);
    }

    /**
     * 
     * @param httpMethod
     * @return
     * @throws UncheckedIOException
     */
    public String execute(final HttpMethod httpMethod) throws UncheckedIOException {
        return execute(String.class, httpMethod);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param httpMethod
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T execute(final Class<T> resultClass, final HttpMethod httpMethod) throws UncheckedIOException {
        return execute(resultClass, httpMethod, null);
    }

    /**
     * 
     * @param httpMethod
     * @param body
     * @return
     * @throws UncheckedIOException
     */
    public String execute(final HttpMethod httpMethod, final Object body) throws UncheckedIOException {
        return execute(String.class, httpMethod, body);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param httpMethod
     * @param body
     * @return
     * @throws UncheckedIOException the unchecked IO exception
     */
    public <T> T execute(final Class<T> resultClass, final HttpMethod httpMethod, final Object body) throws UncheckedIOException {
        N.checkArgNotNull(httpMethod, "httpMethod");

        this.httpMethod = httpMethod;
        this.request = body;

        return execute(resultClass);
    }

    public void execute(final File output, final HttpMethod httpMethod, final Object body) throws UncheckedIOException {
        N.checkArgNotNull(httpMethod, "httpMethod");

        this.httpMethod = httpMethod;
        this.request = body;

        httpClient.execute(output, this.httpMethod, this.request, settings);
    }

    public void execute(final OutputStream output, final HttpMethod httpMethod, final Object body) throws UncheckedIOException {
        N.checkArgNotNull(httpMethod, "httpMethod");

        this.httpMethod = httpMethod;
        this.request = body;

        httpClient.execute(output, this.httpMethod, this.request, settings);
    }

    public void execute(final Writer output, final HttpMethod httpMethod, final Object body) throws UncheckedIOException {
        N.checkArgNotNull(httpMethod, "httpMethod");

        this.httpMethod = httpMethod;
        this.request = body;

        httpClient.execute(output, this.httpMethod, this.request, settings);
    }

    /**
     *
     * @return
     */
    public ContinuableFuture<String> asyncGet() {
        return asyncGet(String.class);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @return
     */
    public <T> ContinuableFuture<T> asyncGet(final Class<T> resultClass) {
        return asyncGet(resultClass, null);
    }

    /**
     *
     * @param query
     * @return
     */
    public ContinuableFuture<String> asyncGet(Object query) {
        return asyncGet(String.class, query);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param query
     * @return
     */
    public <T> ContinuableFuture<T> asyncGet(final Class<T> resultClass, final Object query) {
        this.httpMethod = HttpMethod.GET;
        this.request = query;

        return asyncExecute(resultClass);
    }

    /**
     *
     * @param body
     * @return
     */
    public ContinuableFuture<String> asyncPost(Object body) {
        return asyncPost(String.class, body);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param body
     * @return
     */
    public <T> ContinuableFuture<T> asyncPost(final Class<T> resultClass, final Object body) {
        this.httpMethod = HttpMethod.POST;
        this.request = body;

        return asyncExecute(resultClass);
    }

    /**
     *
     * @param body
     * @return
     */
    public ContinuableFuture<String> asyncPut(Object body) {
        return asyncPut(String.class, body);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param body
     * @return
     */
    public <T> ContinuableFuture<T> asyncPut(final Class<T> resultClass, final Object body) {
        this.httpMethod = HttpMethod.PUT;
        this.request = body;

        return asyncExecute(resultClass);
    }

    /**
     *
     * @param body
     * @return
     */
    public ContinuableFuture<String> asyncPatch(Object body) {
        return asyncPatch(String.class, body);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param body
     * @return
     */
    public <T> ContinuableFuture<T> asyncPatch(final Class<T> resultClass, final Object body) {
        this.httpMethod = HttpMethod.PATCH;
        this.request = body;

        return asyncExecute(resultClass);
    }

    /**
     *
     * @return
     */
    public ContinuableFuture<String> asyncDelete() {
        return asyncDelete(String.class);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @return
     */
    public <T> ContinuableFuture<T> asyncDelete(final Class<T> resultClass) {
        return asyncDelete(resultClass, null);
    }

    /**
     *
     * @param query
     * @return
     */
    public ContinuableFuture<String> asyncDelete(Object query) {
        return asyncDelete(String.class, query);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param query
     * @return
     */
    public <T> ContinuableFuture<T> asyncDelete(final Class<T> resultClass, final Object query) {
        this.httpMethod = HttpMethod.DELETE;
        this.request = query;

        return asyncExecute(resultClass);
    }

    /**
     *
     * @return
     */
    public ContinuableFuture<String> asyncExecute(final HttpMethod httpMethod) {
        return asyncExecute(String.class, httpMethod);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @return
     */
    public <T> ContinuableFuture<T> asyncExecute(final Class<T> resultClass, final HttpMethod httpMethod) {
        return asyncExecute(resultClass, httpMethod, null);
    }

    /**
     *
     * @param body
     * @return
     */
    public ContinuableFuture<String> asyncExecute(final HttpMethod httpMethod, Object body) {
        return asyncExecute(String.class, httpMethod, body);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @param body
     * @return
     */
    public <T> ContinuableFuture<T> asyncExecute(final Class<T> resultClass, final HttpMethod httpMethod, final Object body) {
        this.httpMethod = httpMethod;
        this.request = body;

        return asyncExecute(resultClass);
    }

    public ContinuableFuture<Void> asyncExecute(final File output, final HttpMethod httpMethod, final Object body) {
        this.httpMethod = httpMethod;
        this.request = body;

        return httpClient.asyncExecute(output, this.httpMethod, this.request, settings);
    }

    public ContinuableFuture<Void> asyncExecute(final OutputStream output, final HttpMethod httpMethod, final Object body) {
        this.httpMethod = httpMethod;
        this.request = body;

        return httpClient.asyncExecute(output, this.httpMethod, this.request, settings);
    }

    public ContinuableFuture<Void> asyncExecute(final Writer output, final HttpMethod httpMethod, final Object body) {
        this.httpMethod = httpMethod;
        this.request = body;

        return httpClient.asyncExecute(output, this.httpMethod, this.request, settings);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @return
     */
    protected <T> T execute(final Class<T> resultClass) {
        if (httpMethod == null) {
            throw new RuntimeException("HTTP method is not set");
        }

        return httpClient.execute(resultClass, httpMethod, request, settings);
    }

    /**
     *
     * @param <T>
     * @param resultClass
     * @return
     */
    protected <T> ContinuableFuture<T> asyncExecute(final Class<T> resultClass) {
        if (httpMethod == null) {
            throw new RuntimeException("HTTP method is not set");
        }

        switch (httpMethod) {
            case GET:
                return httpClient.asyncGet(resultClass, request, settings);

            case POST:
                return httpClient.asyncPost(resultClass, request, settings);

            case PUT:
                return httpClient.asyncPut(resultClass, request, settings);

            case DELETE:
                return httpClient.asyncDelete(resultClass, request, settings);

            default:
                throw new RuntimeException("Unsupported HTTP method: " + httpMethod);
        }
    }

    /**
     * Check settings.
     */
    protected void checkSettings() {
        if (settings == null) {
            settings = new HttpSettings();
        }
    }
}
