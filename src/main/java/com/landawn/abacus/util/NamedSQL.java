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

package com.landawn.abacus.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.landawn.abacus.exception.AbacusException;
import com.landawn.abacus.pool.KeyedObjectPool;
import com.landawn.abacus.pool.PoolFactory;
import com.landawn.abacus.pool.PoolableWrapper;

// TODO: Auto-generated Javadoc
/**
 * The Class NamedSQL.
 *
 * @author Haiyang Li
 * @since 0.8
 */
public final class NamedSQL {

    /** The Constant EVICT_TIME. */
    private static final int EVICT_TIME = 60 * 1000;

    /** The Constant LIVE_TIME. */
    private static final int LIVE_TIME = 24 * 60 * 60 * 1000;

    /** The Constant MAX_IDLE_TIME. */
    private static final int MAX_IDLE_TIME = 24 * 60 * 60 * 1000;

    /** The Constant namedSQLPrefixSet. */
    private static final Set<String> namedSQLPrefixSet = N.asSet(WD.INSERT, WD.SELECT, WD.UPDATE, WD.DELETE, WD.WITH);

    /** The Constant factor. */
    private static final int factor = Math.min(Math.max(1, IOUtil.MAX_MEMORY_IN_MB / 1024), 8);

    /** The Constant pool. */
    private static final KeyedObjectPool<String, PoolableWrapper<NamedSQL>> pool = PoolFactory.createKeyedObjectPool(1000 * factor, EVICT_TIME);

    /** The Constant PREFIX_OF_NAMED_PARAMETER. */
    private static final String PREFIX_OF_NAMED_PARAMETER = ":";

    /** The Constant LEFT_OF_IBATIS_NAMED_PARAMETER. */
    private static final String LEFT_OF_IBATIS_NAMED_PARAMETER = "#{";

    /** The Constant RIGHT_OF_IBATIS_NAMED_PARAMETER. */
    private static final String RIGHT_OF_IBATIS_NAMED_PARAMETER = "}";

    /** The Constant PREFIX_OF_COUCHBASE_NAMED_PARAMETER. */
    private static final String PREFIX_OF_COUCHBASE_NAMED_PARAMETER = "$";

    /** The named SQL. */
    private final String namedSQL;

    /** The pure SQL. */
    private final String pureSQL;

    /** The couchbase pure SQL. */
    private String couchbasePureSQL;

    /** The named parameters. */
    private final List<String> namedParameters;

    /** The couchbase named parameters. */
    private List<String> couchbaseNamedParameters;

    /** The parameter count. */
    private int parameterCount;

    /** The couchbase parameter count. */
    private int couchbaseParameterCount;

    /**
     * Instantiates a new named SQL.
     *
     * @param sql the sql
     */
    @SuppressWarnings({ "unchecked" })
    private NamedSQL(String sql) {
        this.namedSQL = sql.trim();

        final List<String> words = SQLParser.parse(namedSQL);

        boolean isNamedSQLPrefix = false;
        for (String word : words) {
            if (N.notNullOrEmpty(word)) {
                isNamedSQLPrefix = namedSQLPrefixSet.contains(word.toUpperCase());
                break;
            }
        }

        final List<String> namedParameterList = new ArrayList<>();

        if (isNamedSQLPrefix) {
            final StringBuilder sb = Objectory.createStringBuilder();

            for (String word : words) {
                if (word.equals(WD.QUESTION_MARK)) {
                    if (namedParameterList.size() > 0) {
                        throw new AbacusException("can't mix '?' and '#{propName}' in the same sql script");
                    }
                    parameterCount++;
                } else if (word.startsWith(LEFT_OF_IBATIS_NAMED_PARAMETER) && word.endsWith(RIGHT_OF_IBATIS_NAMED_PARAMETER)) {
                    namedParameterList.add(word.substring(2, word.length() - 1));

                    word = WD.QUESTION_MARK;
                    parameterCount++;
                } else if (word.startsWith(PREFIX_OF_NAMED_PARAMETER)) {
                    namedParameterList.add(word.substring(1));

                    word = WD.QUESTION_MARK;
                    parameterCount++;
                }

                sb.append(word);
            }

            pureSQL = sb.toString();
            namedParameters = ImmutableList.of(namedParameterList);

            Objectory.recycle(sb);
        } else {
            pureSQL = sql;
            namedParameters = ImmutableList.empty();
        }
    }

    /**
     * Parses the.
     *
     * @param sql the sql
     * @return the named SQL
     */
    public static NamedSQL parse(String sql) {
        NamedSQL result = null;
        PoolableWrapper<NamedSQL> w = pool.get(sql);

        if ((w == null) || (w.value() == null)) {
            synchronized (pool) {
                result = new NamedSQL(sql);
                pool.put(sql, PoolableWrapper.of(result, LIVE_TIME, MAX_IDLE_TIME));
            }
        } else {
            result = w.value();
        }

        return result;
    }

    /**
     * Gets the named SQL.
     *
     * @return the named SQL
     */
    public String getNamedSQL() {
        return namedSQL;
    }

    /**
     * Gets the parameterized SQL.
     *
     * @return the parameterized SQL
     */
    public String getParameterizedSQL() {
        return pureSQL;
    }

    /**
     * Gets the parameterized SQL.
     *
     * @param isForCouchbase the is for couchbase
     * @return the parameterized SQL
     */
    public String getParameterizedSQL(boolean isForCouchbase) {
        if (isForCouchbase) {
            if (N.isNullOrEmpty(couchbasePureSQL)) {
                parseForCouchbase();
            }

            return couchbasePureSQL;
        } else {
            return pureSQL;
        }
    }

    /**
     * Gets the named parameters.
     *
     * @return the named parameters
     */
    public List<String> getNamedParameters() {
        return namedParameters;
    }

    /**
     * Gets the named parameters.
     *
     * @param isForCouchbase the is for couchbase
     * @return the named parameters
     */
    public List<String> getNamedParameters(boolean isForCouchbase) {
        if (isForCouchbase) {
            if (N.isNullOrEmpty(couchbasePureSQL)) {
                parseForCouchbase();
            }

            return couchbaseNamedParameters;
        } else {
            return namedParameters;
        }
    }

    /**
     * Gets the parameter count.
     *
     * @return the parameter count
     */
    public int getParameterCount() {
        return parameterCount;
    }

    /**
     * Gets the parameter count.
     *
     * @param isForCouchbase the is for couchbase
     * @return the parameter count
     */
    public int getParameterCount(boolean isForCouchbase) {
        if (isForCouchbase) {
            if (N.isNullOrEmpty(couchbasePureSQL)) {
                parseForCouchbase();
            }

            return couchbaseParameterCount;
        } else {
            return parameterCount;
        }
    }

    /**
     * Parses the for couchbase.
     */
    private void parseForCouchbase() {
        List<String> couchbaseNamedParameterList = new ArrayList<>();

        final List<String> words = SQLParser.parse(namedSQL);

        boolean isNamedSQLPrefix = false;
        for (String word : words) {
            if (N.notNullOrEmpty(word)) {
                isNamedSQLPrefix = namedSQLPrefixSet.contains(word.toUpperCase());
                break;
            }
        }

        if (isNamedSQLPrefix) {
            final StringBuilder sb = Objectory.createStringBuilder();
            int countOfParameter = 0;

            for (String word : words) {
                if (word.equals(WD.QUESTION_MARK)) {
                    if (couchbaseNamedParameterList.size() > 0) {
                        throw new AbacusException("can't mix '?' and '#{propName}' in the same sql script");
                    }

                    countOfParameter++;
                    word = PREFIX_OF_COUCHBASE_NAMED_PARAMETER + countOfParameter;
                } else if (word.startsWith(LEFT_OF_IBATIS_NAMED_PARAMETER) && word.endsWith(RIGHT_OF_IBATIS_NAMED_PARAMETER)) {
                    couchbaseNamedParameterList.add(word.substring(2, word.length() - 1));

                    countOfParameter++;
                    word = PREFIX_OF_COUCHBASE_NAMED_PARAMETER + countOfParameter;
                } else if (word.startsWith(PREFIX_OF_NAMED_PARAMETER) || word.startsWith(PREFIX_OF_COUCHBASE_NAMED_PARAMETER)) {
                    couchbaseNamedParameterList.add(word.substring(1));

                    countOfParameter++;
                    word = PREFIX_OF_COUCHBASE_NAMED_PARAMETER + countOfParameter;
                }

                sb.append(word);
            }

            boolean isNamedParametersByNum = true;

            for (int i = 0; i < countOfParameter; i++) {
                try {
                    if (N.parseInt(couchbaseNamedParameterList.get(i)) != i + 1) {
                        isNamedParametersByNum = false;
                        break;
                    }
                } catch (Exception e) {
                    // ignore;
                    isNamedParametersByNum = false;
                    break;
                }
            }

            if (isNamedParametersByNum) {
                couchbaseNamedParameterList.clear();
            }

            couchbasePureSQL = sb.toString();
            couchbaseNamedParameters = ImmutableList.of(couchbaseNamedParameterList);
            couchbaseParameterCount = countOfParameter;

            Objectory.recycle(sb);
        } else {
            couchbasePureSQL = namedSQL;
            couchbaseNamedParameters = ImmutableList.empty();
            couchbaseParameterCount = 0;
        }
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((namedSQL == null) ? 0 : namedSQL.hashCode());

        return result;
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof NamedSQL) {
            NamedSQL other = (NamedSQL) obj;

            return N.equals(namedSQL, other.namedSQL);
        }

        return false;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "[NamedSQL] " + namedSQL + " [PureSQL] " + pureSQL;
    }
}