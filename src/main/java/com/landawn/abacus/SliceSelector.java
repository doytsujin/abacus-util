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

package com.landawn.abacus;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * It's designed to support partitioning by tables.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public interface SliceSelector {

    /**
     * Returns the new the sql for selected slice by the specified sql statement and parameters.
     *
     * @param entityName
     * @param sql
     * @param parameters
     * @param options the target data source may be specified by <code>com.landawn.abacus.util.Options.Query.QUERY_WITH_DATA_SOURCE</code>
     * @return
     */
    String select(String entityName, String sql, Object[] parameters, Map<String, Object> options);

    /**
     * Returns the new the sql for selected slice by the specified sql statement and parameters for batch operation.
     *
     * @param entityName
     * @param sql
     * @param parameters
     * @param options the target data source may be specified by <code>com.landawn.abacus.util.Options.Query.QUERY_WITH_DATA_SOURCE</code>
     * @return
     */
    String select(String entityName, String sql, List<?> parameters, Map<String, Object> options);
}
