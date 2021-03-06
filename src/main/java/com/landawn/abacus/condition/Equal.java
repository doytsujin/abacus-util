/*
 * Copyright (C) 2015 HaiYang Li
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

package com.landawn.abacus.condition;

/**
 * The Class Equal.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public class Equal extends Binary {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7021976594945773433L;

    /**
     * Instantiates a new equal.
     */
    // For Kryo
    Equal() {
    }

    /**
     * Instantiates a new equal.
     *
     * @param propName
     * @param propValue
     */
    public Equal(String propName, Object propValue) {
        super(propName, Operator.EQUAL, propValue);
    }
}
