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

package com.landawn.abacus.exception;

// TODO: Auto-generated Javadoc
/**
 * The Class UncheckedException.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public class UncheckedException extends AbacusException {
    /**
     * Field serialVersionUID.
     */
    private static final long serialVersionUID = 3273172039077565878L;

    /**
     * Constructor for UncheckedIOException.
     */
    UncheckedException() {
        super();
    }

    /**
     * Constructor for UncheckedIOException.
     *
     * @param message the message
     */
    UncheckedException(String message) {
        super(message);
    }

    /**
     * Constructor for UncheckedIOException.
     *
     * @param message the message
     * @param cause the cause
     */
    public UncheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for UncheckedIOException.
     *
     * @param cause the cause
     */
    public UncheckedException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the cause of this exception.
     *
     * @return  the {@code IOException} which is the cause of this exception.
     */
    @Override
    public Throwable getCause() {
        return super.getCause();
    }
}