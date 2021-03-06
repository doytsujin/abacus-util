/*
 * Copyright (C) 2020 HaiYang Li
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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface WebMethod {

    String httpMethod() default "";

    String path() default "";

    /**
     * Request content format: {@code Content-Type + Content-Encoding}
     * 
     * @return
     */
    ContentFormat contentFormat() default ContentFormat.NONE;

    String contentCharset() default "";

    /**
     * Response content format: {@code Accept + Accept-Encoding}
     * 
     * @return
     */
    ContentFormat acceptFormat() default ContentFormat.NONE;

    String acceptCharset() default "";

    /**
     * 
     * @return unit is milliseconds
     */
    long connectionTimeout() default -1;

    /**
     * 
     * @return unit is milliseconds
     */
    long readTimeout() default -1;

    int maxRetryTimes() default -1;

    /**
     * 
     * @return unit is milliseconds
     */
    long retryInterval() default -1;

    String[] headers() default {};
}
