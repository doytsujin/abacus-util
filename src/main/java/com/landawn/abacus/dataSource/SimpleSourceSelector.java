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

package com.landawn.abacus.dataSource;

import java.util.List;
import java.util.Map;

import com.landawn.abacus.DataSource;
import com.landawn.abacus.DataSourceManager;
import com.landawn.abacus.DataSourceSelector;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleSourceSelector.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public class SimpleSourceSelector implements DataSourceSelector {

    /** The Constant QUERY_WITH_DATA_SOURCE. */
    private static final String QUERY_WITH_DATA_SOURCE = "queryWithDataSource";

    /**
     *
     * @param dataSourceManager
     * @param entityName
     * @param sql
     * @param parameters
     * @param options
     * @return
     */
    @Override
    public DataSource select(DataSourceManager dataSourceManager, String entityName, String sql, Object[] parameters, Map<String, Object> options) {
        return getDataSource(dataSourceManager, options);
    }

    /**
     *
     * @param dataSourceManager
     * @param entityName
     * @param sql
     * @param parameters
     * @param options
     * @return
     */
    @Override
    public DataSource select(DataSourceManager dataSourceManager, String entityName, String sql, List<?> parameters, Map<String, Object> options) {
        return getDataSource(dataSourceManager, options);
    }

    /**
     * Gets the data source.
     *
     * @param dataSourceManager
     * @param options
     * @return
     */
    private DataSource getDataSource(DataSourceManager dataSourceManager, Map<String, Object> options) {
        String dataSourceName = (options == null) ? null : (String) options.get(QUERY_WITH_DATA_SOURCE);

        DataSource ds = (dataSourceName == null) ? dataSourceManager.getPrimaryDataSource() : dataSourceManager.getActiveDataSource(dataSourceName);

        if (ds == null) {
            throw new IllegalArgumentException("No data source is available with name: " + dataSourceName);
        }

        return ds;
    }
}
