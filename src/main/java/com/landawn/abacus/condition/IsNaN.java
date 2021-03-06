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

import com.landawn.abacus.condition.ConditionFactory.CF;

/**
 * The Class IsNaN.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public class IsNaN extends Is {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7088820715466964254L;

    /**
     * Field NAN.
     */
    static final Expression NAN = CF.expr("NAN");

    /**
     * Instantiates a new checks if is na N.
     */
    // For Kryo
    IsNaN() {
    }

    /**
     * Instantiates a new checks if is na N.
     *
     * @param propName
     */
    public IsNaN(String propName) {
        super(propName, NAN);
    }
}
