/*
 * Copyright (c) 2015, Haiyang Li.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.landawn.abacus.util;

import static com.landawn.abacus.util.WD._PARENTHESES_L;
import static com.landawn.abacus.util.WD._PARENTHESES_R;
import static com.landawn.abacus.util.WD._SPACE;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.landawn.abacus.DirtyMarker;
import com.landawn.abacus.annotation.Beta;
import com.landawn.abacus.annotation.Internal;
import com.landawn.abacus.annotation.NonUpdatable;
import com.landawn.abacus.annotation.NotColumn;
import com.landawn.abacus.annotation.ReadOnly;
import com.landawn.abacus.annotation.ReadOnlyId;
import com.landawn.abacus.condition.Between;
import com.landawn.abacus.condition.Binary;
import com.landawn.abacus.condition.Cell;
import com.landawn.abacus.condition.Clause;
import com.landawn.abacus.condition.Condition;
import com.landawn.abacus.condition.ConditionFactory.CF;
import com.landawn.abacus.condition.Criteria;
import com.landawn.abacus.condition.Expression;
import com.landawn.abacus.condition.Having;
import com.landawn.abacus.condition.In;
import com.landawn.abacus.condition.InSubQuery;
import com.landawn.abacus.condition.Join;
import com.landawn.abacus.condition.Junction;
import com.landawn.abacus.condition.Limit;
import com.landawn.abacus.condition.SubQuery;
import com.landawn.abacus.condition.Where;
import com.landawn.abacus.core.DirtyMarkerUtil;
import com.landawn.abacus.logging.Logger;
import com.landawn.abacus.logging.LoggerFactory;
import com.landawn.abacus.parser.ParserUtil;
import com.landawn.abacus.parser.ParserUtil.EntityInfo;
import com.landawn.abacus.parser.ParserUtil.PropInfo;
import com.landawn.abacus.type.Type;
import com.landawn.abacus.util.Tuple.Tuple3;
import com.landawn.abacus.util.u.Optional;

/**
 * It's easier to write/maintain the sql by <code>SQLBuilder</code> and more efficient, comparing to write sql in plain text.
 * <br>The <code>sql()</code> or <code>pair()</code> method must be called to release resources.
 * <br />Here is a sample:
 * <p>
 * String sql = NE.insert("gui", "firstName", "lastName").into("account").sql();
 * <br />// SQL: INSERT INTO account (gui, first_name, last_name) VALUES (:gui, :firstName, :lastName)
 * </p>
 *
 * The {@code tableName} will NOT be formalized.
 * <li>{@code select(...).from(String tableName).where(...)}</li>
 * <li>{@code insert(...).into(String tableName).values(...)}</li>
 * <li>{@code update(String tableName).set(...).where(...)}</li>
 * <li>{@code deleteFrom(String tableName).where(...)}</li>
 *
 * <br />
 *
 * @author Haiyang Li
 * @see {@link com.landawn.abacus.annotation.ReadOnly}
 * @see {@link com.landawn.abacus.annotation.ReadOnlyId}
 * @see {@link com.landawn.abacus.annotation.NonUpdatable}
 * @see {@link com.landawn.abacus.annotation.Transient}
 * @see {@link com.landawn.abacus.annotation.Table}
 * @see {@link com.landawn.abacus.annotation.Column}
 * @since 0.8
 */
@SuppressWarnings("deprecation")
public abstract class SQLBuilder {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(SQLBuilder.class);

    /** The Constant ALL. */
    public static final String ALL = WD.ALL;

    /** The Constant TOP. */
    public static final String TOP = WD.TOP;

    /** The Constant UNIQUE. */
    public static final String UNIQUE = WD.UNIQUE;

    /** The Constant DISTINCT. */
    public static final String DISTINCT = WD.DISTINCT;

    /** The Constant DISTINCTROW. */
    public static final String DISTINCTROW = WD.DISTINCTROW;

    /** The Constant ASTERISK. */
    public static final String ASTERISK = WD.ASTERISK;

    /** The Constant COUNT_ALL. */
    public static final String COUNT_ALL = "count(*)";

    /** The Constant _1. */
    public static final String _1 = "1";

    /** The Constant _1_list. */
    public static final List<String> _1_list = ImmutableList.of(_1);

    /** The Constant _INSERT. */
    static final char[] _INSERT = WD.INSERT.toCharArray();

    /** The Constant _SPACE_INSERT_SPACE. */
    static final char[] _SPACE_INSERT_SPACE = (WD.SPACE + WD.INSERT + WD.SPACE).toCharArray();

    /** The Constant _INTO. */
    static final char[] _INTO = WD.INTO.toCharArray();

    /** The Constant _SPACE_INTO_SPACE. */
    static final char[] _SPACE_INTO_SPACE = (WD.SPACE + WD.INTO + WD.SPACE).toCharArray();

    /** The Constant _VALUES. */
    static final char[] _VALUES = WD.VALUES.toCharArray();

    /** The Constant _SPACE_VALUES_SPACE. */
    static final char[] _SPACE_VALUES_SPACE = (WD.SPACE + WD.VALUES + WD.SPACE).toCharArray();

    /** The Constant _SELECT. */
    static final char[] _SELECT = WD.SELECT.toCharArray();

    /** The Constant _SPACE_SELECT_SPACE. */
    static final char[] _SPACE_SELECT_SPACE = (WD.SPACE + WD.SELECT + WD.SPACE).toCharArray();

    /** The Constant _FROM. */
    static final char[] _FROM = WD.FROM.toCharArray();

    /** The Constant _SPACE_FROM_SPACE. */
    static final char[] _SPACE_FROM_SPACE = (WD.SPACE + WD.FROM + WD.SPACE).toCharArray();

    /** The Constant _UPDATE. */
    static final char[] _UPDATE = WD.UPDATE.toCharArray();

    /** The Constant _SPACE_UPDATE_SPACE. */
    static final char[] _SPACE_UPDATE_SPACE = (WD.SPACE + WD.UPDATE + WD.SPACE).toCharArray();

    /** The Constant _SET. */
    static final char[] _SET = WD.SET.toCharArray();

    /** The Constant _SPACE_SET_SPACE. */
    static final char[] _SPACE_SET_SPACE = (WD.SPACE + WD.SET + WD.SPACE).toCharArray();

    /** The Constant _DELETE. */
    static final char[] _DELETE = WD.DELETE.toCharArray();

    /** The Constant _SPACE_DELETE_SPACE. */
    static final char[] _SPACE_DELETE_SPACE = (WD.SPACE + WD.DELETE + WD.SPACE).toCharArray();

    /** The Constant _JOIN. */
    static final char[] _JOIN = WD.JOIN.toCharArray();

    /** The Constant _SPACE_JOIN_SPACE. */
    static final char[] _SPACE_JOIN_SPACE = (WD.SPACE + WD.JOIN + WD.SPACE).toCharArray();

    /** The Constant _LEFT_JOIN. */
    static final char[] _LEFT_JOIN = WD.LEFT_JOIN.toCharArray();

    /** The Constant _SPACE_LEFT_JOIN_SPACE. */
    static final char[] _SPACE_LEFT_JOIN_SPACE = (WD.SPACE + WD.LEFT_JOIN + WD.SPACE).toCharArray();

    /** The Constant _RIGHT_JOIN. */
    static final char[] _RIGHT_JOIN = WD.RIGHT_JOIN.toCharArray();

    /** The Constant _SPACE_RIGHT_JOIN_SPACE. */
    static final char[] _SPACE_RIGHT_JOIN_SPACE = (WD.SPACE + WD.RIGHT_JOIN + WD.SPACE).toCharArray();

    /** The Constant _FULL_JOIN. */
    static final char[] _FULL_JOIN = WD.FULL_JOIN.toCharArray();

    /** The Constant _SPACE_FULL_JOIN_SPACE. */
    static final char[] _SPACE_FULL_JOIN_SPACE = (WD.SPACE + WD.FULL_JOIN + WD.SPACE).toCharArray();

    /** The Constant _CROSS_JOIN. */
    static final char[] _CROSS_JOIN = WD.CROSS_JOIN.toCharArray();

    /** The Constant _SPACE_CROSS_JOIN_SPACE. */
    static final char[] _SPACE_CROSS_JOIN_SPACE = (WD.SPACE + WD.CROSS_JOIN + WD.SPACE).toCharArray();

    /** The Constant _INNER_JOIN. */
    static final char[] _INNER_JOIN = WD.INNER_JOIN.toCharArray();

    /** The Constant _SPACE_INNER_JOIN_SPACE. */
    static final char[] _SPACE_INNER_JOIN_SPACE = (WD.SPACE + WD.INNER_JOIN + WD.SPACE).toCharArray();

    /** The Constant _NATURAL_JOIN. */
    static final char[] _NATURAL_JOIN = WD.NATURAL_JOIN.toCharArray();

    /** The Constant _SPACE_NATURAL_JOIN_SPACE. */
    static final char[] _SPACE_NATURAL_JOIN_SPACE = (WD.SPACE + WD.NATURAL_JOIN + WD.SPACE).toCharArray();

    /** The Constant _ON. */
    static final char[] _ON = WD.ON.toCharArray();

    /** The Constant _SPACE_ON_SPACE. */
    static final char[] _SPACE_ON_SPACE = (WD.SPACE + WD.ON + WD.SPACE).toCharArray();

    /** The Constant _USING. */
    static final char[] _USING = WD.USING.toCharArray();

    /** The Constant _SPACE_USING_SPACE. */
    static final char[] _SPACE_USING_SPACE = (WD.SPACE + WD.USING + WD.SPACE).toCharArray();

    /** The Constant _WHERE. */
    static final char[] _WHERE = WD.WHERE.toCharArray();

    /** The Constant _SPACE_WHERE_SPACE. */
    static final char[] _SPACE_WHERE_SPACE = (WD.SPACE + WD.WHERE + WD.SPACE).toCharArray();

    /** The Constant _GROUP_BY. */
    static final char[] _GROUP_BY = WD.GROUP_BY.toCharArray();

    /** The Constant _SPACE_GROUP_BY_SPACE. */
    static final char[] _SPACE_GROUP_BY_SPACE = (WD.SPACE + WD.GROUP_BY + WD.SPACE).toCharArray();

    /** The Constant _HAVING. */
    static final char[] _HAVING = WD.HAVING.toCharArray();

    /** The Constant _SPACE_HAVING_SPACE. */
    static final char[] _SPACE_HAVING_SPACE = (WD.SPACE + WD.HAVING + WD.SPACE).toCharArray();

    /** The Constant _ORDER_BY. */
    static final char[] _ORDER_BY = WD.ORDER_BY.toCharArray();

    /** The Constant _SPACE_ORDER_BY_SPACE. */
    static final char[] _SPACE_ORDER_BY_SPACE = (WD.SPACE + WD.ORDER_BY + WD.SPACE).toCharArray();

    /** The Constant _LIMIT. */
    static final char[] _LIMIT = (WD.SPACE + WD.LIMIT + WD.SPACE).toCharArray();

    /** The Constant _SPACE_LIMIT_SPACE. */
    static final char[] _SPACE_LIMIT_SPACE = (WD.SPACE + WD.LIMIT + WD.SPACE).toCharArray();

    /** The Constant _OFFSET. */
    static final char[] _OFFSET = WD.OFFSET.toCharArray();

    /** The Constant _SPACE_OFFSET_SPACE. */
    static final char[] _SPACE_OFFSET_SPACE = (WD.SPACE + WD.OFFSET + WD.SPACE).toCharArray();

    /** The Constant _AND. */
    static final char[] _AND = WD.AND.toCharArray();

    /** The Constant _SPACE_AND_SPACE. */
    static final char[] _SPACE_AND_SPACE = (WD.SPACE + WD.AND + WD.SPACE).toCharArray();

    /** The Constant _OR. */
    static final char[] _OR = WD.OR.toCharArray();

    /** The Constant _SPACE_OR_SPACE. */
    static final char[] _SPACE_OR_SPACE = (WD.SPACE + WD.OR + WD.SPACE).toCharArray();

    /** The Constant _UNION. */
    static final char[] _UNION = WD.UNION.toCharArray();

    /** The Constant _SPACE_UNION_SPACE. */
    static final char[] _SPACE_UNION_SPACE = (WD.SPACE + WD.UNION + WD.SPACE).toCharArray();

    /** The Constant _UNION_ALL. */
    static final char[] _UNION_ALL = WD.UNION_ALL.toCharArray();

    /** The Constant _SPACE_UNION_ALL_SPACE. */
    static final char[] _SPACE_UNION_ALL_SPACE = (WD.SPACE + WD.UNION_ALL + WD.SPACE).toCharArray();

    /** The Constant _INTERSECT. */
    static final char[] _INTERSECT = WD.INTERSECT.toCharArray();

    /** The Constant _SPACE_INTERSECT_SPACE. */
    static final char[] _SPACE_INTERSECT_SPACE = (WD.SPACE + WD.INTERSECT + WD.SPACE).toCharArray();

    /** The Constant _EXCEPT. */
    static final char[] _EXCEPT = WD.EXCEPT.toCharArray();

    /** The Constant _SPACE_EXCEPT_SPACE. */
    static final char[] _SPACE_EXCEPT_SPACE = (WD.SPACE + WD.EXCEPT + WD.SPACE).toCharArray();

    /** The Constant _EXCEPT2. */
    static final char[] _EXCEPT2 = WD.EXCEPT2.toCharArray();

    /** The Constant _SPACE_EXCEPT2_SPACE. */
    static final char[] _SPACE_EXCEPT2_SPACE = (WD.SPACE + WD.EXCEPT2 + WD.SPACE).toCharArray();

    /** The Constant _AS. */
    static final char[] _AS = WD.AS.toCharArray();

    /** The Constant _SPACE_AS_SPACE. */
    static final char[] _SPACE_AS_SPACE = (WD.SPACE + WD.AS + WD.SPACE).toCharArray();

    /** The Constant _SPACE_EQUAL_SPACE. */
    static final char[] _SPACE_EQUAL_SPACE = (WD.SPACE + WD.EQUAL + WD.SPACE).toCharArray();

    /** The Constant _SPACE_FOR_UPDATE. */
    static final char[] _SPACE_FOR_UPDATE = (WD.SPACE + WD.FOR_UPDATE).toCharArray();

    /** The Constant _COMMA_SPACE. */
    static final char[] _COMMA_SPACE = WD.COMMA_SPACE.toCharArray();

    /** The Constant SPACE_AS_SPACE. */
    static final String SPACE_AS_SPACE = WD.SPACE + WD.AS + WD.SPACE;

    private static final Set<String> sqlKeyWords = new HashSet<>(1024);

    static {
        final Field[] fields = WD.class.getDeclaredFields();
        int m = 0;

        for (Field field : fields) {
            m = field.getModifiers();

            if (Modifier.isPublic(m) && Modifier.isStatic(m) && Modifier.isFinal(m) && field.getType().equals(String.class)) {
                try {
                    final String value = (String) field.get(null);

                    for (String e : StringUtil.split(value, ' ', true)) {
                        sqlKeyWords.add(e);
                        sqlKeyWords.add(e.toUpperCase());
                        sqlKeyWords.add(e.toLowerCase());
                    }
                } catch (Exception e) {
                    // ignore, should never happen.
                }
            }
        }
    }

    /** The Constant entityTablePropColumnNameMap. */
    private static final Map<Class<?>, Map<NamingPolicy, ImmutableMap<String, String>>> entityTablePropColumnNameMap = new ObjectPool<>(N.POOL_SIZE);

    /** The Constant subEntityPropNamesPool. */
    private static final Map<Class<?>, ImmutableSet<String>> subEntityPropNamesPool = new ObjectPool<>(N.POOL_SIZE);

    /** The Constant nonSubEntityPropNamesPool. */
    private static final Map<Class<?>, ImmutableSet<String>> nonSubEntityPropNamesPool = new ObjectPool<>(N.POOL_SIZE);

    /** The Constant defaultPropNamesPool. */
    private static final Map<Class<?>, Set<String>[]> defaultPropNamesPool = new ObjectPool<>(N.POOL_SIZE);

    private static final Map<NamingPolicy, Map<Class<?>, String>> fullSelectPartsPool = new HashMap<>(NamingPolicy.values().length);

    static {
        for (NamingPolicy np : NamingPolicy.values()) {
            fullSelectPartsPool.put(np, new ConcurrentHashMap<Class<?>, String>());
        }
    }

    /** The Constant tableDeleteFrom. */
    private static final Map<String, char[]> tableDeleteFrom = new ConcurrentHashMap<>();

    /** The Constant classTableNameMap. */
    private static final Map<Class<?>, String[]> classTableNameMap = new ConcurrentHashMap<>();

    /** The Constant activeStringBuilderCounter. */
    private static final AtomicInteger activeStringBuilderCounter = new AtomicInteger();

    /** The naming policy. */
    private final NamingPolicy namingPolicy;

    /** The sql policy. */
    private final SQLPolicy sqlPolicy;

    /** The parameters. */
    private final List<Object> parameters = new ArrayList<>();

    /** The sb. */
    private StringBuilder sb;

    /** The entity class. */
    private Class<?> entityClass;

    /** The op. */
    private OperationType op;

    /** The table name. */
    private String tableName;

    /** The predicates. */
    private String predicates;

    /** The column names. */
    private String[] columnNames;

    /** The column name list. */
    private Collection<String> columnNameList;

    /** The column aliases. */
    private Map<String, String> columnAliases;

    private List<Tuple3<Class<?>, String, Set<String>>> multiSelects;

    private Map<String, Map<String, String>> aliasPropColumnNameMap;

    /** The props. */
    private Map<String, Object> props;

    /** The props list. */
    private Collection<Map<String, Object>> propsList;

    /**
     * Instantiates a new SQL builder.
     *
     * @param namingPolicy
     * @param sqlPolicy
     */
    SQLBuilder(final NamingPolicy namingPolicy, final SQLPolicy sqlPolicy) {
        if (activeStringBuilderCounter.incrementAndGet() > 1024) {
            logger.error("Too many(" + activeStringBuilderCounter.get()
                    + ") StringBuilder instances are created in SQLBuilder. The method sql()/pair() must be called to release resources and close SQLBuilder");
        }

        this.sb = Objectory.createStringBuilder();

        this.namingPolicy = namingPolicy == null ? NamingPolicy.LOWER_CASE_WITH_UNDERSCORE : namingPolicy;
        this.sqlPolicy = sqlPolicy == null ? SQLPolicy.SQL : sqlPolicy;
    }

    /**
     * Register non sub entity prop names.
     *
     * @param entityClass
     * @param nonSubEntityPropNames
     * @deprecated annotated the entity field or get/set method get {@code @Column}
     */
    @Deprecated
    public static void registerNonSubEntityPropNames(final Class<?> entityClass, final Collection<String> nonSubEntityPropNames) {
        final ImmutableSet<String> set = ImmutableSet.copyOf(nonSubEntityPropNames);

        synchronized (entityClass) {
            nonSubEntityPropNamesPool.put(entityClass, set);

            subEntityPropNamesPool.remove(entityClass);
            defaultPropNamesPool.remove(entityClass);

            entityTablePropColumnNameMap.remove(entityClass);

            for (NamingPolicy np : NamingPolicy.values()) {
                fullSelectPartsPool.get(np).remove(entityClass);
            }
        }
    }

    /**
     * Gets the table name.
     *
     * @param entityClass
     * @param namingPolicy
     * @return
     */
    static String getTableName(final Class<?> entityClass, final NamingPolicy namingPolicy) {
        String[] entityTableNames = classTableNameMap.get(entityClass);

        if (entityTableNames == null) {
            final EntityInfo entityInfo = ParserUtil.getEntityInfo(entityClass);

            if (entityInfo.tableName.isPresent()) {
                entityTableNames = Array.repeat(entityInfo.tableName.get(), 3);
            } else {
                final String simpleClassName = ClassUtil.getSimpleClassName(entityClass);
                entityTableNames = new String[] { ClassUtil.toLowerCaseWithUnderscore(simpleClassName), ClassUtil.toUpperCaseWithUnderscore(simpleClassName),
                        ClassUtil.toCamelCase(simpleClassName) };
            }

            classTableNameMap.put(entityClass, entityTableNames);
        }

        switch (namingPolicy) {
            case LOWER_CASE_WITH_UNDERSCORE:
                return entityTableNames[0];

            case UPPER_CASE_WITH_UNDERSCORE:
                return entityTableNames[1];

            default:
                return entityTableNames[2];
        }
    }

    /**
     * Gets the insert prop names by class.
     *
     * @param entity
     * @param excludedPropNames
     * @return
     */
    @Internal
    public static Collection<String> getInsertPropNames(final Object entity, final Set<String> excludedPropNames) {
        final Class<?> entityClass = entity.getClass();
        final boolean isDirtyMarker = ClassUtil.isDirtyMarker(entityClass);

        if (isDirtyMarker) {
            final Collection<String> signedPropNames = ((DirtyMarker) entity).signedPropNames();

            if (N.isNullOrEmpty(excludedPropNames)) {
                return signedPropNames;
            } else {
                final List<String> tmp = new ArrayList<>(signedPropNames);
                tmp.removeAll(excludedPropNames);
                return tmp;
            }
        } else {
            final Collection<String>[] val = loadPropNamesByClass(entityClass);

            if (N.isNullOrEmpty(excludedPropNames)) {
                final Collection<String> idPropNames = ClassUtil.getIdFieldNames(entityClass);

                if (N.isNullOrEmpty(idPropNames)) {
                    return val[2];
                } else {
                    final EntityInfo entityInfo = ParserUtil.getEntityInfo(entityClass);

                    for (String idPropName : idPropNames) {
                        if (!isDefaultIdPropValue(entityInfo.getPropInfo(idPropName))) {
                            return val[2];
                        }
                    }

                    return val[3];
                }
            } else {
                final List<String> tmp = new ArrayList<>(val[2]);
                tmp.removeAll(excludedPropNames);
                return tmp;
            }
        }
    }

    /**
     * Gets the insert prop names by class.
     *
     * @param entityClass
     * @param excludedPropNames
     * @return
     */
    @Internal
    public static Collection<String> getInsertPropNamesByClass(final Class<?> entityClass, final Set<String> excludedPropNames) {
        final Collection<String>[] val = loadPropNamesByClass(entityClass);
        final Collection<String> propNames = val[2];

        if (N.isNullOrEmpty(excludedPropNames)) {
            return propNames;
        } else {
            final List<String> tmp = new ArrayList<>(propNames);
            tmp.removeAll(excludedPropNames);
            return tmp;
        }
    }

    /**
     * Gets the select prop names by class.
     *
     * @param entityClass
     * @param includeSubEntityProperties
     * @param excludedPropNames
     * @return
     */
    @Internal
    public static Collection<String> getSelectPropNamesByClass(final Class<?> entityClass, final boolean includeSubEntityProperties,
            final Set<String> excludedPropNames) {
        final Collection<String>[] val = loadPropNamesByClass(entityClass);
        final Collection<String> propNames = includeSubEntityProperties ? val[0] : val[1];

        if (N.isNullOrEmpty(excludedPropNames)) {
            return propNames;
        } else {
            final List<String> tmp = new ArrayList<>(propNames);
            tmp.removeAll(excludedPropNames);
            return tmp;
        }
    }

    /**
     * Checks if is default id prop value.
     *
     * @param propValue
     * @return true, if is default id prop value
     */
    @Internal
    static boolean isDefaultIdPropValue(final Object propValue) {
        return (propValue == null) || (propValue instanceof Number && (((Number) propValue).longValue() == 0));
    }

    /**
     * Gets the update prop names by class.
     *
     * @param entityClass
     * @param excludedPropNames
     * @return
     */
    @Internal
    public static Collection<String> getUpdatePropNamesByClass(final Class<?> entityClass, final Set<String> excludedPropNames) {
        final Collection<String>[] val = loadPropNamesByClass(entityClass);
        final Collection<String> propNames = val[4];

        if (N.isNullOrEmpty(excludedPropNames)) {
            return propNames;
        } else {
            final List<String> tmp = new ArrayList<>(propNames);
            tmp.removeAll(excludedPropNames);
            return tmp;
        }
    }

    /**
     * Load prop names by class.
     *
     * @param entityClass
     * @return
     */
    static Set<String>[] loadPropNamesByClass(final Class<?> entityClass) {
        Set<String>[] val = defaultPropNamesPool.get(entityClass);

        if (val == null) {
            synchronized (entityClass) {
                final Set<String> entityPropNames = N.newLinkedHashSet(ClassUtil.getPropNameList(entityClass));
                final Set<String> subEntityPropNames = getSubEntityPropNames(entityClass);

                if (N.notNullOrEmpty(subEntityPropNames)) {
                    entityPropNames.removeAll(subEntityPropNames);
                }

                val = new Set[5];
                val[0] = N.newLinkedHashSet(entityPropNames);
                val[1] = N.newLinkedHashSet(entityPropNames);
                val[2] = N.newLinkedHashSet(entityPropNames);
                val[3] = N.newLinkedHashSet(entityPropNames);
                val[4] = N.newLinkedHashSet(entityPropNames);

                final EntityInfo entityInfo = ParserUtil.getEntityInfo(entityClass);
                Class<?> subEntityClass = null;
                Set<String> subEntityPropNameList = null;

                for (String subEntityPropName : subEntityPropNames) {
                    PropInfo propInfo = entityInfo.getPropInfo(subEntityPropName);
                    subEntityClass = (propInfo.type.isCollection() ? propInfo.type.getElementType() : propInfo.type).clazz();

                    subEntityPropNameList = N.newLinkedHashSet(ClassUtil.getPropNameList(subEntityClass));
                    subEntityPropNameList.removeAll(getSubEntityPropNames(subEntityClass));

                    for (String pn : subEntityPropNameList) {
                        val[0].add(StringUtil.concat(subEntityPropName, WD.PERIOD, pn));
                    }
                }

                final Set<String> nonUpdatableNonWritablePropNames = N.newHashSet();
                final Set<String> nonUpdatablePropNames = N.newHashSet();
                final Set<String> transientPropNames = N.newHashSet();

                for (PropInfo propInfo : entityInfo.propInfoList) {
                    if (propInfo.isAnnotationPresent(ReadOnly.class) || propInfo.isAnnotationPresent(ReadOnlyId.class)) {
                        nonUpdatableNonWritablePropNames.add(propInfo.name);
                    }

                    if (propInfo.isAnnotationPresent(NonUpdatable.class)) {
                        nonUpdatablePropNames.add(propInfo.name);
                    }

                    if (propInfo.isTransient || propInfo.isAnnotationPresent(NotColumn.class)) {
                        nonUpdatableNonWritablePropNames.add(propInfo.name);
                        transientPropNames.add(propInfo.name);
                    }
                }

                nonUpdatablePropNames.addAll(nonUpdatableNonWritablePropNames);

                val[0].removeAll(transientPropNames);
                val[1].removeAll(transientPropNames);
                val[2].removeAll(nonUpdatableNonWritablePropNames);
                val[3].removeAll(nonUpdatableNonWritablePropNames);
                val[4].removeAll(nonUpdatablePropNames);

                for (String idPropName : ClassUtil.getIdFieldNames(entityClass)) {
                    val[3].remove(idPropName);
                    val[3].remove(ClassUtil.getPropNameByMethod(ClassUtil.getPropGetMethod(entityClass, idPropName)));
                }

                val[0] = ImmutableSet.of(val[0]); // for select, including sub entity properties.
                val[1] = ImmutableSet.of(val[1]); // for select, no sub entity properties.
                val[2] = ImmutableSet.of(val[2]); // for insert with id
                val[3] = ImmutableSet.of(val[3]); // for insert without id
                val[4] = ImmutableSet.of(val[4]); // for update.

                defaultPropNamesPool.put(entityClass, val);
            }
        }

        return val;
    }

    /**
     * Gets the sub entity prop names.
     *
     * @param entityClass
     * @return
     */
    static ImmutableSet<String> getSubEntityPropNames(final Class<?> entityClass) {
        synchronized (entityClass) {
            ImmutableSet<String> subEntityPropNames = subEntityPropNamesPool.get(entityClass);

            if (subEntityPropNames == null) {
                final EntityInfo entityInfo = ParserUtil.getEntityInfo(entityClass);
                final ImmutableSet<String> nonSubEntityPropNames = nonSubEntityPropNamesPool.get(entityClass);
                final Set<String> subEntityPropNameSet = N.newLinkedHashSet();

                for (PropInfo propInfo : entityInfo.propInfoList) {
                    if (!propInfo.isMarkedToColumn && (propInfo.type.isEntity() || (propInfo.type.isCollection() && propInfo.type.getElementType().isEntity()))
                            && (nonSubEntityPropNames == null || !nonSubEntityPropNames.contains(propInfo.name))) {
                        subEntityPropNameSet.add(propInfo.name);
                    }
                }

                subEntityPropNames = ImmutableSet.of(subEntityPropNameSet);

                subEntityPropNamesPool.put(entityClass, subEntityPropNames);
            }

            return subEntityPropNames;
        }
    }

    /**
     * Gets the prop column name map.
     *
     * @return
     */
    public static ImmutableMap<String, String> getPropColumnNameMap(final Class<?> entityClass, final NamingPolicy namingPolicy) {
        if (entityClass == null || Map.class.isAssignableFrom(entityClass)) {
            return ImmutableMap.empty();
        }

        final Map<NamingPolicy, ImmutableMap<String, String>> namingColumnNameMap = entityTablePropColumnNameMap.get(entityClass);
        ImmutableMap<String, String> result = null;

        if (namingColumnNameMap == null || (result = namingColumnNameMap.get(namingPolicy)) == null) {
            result = registerEntityPropColumnNameMap(entityClass, namingPolicy, null);
        }

        return result;
    }

    private static ImmutableMap<String, String> registerEntityPropColumnNameMap(final Class<?> entityClass, final NamingPolicy namingPolicy,
            final Set<Class<?>> registeringClasses) {
        N.checkArgNotNull(entityClass);

        if (registeringClasses != null) {
            if (registeringClasses.contains(entityClass)) {
                throw new RuntimeException("Cycling references found among: " + registeringClasses);
            } else {
                registeringClasses.add(entityClass);
            }
        }

        Map<String, String> propColumnNameMap = new HashMap<>();
        final EntityInfo entityInfo = ParserUtil.getEntityInfo(entityClass);

        for (PropInfo propInfo : entityInfo.propInfoList) {
            if (propInfo.columnName.isPresent()) {
                propColumnNameMap.put(propInfo.name, propInfo.columnName.get());
            } else {
                propColumnNameMap.put(propInfo.name, formalizeColumnName(propInfo.name, namingPolicy));

                final Type<?> propType = propInfo.type.isCollection() ? propInfo.type.getElementType() : propInfo.type;

                if (propType.isEntity() && (registeringClasses == null || !registeringClasses.contains(propType.clazz()))) {
                    final Set<Class<?>> newRegisteringClasses = registeringClasses == null ? N.<Class<?>> newLinkedHashSet() : registeringClasses;
                    newRegisteringClasses.add(entityClass);

                    final Map<String, String> subPropColumnNameMap = registerEntityPropColumnNameMap(propType.clazz(), namingPolicy, newRegisteringClasses);

                    if (N.notNullOrEmpty(subPropColumnNameMap)) {
                        final String subTableName = getTableName(propType.clazz(), namingPolicy);

                        for (Map.Entry<String, String> entry : subPropColumnNameMap.entrySet()) {
                            propColumnNameMap.put(propInfo.name + WD.PERIOD + entry.getKey(), subTableName + WD.PERIOD + entry.getValue());
                        }
                    }
                }
            }
        }

        //    final Map<String, String> tmp = entityTablePropColumnNameMap.get(entityClass);
        //
        //    if (N.notNullOrEmpty(tmp)) {
        //        propColumnNameMap.putAll(tmp);
        //    }

        if (N.isNullOrEmpty(propColumnNameMap)) {
            propColumnNameMap = N.<String, String> emptyMap();
        }

        final ImmutableMap<String, String> result = ImmutableMap.of(propColumnNameMap);

        Map<NamingPolicy, ImmutableMap<String, String>> namingPropColumnMap = entityTablePropColumnNameMap.get(entityClass);

        if (namingPropColumnMap == null) {
            namingPropColumnMap = new EnumMap<>(NamingPolicy.class);
            // TODO not necessary?
            // namingPropColumnMap = Collections.synchronizedMap(namingPropColumnMap)
            entityTablePropColumnNameMap.put(entityClass, namingPropColumnMap);
        }

        namingPropColumnMap.put(namingPolicy, result);

        return result;
    }

    /**
     * Gets the select table names.
     *
     * @param entityClass
     * @param namingPolicy
     * @return
     */
    private static List<String> getSelectTableNames(final Class<?> entityClass, final NamingPolicy namingPolicy) {
        final Set<String> subEntityPropNames = getSubEntityPropNames(entityClass);

        if (N.isNullOrEmpty(subEntityPropNames)) {
            return N.emptyList();
        }

        final List<String> res = new ArrayList<>(subEntityPropNames.size() + 1);
        res.add(getTableName(entityClass, namingPolicy));

        final EntityInfo entityInfo = ParserUtil.getEntityInfo(entityClass);
        PropInfo propInfo = null;
        Class<?> subEntityClass = null;

        for (String subEntityPropName : subEntityPropNames) {
            propInfo = entityInfo.getPropInfo(subEntityPropName);
            subEntityClass = (propInfo.type.isCollection() ? propInfo.type.getElementType() : propInfo.type).clazz();
            res.add(getTableName(subEntityClass, namingPolicy));
        }

        return res;
    }

    //    /**
    //     * Register the irregular column names which can not be converted from property name by naming policy.
    //     *
    //     * @param propNameTableInterface the interface generated by <code>com.landawn.abacus.util.CodeGenerator</code>
    //     */
    //    public static void registerColumnName(final Class<?> propNameTableInterface) {
    //        final String PCM = "_PCM";
    //
    //        try {
    //            final Map<String, String> _pcm = (Map<String, String>) propNameTableInterface.getField(PCM).get(null);
    //
    //            for (Class<?> cls : propNameTableInterface.getDeclaredClasses()) {
    //                final String entityName = (String) cls.getField(D.UNDERSCORE).get(null);
    //                final Map<String, String> entityPCM = (Map<String, String>) cls.getField(PCM).get(null);
    //
    //                final Map<String, String> propColumnNameMap = new HashMap<>(_pcm);
    //                propColumnNameMap.putAll(entityPCM);
    //
    //                registerColumnName(entityName, propColumnNameMap);
    //            }
    //        } catch (Exception e) {
    //            throw N.toRuntimeException(e);
    //        }
    //    }

    //    /**
    //     * Returns an immutable list of the property name by the specified entity class.
    //     *
    //     * @param entityClass
    //     * @return
    //     */
    //    public static List<String> propNameList(final Class<?> entityClass) {
    //        List<String> propNameList = classPropNameListPool.get(entityClass);
    //
    //        if (propNameList == null) {
    //            synchronized (classPropNameListPool) {
    //                propNameList = classPropNameListPool.get(entityClass);
    //
    //                if (propNameList == null) {
    //                    propNameList = N.asImmutableList(new ArrayList<>(N.getPropGetMethodList(entityClass).keySet()));
    //                    classPropNameListPool.put(entityClass, propNameList);
    //                }
    //            }
    //        }
    //
    //        return propNameList;
    //    }

    //    /**
    //     * Returns an immutable set of the property name by the specified entity class.
    //     *
    //     * @param entityClass
    //     * @return
    //     */
    //    public static Set<String> propNameSet(final Class<?> entityClass) {
    //        Set<String> propNameSet = classPropNameSetPool.get(entityClass);
    //
    //        if (propNameSet == null) {
    //            synchronized (classPropNameSetPool) {
    //                propNameSet = classPropNameSetPool.get(entityClass);
    //
    //                if (propNameSet == null) {
    //                    propNameSet = N.asImmutableSet(N.newLinkedHashSet(N.getPropGetMethodList(entityClass).keySet()));
    //                    classPropNameSetPool.put(entityClass, propNameSet);
    //                }
    //            }
    //        }
    //
    //        return propNameSet;
    //    }

    /**
     *
     * @param propNames
     * @return
     */
    @Beta
    static Map<String, Expression> named(final String... propNames) {
        final Map<String, Expression> m = new LinkedHashMap<>(N.initHashCapacity(propNames.length));

        for (String propName : propNames) {
            m.put(propName, CF.QME);
        }

        return m;
    }

    /**
     *
     * @param propNames
     * @return
     */
    @Beta
    static Map<String, Expression> named(final Collection<String> propNames) {
        final Map<String, Expression> m = new LinkedHashMap<>(N.initHashCapacity(propNames.size()));

        for (String propName : propNames) {
            m.put(propName, CF.QME);
        }

        return m;
    }

    /** The Constant QM_CACHE. */
    private static final Map<Integer, String> QM_CACHE = new HashMap<>();

    static {
        for (int i = 0; i <= 30; i++) {
            QM_CACHE.put(i, StringUtil.repeat("?", i, ", "));
        }

        QM_CACHE.put(100, StringUtil.repeat("?", 100, ", "));
        QM_CACHE.put(200, StringUtil.repeat("?", 200, ", "));
        QM_CACHE.put(300, StringUtil.repeat("?", 300, ", "));
        QM_CACHE.put(500, StringUtil.repeat("?", 500, ", "));
        QM_CACHE.put(1000, StringUtil.repeat("?", 1000, ", "));
    }

    /**
     * Repeat question mark({@code ?}) {@code n} times with delimiter {@code ", "}.
     * <br />
     * It's designed for batch SQL builder.
     *
     * @param n
     * @return
     */
    public static String repeatQM(int n) {
        N.checkArgNotNegative(n, "count");

        String result = QM_CACHE.get(n);

        if (result == null) {
            result = StringUtil.repeat("?", n, ", ");
        }

        return result;
    }

    /**
     *
     * @param tableName
     * @return
     */
    public SQLBuilder into(final String tableName) {
        if (op != OperationType.ADD) {
            throw new RuntimeException("Invalid operation: " + op);
        }

        if (N.isNullOrEmpty(columnNames) && N.isNullOrEmpty(columnNameList) && N.isNullOrEmpty(props) && N.isNullOrEmpty(propsList)) {
            throw new RuntimeException("Column names or props must be set first by insert");
        }

        this.tableName = tableName;

        sb.append(_INSERT);
        sb.append(_SPACE_INTO_SPACE);

        sb.append(tableName);

        sb.append(_SPACE);
        sb.append(WD._PARENTHESES_L);

        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);

        if (N.notNullOrEmpty(columnNames)) {
            if (columnNames.length == 1 && columnNames[0].indexOf(_SPACE) > 0) {
                sb.append(columnNames[0]);
            } else {
                for (int i = 0, len = columnNames.length; i < len; i++) {
                    if (i > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, columnNames[i]));
                }
            }
        } else if (N.notNullOrEmpty(columnNameList)) {
            int i = 0;
            for (String columnName : columnNameList) {
                if (i++ > 0) {
                    sb.append(_COMMA_SPACE);
                }

                sb.append(formalizeColumnName(propColumnNameMap, columnName));
            }
        } else {
            final Map<String, Object> props = N.isNullOrEmpty(this.props) ? propsList.iterator().next() : this.props;

            int i = 0;
            for (String columnName : props.keySet()) {
                if (i++ > 0) {
                    sb.append(_COMMA_SPACE);
                }

                sb.append(formalizeColumnName(propColumnNameMap, columnName));
            }
        }

        sb.append(WD._PARENTHESES_R);

        sb.append(_SPACE_VALUES_SPACE);

        sb.append(WD._PARENTHESES_L);

        if (N.notNullOrEmpty(columnNames)) {
            switch (sqlPolicy) {
                case SQL:
                case PARAMETERIZED_SQL: {
                    for (int i = 0, len = columnNames.length; i < len; i++) {
                        if (i > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append(WD._QUESTION_MARK);
                    }

                    break;
                }

                case NAMED_SQL: {
                    for (int i = 0, len = columnNames.length; i < len; i++) {
                        if (i > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append(":");
                        sb.append(columnNames[i]);
                    }

                    break;
                }

                case IBATIS_SQL: {
                    for (int i = 0, len = columnNames.length; i < len; i++) {
                        if (i > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append("#{");
                        sb.append(columnNames[i]);
                        sb.append('}');
                    }

                    break;
                }

                default:
                    throw new RuntimeException("Not supported SQL policy: " + sqlPolicy);
            }
        } else if (N.notNullOrEmpty(columnNameList)) {
            switch (sqlPolicy) {
                case SQL:
                case PARAMETERIZED_SQL: {
                    for (int i = 0, size = columnNameList.size(); i < size; i++) {
                        if (i > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append(WD._QUESTION_MARK);
                    }

                    break;
                }

                case NAMED_SQL: {
                    int i = 0;
                    for (String columnName : columnNameList) {
                        if (i++ > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append(":");
                        sb.append(columnName);
                    }

                    break;
                }

                case IBATIS_SQL: {
                    int i = 0;
                    for (String columnName : columnNameList) {
                        if (i++ > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append("#{");
                        sb.append(columnName);
                        sb.append('}');
                    }

                    break;
                }

                default:
                    throw new RuntimeException("Not supported SQL policy: " + sqlPolicy);
            }
        } else if (N.notNullOrEmpty(props)) {
            appendInsertProps(props);
        } else {
            int i = 0;
            for (Map<String, Object> props : propsList) {
                if (i++ > 0) {
                    sb.append(WD._PARENTHESES_R);
                    sb.append(_COMMA_SPACE);
                    sb.append(WD._PARENTHESES_L);
                }

                appendInsertProps(props);
            }
        }

        sb.append(WD._PARENTHESES_R);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder into(final Class<?> entityClass) {
        if (this.entityClass == null) {
            this.entityClass = entityClass;
        }

        return into(getTableName(entityClass, namingPolicy));
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder from(String expr) {
        expr = expr.trim();
        String tableName = expr.indexOf(WD._COMMA) > 0 ? StringUtil.substring(expr, 0, WD._COMMA).get() : expr;

        if (tableName.indexOf(_SPACE) > 0) {
            tableName = StringUtil.substring(tableName, 0, _SPACE).get();
        }

        return from(tableName.trim(), expr);
    }

    /**
     *
     * @param tableNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder from(final String... tableNames) {
        if (tableNames.length == 1) {
            return from(tableNames[0].trim());
        }

        final String tableName = tableNames[0].trim();
        return from(tableName, StringUtil.join(tableNames, WD.COMMA_SPACE));
    }

    /**
     *
     * @param tableNames
     * @return
     */
    public SQLBuilder from(final Collection<String> tableNames) {
        if (tableNames.size() == 1) {
            return from(tableNames.iterator().next().trim());
        }

        final String tableName = tableNames.iterator().next().trim();
        return from(tableName, StringUtil.join(tableNames, WD.COMMA_SPACE));
    }

    /**
     *
     * @param tableAliases
     * @return
     */
    public SQLBuilder from(final Map<String, String> tableAliases) {
        final String tableName = tableAliases.keySet().iterator().next().trim();

        return from(tableName, StringUtil.joinEntries(tableAliases, WD.COMMA_SPACE, " "));
    }

    /**
     *
     * @param tableName
     * @param fromCause
     * @return
     */
    private SQLBuilder from(final String tableName, final String fromCause) {
        if (op != OperationType.QUERY) {
            throw new RuntimeException("Invalid operation: " + op);
        }

        if (N.isNullOrEmpty(columnNames) && N.isNullOrEmpty(columnNameList) && N.isNullOrEmpty(columnAliases) && N.isNullOrEmpty(multiSelects)) {
            throw new RuntimeException("Column names or props must be set first by select");
        }

        this.tableName = tableName;

        sb.append(_SELECT);
        sb.append(_SPACE);

        if (N.notNullOrEmpty(predicates)) {
            sb.append(predicates);
            sb.append(_SPACE);
        }

        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);

        if (N.notNullOrEmpty(columnNames)) {
            if (columnNames.length == 1) {
                final String columnName = StringUtil.trim(columnNames[0]);
                int idx = columnName.indexOf(' ');

                if (idx < 0) {
                    idx = columnName.indexOf(',');
                }

                if (idx > 0) {
                    sb.append(columnName);
                } else {
                    sb.append(formalizeColumnName(propColumnNameMap, columnName));

                    if (namingPolicy != NamingPolicy.LOWER_CAMEL_CASE && !WD.ASTERISK.equals(columnName)) {
                        sb.append(_SPACE_AS_SPACE);

                        sb.append(WD._QUOTATION_D);
                        sb.append(columnName);
                        sb.append(WD._QUOTATION_D);
                    }
                }
            } else {
                String columnName = null;

                for (int i = 0, len = columnNames.length; i < len; i++) {
                    columnName = StringUtil.trim(columnNames[i]);

                    if (i > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    int idx = columnName.indexOf(' ');

                    if (idx > 0) {
                        int idx2 = columnName.indexOf(" AS ", idx);

                        if (idx2 < 0) {
                            idx2 = columnName.indexOf(" as ", idx);
                        }

                        sb.append(formalizeColumnName(propColumnNameMap, columnName.substring(0, idx).trim()));

                        sb.append(_SPACE_AS_SPACE);

                        sb.append(WD._QUOTATION_D);
                        sb.append(columnName.substring(idx2 > 0 ? idx2 + 4 : idx + 1).trim());
                        sb.append(WD._QUOTATION_D);
                    } else {
                        sb.append(formalizeColumnName(propColumnNameMap, columnName));

                        if (namingPolicy != NamingPolicy.LOWER_CAMEL_CASE && !WD.ASTERISK.equals(columnName)) {
                            sb.append(_SPACE_AS_SPACE);

                            sb.append(WD._QUOTATION_D);
                            sb.append(columnName);
                            sb.append(WD._QUOTATION_D);
                        }
                    }
                }
            }
        } else if (N.notNullOrEmpty(columnNameList)) {
            if (entityClass != null && columnNameList == getSelectPropNamesByClass(entityClass, false, null)) {
                String fullSelectParts = fullSelectPartsPool.get(namingPolicy).get(entityClass);

                if (N.isNullOrEmpty(fullSelectParts)) {
                    fullSelectParts = "";

                    int i = 0;
                    for (String columnName : columnNameList) {
                        if (i++ > 0) {
                            fullSelectParts += WD.COMMA_SPACE;
                        }

                        fullSelectParts += formalizeColumnName(propColumnNameMap, columnName);

                        if (namingPolicy != NamingPolicy.LOWER_CAMEL_CASE && !WD.ASTERISK.equals(columnName)) {
                            fullSelectParts += " AS ";

                            fullSelectParts += WD.QUOTATION_D;
                            fullSelectParts += columnName;
                            fullSelectParts += WD.QUOTATION_D;
                        }
                    }

                    fullSelectPartsPool.get(namingPolicy).put(entityClass, fullSelectParts);
                }

                sb.append(fullSelectParts);
            } else {
                int i = 0;
                for (String columnName : columnNameList) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, columnName));

                    if (namingPolicy != NamingPolicy.LOWER_CAMEL_CASE && !WD.ASTERISK.equals(columnName)) {
                        sb.append(_SPACE_AS_SPACE);

                        sb.append(WD._QUOTATION_D);
                        sb.append(columnName);
                        sb.append(WD._QUOTATION_D);
                    }
                }
            }
        } else if (N.notNullOrEmpty(columnAliases)) {
            int i = 0;
            for (Map.Entry<String, String> entry : columnAliases.entrySet()) {
                if (i++ > 0) {
                    sb.append(_COMMA_SPACE);
                }

                sb.append(formalizeColumnName(propColumnNameMap, entry.getKey()));

                if (N.notNullOrEmpty(entry.getValue())) {
                    sb.append(_SPACE_AS_SPACE);

                    sb.append(WD._QUOTATION_D);
                    sb.append(entry.getValue());
                    sb.append(WD._QUOTATION_D);
                }
            }
        } else if (N.notNullOrEmpty(multiSelects)) {
            int i = 0;

            aliasPropColumnNameMap = new HashMap<>(multiSelects.size());

            for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                if (N.notNullOrEmpty(tp._2)) {
                    aliasPropColumnNameMap.put(tp._2, getPropColumnNameMap(tp._1, namingPolicy));
                }
            }

            for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                final String tableAlias = tp._2;
                final boolean withTableAlias = N.notNullOrEmpty(tableAlias);
                final Map<String, String> eachPropColumnNameMap = getPropColumnNameMap(tp._1, namingPolicy);

                for (String propName : getSelectPropNamesByClass(tp._1, false, tp._3)) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(eachPropColumnNameMap, propName));

                    sb.append(_SPACE_AS_SPACE);
                    sb.append(WD._QUOTATION_D);

                    if (withTableAlias) {
                        sb.append(tableAlias).append(WD._PERIOD);
                    }

                    sb.append(propName);
                    sb.append(WD._QUOTATION_D);
                }
            }
        } else {
            throw new UnsupportedOperationException("No select part specified");
        }

        sb.append(_SPACE_FROM_SPACE);

        sb.append(fromCause);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder from(final Class<?> entityClass) {
        if (this.entityClass == null) {
            this.entityClass = entityClass;
        }

        return from(getTableName(entityClass, namingPolicy));
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder from(final Class<?> entityClass, final String alias) {
        if (this.entityClass == null) {
            this.entityClass = entityClass;
        }

        addPropColumnMapForAlias(entityClass, alias);

        return from(getTableName(entityClass, namingPolicy) + " " + alias);
    }

    private void addPropColumnMapForAlias(final Class<?> entityClass, final String alias) {
        if (aliasPropColumnNameMap == null) {
            aliasPropColumnNameMap = new HashMap<>();
        }

        aliasPropColumnNameMap.put(alias, getPropColumnNameMap(entityClass, namingPolicy));
    }

    private SQLBuilder from(final Class<?> entityClass, final Collection<String> tableNames) {
        if (this.entityClass == null) {
            this.entityClass = entityClass;
        }

        return from(tableNames);
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder join(final String expr) {
        sb.append(_SPACE_JOIN_SPACE);

        sb.append(expr);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder join(final Class<?> entityClass) {
        sb.append(_SPACE_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy));

        return this;
    }

    /**
     * 
     * @param entityClass
     * @param alias
     * @return
     */
    public SQLBuilder join(final Class<?> entityClass, final String alias) {
        addPropColumnMapForAlias(entityClass, alias);

        sb.append(_SPACE_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy) + " " + alias);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder innerJoin(final String expr) {
        sb.append(_SPACE_INNER_JOIN_SPACE);

        sb.append(expr);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder innerJoin(final Class<?> entityClass) {
        sb.append(_SPACE_INNER_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy));

        return this;
    }

    /**
     *
     * @param entityClass
     * @param alias
     * @return
     */
    public SQLBuilder innerJoin(final Class<?> entityClass, final String alias) {
        addPropColumnMapForAlias(entityClass, alias);

        sb.append(_SPACE_INNER_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy) + " " + alias);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder leftJoin(final String expr) {
        sb.append(_SPACE_LEFT_JOIN_SPACE);

        sb.append(expr);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder leftJoin(final Class<?> entityClass) {
        sb.append(_SPACE_LEFT_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy));

        return this;
    }

    /**
     *
     * @param entityClass
     * @param alias
     * @return
     */
    public SQLBuilder leftJoin(final Class<?> entityClass, final String alias) {
        addPropColumnMapForAlias(entityClass, alias);

        sb.append(_SPACE_LEFT_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy) + " " + alias);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder rightJoin(final String expr) {
        sb.append(_SPACE_RIGHT_JOIN_SPACE);

        sb.append(expr);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder rightJoin(final Class<?> entityClass) {
        sb.append(_SPACE_RIGHT_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy));

        return this;
    }

    /**
     *
     * @param entityClass
     * @param alias
     * @return
     */
    public SQLBuilder rightJoin(final Class<?> entityClass, final String alias) {
        addPropColumnMapForAlias(entityClass, alias);

        sb.append(_SPACE_RIGHT_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy) + " " + alias);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder fullJoin(final String expr) {
        sb.append(_SPACE_FULL_JOIN_SPACE);

        sb.append(expr);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder fullJoin(final Class<?> entityClass) {
        sb.append(_SPACE_FULL_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy));

        return this;
    }

    /**
     *
     * @param entityClass
     * @param alias
     * @return
     */
    public SQLBuilder fullJoin(final Class<?> entityClass, final String alias) {
        addPropColumnMapForAlias(entityClass, alias);

        sb.append(_SPACE_FULL_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy) + " " + alias);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder crossJoin(final String expr) {
        sb.append(_SPACE_CROSS_JOIN_SPACE);

        sb.append(expr);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder crossJoin(final Class<?> entityClass) {
        sb.append(_SPACE_CROSS_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy));

        return this;
    }

    /**
     *
     * @param entityClass
     * @param alias
     * @return
     */
    public SQLBuilder crossJoin(final Class<?> entityClass, final String alias) {
        addPropColumnMapForAlias(entityClass, alias);

        sb.append(_SPACE_CROSS_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy) + " " + alias);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder naturalJoin(final String expr) {
        sb.append(_SPACE_NATURAL_JOIN_SPACE);

        sb.append(expr);

        return this;
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder naturalJoin(final Class<?> entityClass) {
        sb.append(_SPACE_NATURAL_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy));

        return this;
    }

    /**
     *
     * @param entityClass
     * @param alias
     * @return
     */
    public SQLBuilder naturalJoin(final Class<?> entityClass, final String alias) {
        addPropColumnMapForAlias(entityClass, alias);

        sb.append(_SPACE_NATURAL_JOIN_SPACE);

        sb.append(getTableName(entityClass, namingPolicy) + " " + alias);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder on(final String expr) {
        sb.append(_SPACE_ON_SPACE);

        appendStringExpr(expr);

        return this;
    }

    /**
     *
     * @param cond any literal written in <code>Expression</code> condition won't be formalized
     * @return
     */
    public SQLBuilder on(final Condition cond) {
        sb.append(_SPACE_ON_SPACE);

        appendCondition(cond);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder using(final String expr) {
        sb.append(_SPACE_USING_SPACE);

        sb.append(formalizeColumnName(expr));

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder where(final String expr) {
        init(true);

        sb.append(_SPACE_WHERE_SPACE);

        appendStringExpr(expr);

        return this;
    }

    /**
     * Append string expr.
     *
     * @param expr
     */
    private void appendStringExpr(final String expr) {
        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);
        final List<String> words = SQLParser.parse(expr);

        String word = null;
        for (int i = 0, len = words.size(); i < len; i++) {
            word = words.get(i);

            if (!StringUtil.isAsciiAlpha(word.charAt(0))) {
                sb.append(word);
            } else if (SQLParser.isFunctionName(words, len, i)) {
                sb.append(word);
            } else {
                sb.append(formalizeColumnName(propColumnNameMap, word));
            }
        }
    }

    public SQLBuilder where(final Condition cond) {
        init(true);

        sb.append(_SPACE_WHERE_SPACE);

        appendCondition(cond);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder groupBy(final String expr) {
        sb.append(_SPACE_GROUP_BY_SPACE);

        if (expr.indexOf(_SPACE) > 0) {
            // sb.append(columnNames[0]);
            appendStringExpr(expr);
        } else {
            sb.append(formalizeColumnName(expr));
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder groupBy(final String... columnNames) {
        sb.append(_SPACE_GROUP_BY_SPACE);

        if (columnNames.length == 1) {
            if (columnNames[0].indexOf(_SPACE) > 0) {
                // sb.append(columnNames[0]);
                appendStringExpr(columnNames[0]);
            } else {
                sb.append(formalizeColumnName(columnNames[0]));
            }
        } else {
            final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);

            for (int i = 0, len = columnNames.length; i < len; i++) {
                if (i > 0) {
                    sb.append(_COMMA_SPACE);
                }

                sb.append(formalizeColumnName(propColumnNameMap, columnNames[i]));
            }
        }

        return this;
    }

    /**
     *
     * @param columnName
     * @param direction
     * @return
     */
    public SQLBuilder groupBy(final String columnName, final SortDirection direction) {
        groupBy(columnName);

        sb.append(_SPACE);
        sb.append(direction.toString());

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    public SQLBuilder groupBy(final Collection<String> columnNames) {
        sb.append(_SPACE_GROUP_BY_SPACE);

        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);
        int i = 0;
        for (String columnName : columnNames) {
            if (i++ > 0) {
                sb.append(_COMMA_SPACE);
            }

            sb.append(formalizeColumnName(propColumnNameMap, columnName));
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @param direction
     * @return
     */
    public SQLBuilder groupBy(final Collection<String> columnNames, final SortDirection direction) {
        groupBy(columnNames);

        sb.append(_SPACE);
        sb.append(direction.toString());

        return this;
    }

    /**
     *
     * @param orders
     * @return
     */
    public SQLBuilder groupBy(final Map<String, SortDirection> orders) {
        sb.append(_SPACE_GROUP_BY_SPACE);

        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);
        int i = 0;
        for (Map.Entry<String, SortDirection> entry : orders.entrySet()) {
            if (i++ > 0) {

                sb.append(_COMMA_SPACE);
            }

            sb.append(formalizeColumnName(propColumnNameMap, entry.getKey()));

            sb.append(_SPACE);
            sb.append(entry.getValue().toString());
        }

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder having(final String expr) {
        sb.append(_SPACE_HAVING_SPACE);

        appendStringExpr(expr);

        return this;
    }

    /**
     *
     * @param cond any literal written in <code>Expression</code> condition won't be formalized
     * @return
     */
    public SQLBuilder having(final Condition cond) {
        sb.append(_SPACE_HAVING_SPACE);

        appendCondition(cond);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder orderBy(final String expr) {
        sb.append(_SPACE_ORDER_BY_SPACE);

        if (expr.indexOf(_SPACE) > 0) {
            // sb.append(columnNames[0]);
            appendStringExpr(expr);
        } else {
            sb.append(formalizeColumnName(expr));
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder orderBy(final String... columnNames) {
        sb.append(_SPACE_ORDER_BY_SPACE);

        if (columnNames.length == 1) {
            if (columnNames[0].indexOf(_SPACE) > 0) {
                // sb.append(columnNames[0]);
                appendStringExpr(columnNames[0]);
            } else {
                sb.append(formalizeColumnName(columnNames[0]));
            }
        } else {
            final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);

            for (int i = 0, len = columnNames.length; i < len; i++) {
                if (i > 0) {
                    sb.append(_COMMA_SPACE);
                }

                sb.append(formalizeColumnName(propColumnNameMap, columnNames[i]));
            }
        }

        return this;
    }

    /**
     *
     * @param columnName
     * @param direction
     * @return
     */
    public SQLBuilder orderBy(final String columnName, final SortDirection direction) {
        orderBy(columnName);

        sb.append(_SPACE);
        sb.append(direction.toString());

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    public SQLBuilder orderBy(final Collection<String> columnNames) {
        sb.append(_SPACE_ORDER_BY_SPACE);

        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);
        int i = 0;
        for (String columnName : columnNames) {
            if (i++ > 0) {
                sb.append(_COMMA_SPACE);
            }

            sb.append(formalizeColumnName(propColumnNameMap, columnName));
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @param direction
     * @return
     */
    public SQLBuilder orderBy(final Collection<String> columnNames, final SortDirection direction) {
        orderBy(columnNames);

        sb.append(_SPACE);
        sb.append(direction.toString());

        return this;
    }

    /**
     *
     * @param orders
     * @return
     */
    public SQLBuilder orderBy(final Map<String, SortDirection> orders) {
        sb.append(_SPACE_ORDER_BY_SPACE);

        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);
        int i = 0;

        for (Map.Entry<String, SortDirection> entry : orders.entrySet()) {
            if (i++ > 0) {
                sb.append(_COMMA_SPACE);
            }

            sb.append(formalizeColumnName(propColumnNameMap, entry.getKey()));

            sb.append(_SPACE);
            sb.append(entry.getValue().toString());
        }

        return this;
    }

    /**
     *
     * @param count
     * @return
     */
    public SQLBuilder limit(final int count) {
        sb.append(_SPACE_LIMIT_SPACE);

        sb.append(count);

        return this;
    }

    /**
     *
     * @param offset
     * @param count
     * @return
     */
    public SQLBuilder limit(final int offset, final int count) {
        sb.append(_SPACE_LIMIT_SPACE);

        sb.append(offset);

        sb.append(_COMMA_SPACE);

        sb.append(count);

        return this;
    }

    /**
     *
     * @param offset
     * @return
     */
    public SQLBuilder offset(final int offset) {
        sb.append(_SPACE_OFFSET_SPACE);

        sb.append(offset);

        return this;
    }

    /**
     * Limit by row num.
     *
     * @param count
     * @return
     */
    public SQLBuilder limitByRowNum(final int count) {
        sb.append(" ROWNUM ");

        sb.append(count);

        return this;
    }

    public SQLBuilder append(final Condition cond) {
        init(true);

        if (cond instanceof Criteria) {
            final Criteria criteria = (Criteria) cond;

            final Collection<Join> joins = criteria.getJoins();

            if (N.notNullOrEmpty(joins)) {
                for (Join join : joins) {
                    sb.append(_SPACE).append(join.getOperator()).append(_SPACE);

                    if (join.getJoinEntities().size() == 1) {
                        sb.append(join.getJoinEntities().get(0));
                    } else {
                        sb.append(WD._PARENTHESES_L);
                        int idx = 0;

                        for (String joinTableName : join.getJoinEntities()) {
                            if (idx++ > 0) {
                                sb.append(_COMMA_SPACE);
                            }

                            sb.append(joinTableName);
                        }

                        sb.append(WD._PARENTHESES_R);
                    }

                    appendCondition(join.getCondition());
                }
            }

            final Cell where = criteria.getWhere();

            if ((where != null)) {
                sb.append(_SPACE_WHERE_SPACE);
                appendCondition(where.getCondition());
            }

            final Cell groupBy = criteria.getGroupBy();

            if (groupBy != null) {
                sb.append(_SPACE_GROUP_BY_SPACE);
                appendCondition(groupBy.getCondition());
            }

            final Cell having = criteria.getHaving();

            if (having != null) {
                sb.append(_SPACE_HAVING_SPACE);
                appendCondition(having.getCondition());
            }

            List<Cell> aggregations = criteria.getAggregation();

            if (N.notNullOrEmpty(aggregations)) {
                for (Cell aggregation : aggregations) {
                    sb.append(_SPACE).append(aggregation.getOperator()).append(_SPACE);
                    appendCondition(aggregation.getCondition());
                }
            }

            final Cell orderBy = criteria.getOrderBy();

            if (orderBy != null) {
                sb.append(_SPACE_ORDER_BY_SPACE);
                appendCondition(orderBy.getCondition());
            }

            final Limit limit = criteria.getLimit();

            if (limit != null) {
                if (N.notNullOrEmpty(limit.getExpr())) {
                    sb.append(_SPACE).append(limit.getExpr());
                } else {
                    if (limit.getOffset() > 0) {
                        limit(limit.getOffset(), limit.getCount());
                    } else {
                        limit(limit.getCount());
                    }
                }
            }
        } else if (cond instanceof Clause) {
            sb.append(_SPACE).append(cond.getOperator()).append(_SPACE);
            appendCondition(((Clause) cond).getCondition());
        } else {
            sb.append(_SPACE_WHERE_SPACE);
            appendCondition(cond);
        }

        return this;
    }

    /**
     *
     * @param sqlBuilder
     * @return
     */
    public SQLBuilder union(final SQLBuilder sqlBuilder) {
        final String sql = sqlBuilder.sql();

        if (N.notNullOrEmpty(sqlBuilder.parameters())) {
            parameters.addAll(sqlBuilder.parameters());
        }

        return union(sql);
    }

    /**
     *
     * @param query
     * @return
     */
    public SQLBuilder union(final String query) {
        return union(N.asArray(query));
    }

    /**
     *
     * @param columnNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder union(final String... columnNames) {
        op = OperationType.QUERY;

        this.columnNames = columnNames;
        this.columnNameList = null;
        this.columnAliases = null;

        sb.append(_SPACE_UNION_SPACE);

        // it's sub query
        if (isSubQuery(columnNames)) {
            sb.append(columnNames[0]);

            this.columnNames = null;
        } else {
            // build in from method.
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    public SQLBuilder union(final Collection<String> columnNames) {
        op = OperationType.QUERY;

        this.columnNames = null;
        this.columnNameList = columnNames;
        this.columnAliases = null;

        sb.append(_SPACE_UNION_SPACE);

        return this;
    }

    /**
     *
     * @param sqlBuilder
     * @return
     */
    public SQLBuilder unionAll(final SQLBuilder sqlBuilder) {
        final String sql = sqlBuilder.sql();

        if (N.notNullOrEmpty(sqlBuilder.parameters())) {
            parameters.addAll(sqlBuilder.parameters());
        }

        return unionAll(sql);
    }

    /**
     *
     * @param query
     * @return
     */
    public SQLBuilder unionAll(final String query) {
        return unionAll(N.asArray(query));
    }

    /**
     *
     * @param columnNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder unionAll(final String... columnNames) {
        op = OperationType.QUERY;

        this.columnNames = columnNames;
        this.columnNameList = null;
        this.columnAliases = null;

        sb.append(_SPACE_UNION_ALL_SPACE);

        // it's sub query
        if (isSubQuery(columnNames)) {
            sb.append(columnNames[0]);

            this.columnNames = null;
        } else {
            // build in from method.
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    public SQLBuilder unionAll(final Collection<String> columnNames) {
        op = OperationType.QUERY;

        this.columnNames = null;
        this.columnNameList = columnNames;
        this.columnAliases = null;

        sb.append(_SPACE_UNION_ALL_SPACE);

        return this;
    }

    /**
     *
     * @param sqlBuilder
     * @return
     */
    public SQLBuilder intersect(final SQLBuilder sqlBuilder) {
        final String sql = sqlBuilder.sql();

        if (N.notNullOrEmpty(sqlBuilder.parameters())) {
            parameters.addAll(sqlBuilder.parameters());
        }

        return intersect(sql);
    }

    /**
     *
     * @param query
     * @return
     */
    public SQLBuilder intersect(final String query) {
        return intersect(N.asArray(query));
    }

    /**
     *
     * @param columnNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder intersect(final String... columnNames) {
        op = OperationType.QUERY;

        this.columnNames = columnNames;
        this.columnNameList = null;
        this.columnAliases = null;

        sb.append(_SPACE_INTERSECT_SPACE);

        // it's sub query
        if (isSubQuery(columnNames)) {
            sb.append(columnNames[0]);

            this.columnNames = null;
        } else {
            // build in from method.
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    public SQLBuilder intersect(final Collection<String> columnNames) {
        op = OperationType.QUERY;

        this.columnNames = null;
        this.columnNameList = columnNames;
        this.columnAliases = null;

        sb.append(_SPACE_INTERSECT_SPACE);

        return this;
    }

    /**
     *
     * @param sqlBuilder
     * @return
     */
    public SQLBuilder except(final SQLBuilder sqlBuilder) {
        final String sql = sqlBuilder.sql();

        if (N.notNullOrEmpty(sqlBuilder.parameters())) {
            parameters.addAll(sqlBuilder.parameters());
        }

        return except(sql);
    }

    /**
     *
     * @param query
     * @return
     */
    public SQLBuilder except(final String query) {
        return except(N.asArray(query));
    }

    /**
     *
     * @param columnNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder except(final String... columnNames) {
        op = OperationType.QUERY;

        this.columnNames = columnNames;
        this.columnNameList = null;
        this.columnAliases = null;

        sb.append(_SPACE_EXCEPT_SPACE);

        // it's sub query
        if (isSubQuery(columnNames)) {
            sb.append(columnNames[0]);

            this.columnNames = null;
        } else {
            // build in from method.
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    public SQLBuilder except(final Collection<String> columnNames) {
        op = OperationType.QUERY;

        this.columnNames = null;
        this.columnNameList = columnNames;
        this.columnAliases = null;

        sb.append(_SPACE_EXCEPT_SPACE);

        return this;
    }

    /**
     *
     * @param sqlBuilder
     * @return
     */
    public SQLBuilder minus(final SQLBuilder sqlBuilder) {
        final String sql = sqlBuilder.sql();

        if (N.notNullOrEmpty(sqlBuilder.parameters())) {
            parameters.addAll(sqlBuilder.parameters());
        }

        return minus(sql);
    }

    /**
     *
     * @param query
     * @return
     */
    public SQLBuilder minus(final String query) {
        return minus(N.asArray(query));
    }

    /**
     *
     * @param columnNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder minus(final String... columnNames) {
        op = OperationType.QUERY;

        this.columnNames = columnNames;
        this.columnNameList = null;
        this.columnAliases = null;

        sb.append(_SPACE_EXCEPT2_SPACE);

        // it's sub query
        if (isSubQuery(columnNames)) {
            sb.append(columnNames[0]);

            this.columnNames = null;
        } else {
            // build in from method.
        }

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    public SQLBuilder minus(final Collection<String> columnNames) {
        op = OperationType.QUERY;

        this.columnNames = null;
        this.columnNameList = columnNames;
        this.columnAliases = null;

        sb.append(_SPACE_EXCEPT2_SPACE);

        return this;
    }

    /**
     *
     * @return
     */
    public SQLBuilder forUpdate() {
        sb.append(_SPACE_FOR_UPDATE);

        return this;
    }

    /**
     *
     * @param expr
     * @return
     */
    public SQLBuilder set(final String expr) {
        return set(N.asArray(expr));
    }

    /**
     *
     * @param columnNames
     * @return
     */
    @SafeVarargs
    public final SQLBuilder set(final String... columnNames) {
        init(false);

        if (columnNames.length == 1 && columnNames[0].contains(WD.EQUAL)) {
            appendStringExpr(columnNames[0]);
        } else {
            final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);

            switch (sqlPolicy) {
                case SQL:
                case PARAMETERIZED_SQL: {
                    for (int i = 0, len = columnNames.length; i < len; i++) {
                        if (i > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append(formalizeColumnName(propColumnNameMap, columnNames[i]));

                        sb.append(_SPACE_EQUAL_SPACE);

                        sb.append(WD._QUESTION_MARK);
                    }

                    break;
                }

                case NAMED_SQL: {
                    for (int i = 0, len = columnNames.length; i < len; i++) {
                        if (i > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append(formalizeColumnName(propColumnNameMap, columnNames[i]));

                        sb.append(_SPACE_EQUAL_SPACE);

                        sb.append(":");
                        sb.append(columnNames[i]);
                    }

                    break;
                }

                case IBATIS_SQL: {
                    for (int i = 0, len = columnNames.length; i < len; i++) {
                        if (i > 0) {
                            sb.append(_COMMA_SPACE);
                        }

                        sb.append(formalizeColumnName(propColumnNameMap, columnNames[i]));

                        sb.append(_SPACE_EQUAL_SPACE);

                        sb.append("#{");
                        sb.append(columnNames[i]);
                        sb.append('}');
                    }

                    break;
                }

                default:
                    throw new RuntimeException("Not supported SQL policy: " + sqlPolicy);
            }
        }

        columnNameList = null;

        return this;
    }

    /**
     *
     * @param columnNames
     * @return
     */
    public SQLBuilder set(final Collection<String> columnNames) {
        init(false);

        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);

        switch (sqlPolicy) {
            case SQL:
            case PARAMETERIZED_SQL: {
                int i = 0;
                for (String columnName : columnNames) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, columnName));

                    sb.append(_SPACE_EQUAL_SPACE);

                    sb.append(WD._QUESTION_MARK);
                }

                break;
            }

            case NAMED_SQL: {
                int i = 0;
                for (String columnName : columnNames) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, columnName));

                    sb.append(_SPACE_EQUAL_SPACE);

                    sb.append(":");
                    sb.append(columnName);
                }

                break;
            }

            case IBATIS_SQL: {
                int i = 0;
                for (String columnName : columnNames) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, columnName));

                    sb.append(_SPACE_EQUAL_SPACE);

                    sb.append("#{");
                    sb.append(columnName);
                    sb.append('}');
                }

                break;
            }

            default:
                throw new RuntimeException("Not supported SQL policy: " + sqlPolicy);
        }

        columnNameList = null;

        return this;
    }

    /**
     *
     * @param props
     * @return
     */
    public SQLBuilder set(final Map<String, Object> props) {
        init(false);

        final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);

        switch (sqlPolicy) {
            case SQL: {
                int i = 0;
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, entry.getKey()));

                    sb.append(_SPACE_EQUAL_SPACE);

                    setParameterForSQL(entry.getValue());
                }

                break;
            }

            case PARAMETERIZED_SQL: {
                int i = 0;
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, entry.getKey()));

                    sb.append(_SPACE_EQUAL_SPACE);

                    setParameterForRawSQL(entry.getValue());
                }

                break;
            }

            case NAMED_SQL: {
                int i = 0;
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, entry.getKey()));

                    sb.append(_SPACE_EQUAL_SPACE);

                    setParameterForNamedSQL(entry.getKey(), entry.getValue());
                }

                break;
            }

            case IBATIS_SQL: {
                int i = 0;
                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(formalizeColumnName(propColumnNameMap, entry.getKey()));

                    sb.append(_SPACE_EQUAL_SPACE);

                    setParameterForIbatisNamedSQL(entry.getKey(), entry.getValue());
                }

                break;
            }

            default:
                throw new RuntimeException("Not supported SQL policy: " + sqlPolicy);
        }

        columnNameList = null;

        return this;
    }

    /**
     * Only the dirty properties will be set into the result SQL if the specified entity is a dirty marker entity.
     *
     * @param entity
     * @return
     */
    public SQLBuilder set(final Object entity) {
        return set(entity, null);
    }

    /**
     * Only the dirty properties will be set into the result SQL if the specified entity is a dirty marker entity.
     *
     * @param entity
     * @param excludedPropNames
     * @return
     */
    public SQLBuilder set(final Object entity, final Set<String> excludedPropNames) {
        if (entity instanceof String) {
            return set(N.asArray((String) entity));
        } else if (entity instanceof Map) {
            if (N.isNullOrEmpty(excludedPropNames)) {
                return set((Map<String, Object>) entity);
            } else {
                final Map<String, Object> props = new LinkedHashMap<>((Map<String, Object>) entity);
                Maps.removeKeys(props, excludedPropNames);
                return set(props);
            }
        } else {
            final Class<?> entityClass = entity.getClass();
            this.entityClass = entityClass;
            final Collection<String> propNames = getUpdatePropNamesByClass(entityClass, excludedPropNames);
            final Set<String> dirtyPropNames = DirtyMarkerUtil.isDirtyMarker(entityClass) ? DirtyMarkerUtil.dirtyPropNames((DirtyMarker) entity) : null;
            final boolean isEmptyDirtyPropNames = N.isNullOrEmpty(dirtyPropNames);
            final Map<String, Object> props = N.newHashMap(N.initHashCapacity(N.isNullOrEmpty(dirtyPropNames) ? propNames.size() : dirtyPropNames.size()));
            final EntityInfo entityInfo = ParserUtil.getEntityInfo(entityClass);

            for (String propName : propNames) {
                if (isEmptyDirtyPropNames || dirtyPropNames.contains(propName)) {
                    props.put(propName, entityInfo.getPropValue(entity, propName));
                }
            }

            return set(props);
        }
    }

    /**
     *
     * @param entityClass
     * @return
     */
    public SQLBuilder set(Class<?> entityClass) {
        this.entityClass = entityClass;

        return set(entityClass, null);
    }

    /**
     *
     * @param entityClass
     * @param excludedPropNames
     * @return
     */
    public SQLBuilder set(Class<?> entityClass, final Set<String> excludedPropNames) {
        this.entityClass = entityClass;

        return set(getUpdatePropNamesByClass(entityClass, excludedPropNames));
    }

    /**
     * This SQLBuilder will be closed after <code>sql()</code> is called.
     *
     * @return
     */
    public String sql() {
        if (sb == null) {
            throw new RuntimeException("This SQLBuilder has been closed after sql() was called previously");
        }

        init(true);

        String sql = null;

        try {
            sql = sb.charAt(0) == ' ' ? sb.substring(1) : sb.toString();
        } finally {
            Objectory.recycle(sb);
            sb = null;

            activeStringBuilderCounter.decrementAndGet();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(sql);
        }

        return sql;
    }

    /**
     *
     * @return
     */
    public List<Object> parameters() {
        return parameters;
    }

    /**
     *  This SQLBuilder will be closed after <code>pair()</code> is called.
     *
     * @return
     */
    public SP pair() {
        return new SP(sql(), parameters);
    }

    /**
     *
     * @param <T>
     * @param <EX>
     * @param func
     * @return
     * @throws EX the ex
     */
    public <T, EX extends Exception> T apply(final Throwables.Function<? super SP, T, EX> func) throws EX {
        return func.apply(this.pair());
    }

    /**
     *
     * @param <EX>
     * @param consumer
     * @throws EX the ex
     */
    public <EX extends Exception> void accept(final Throwables.Consumer<? super SP, EX> consumer) throws EX {
        consumer.accept(this.pair());
    }

    /**
     *
     * @param setForUpdate
     */
    void init(boolean setForUpdate) {
        // Note: any change, please take a look at: parse(final Class<?> entityClass, final Condition cond) first.

        if (sb.length() > 0) {
            return;
        }

        if (op == OperationType.UPDATE) {
            sb.append(_UPDATE);

            sb.append(_SPACE);
            sb.append(tableName);

            sb.append(_SPACE_SET_SPACE);

            if (setForUpdate && N.notNullOrEmpty(columnNameList)) {
                set(columnNameList);
            }
        } else if (op == OperationType.DELETE) {
            final String newTableName = tableName;

            char[] deleteFromTableChars = tableDeleteFrom.get(newTableName);

            if (deleteFromTableChars == null) {
                deleteFromTableChars = (WD.DELETE + WD.SPACE + WD.FROM + WD.SPACE + newTableName).toCharArray();
                tableDeleteFrom.put(newTableName, deleteFromTableChars);
            }

            sb.append(deleteFromTableChars);
        }
    }

    /**
     * Sets the parameter for SQL.
     *
     * @param propValue the new parameter for SQL
     */
    private void setParameterForSQL(final Object propValue) {
        if (CF.QME.equals(propValue)) {
            sb.append(WD._QUESTION_MARK);
        } else if (propValue instanceof Condition) {
            appendCondition((Condition) propValue);
        } else {
            sb.append(Expression.formalize(propValue));
        }
    }

    /**
     * Sets the parameter for raw SQL.
     *
     * @param propValue the new parameter for raw SQL
     */
    private void setParameterForRawSQL(final Object propValue) {
        if (CF.QME.equals(propValue)) {
            sb.append(WD._QUESTION_MARK);
        } else if (propValue instanceof Condition) {
            appendCondition((Condition) propValue);
        } else {
            sb.append(WD._QUESTION_MARK);

            parameters.add(propValue);
        }
    }

    /**
     * Sets the parameter for ibatis named SQL.
     *
     * @param propName
     * @param propValue
     */
    private void setParameterForIbatisNamedSQL(final String propName, final Object propValue) {
        if (CF.QME.equals(propValue)) {
            sb.append("#{");
            sb.append(propName);
            sb.append('}');
        } else if (propValue instanceof Condition) {
            appendCondition((Condition) propValue);
        } else {
            sb.append("#{");
            sb.append(propName);
            sb.append('}');

            parameters.add(propValue);
        }
    }

    /**
     * Sets the parameter for named SQL.
     *
     * @param propName
     * @param propValue
     */
    private void setParameterForNamedSQL(final String propName, final Object propValue) {
        if (CF.QME.equals(propValue)) {
            sb.append(":");
            sb.append(propName);
        } else if (propValue instanceof Condition) {
            appendCondition((Condition) propValue);
        } else {
            sb.append(":");
            sb.append(propName);

            parameters.add(propValue);
        }
    }

    /**
     * Sets the parameter.
     *
     * @param propName
     * @param propValue
     */
    private void setParameter(final String propName, final Object propValue) {
        switch (sqlPolicy) {
            case SQL: {
                setParameterForSQL(propValue);

                break;
            }

            case PARAMETERIZED_SQL: {
                setParameterForRawSQL(propValue);

                break;
            }

            case NAMED_SQL: {
                setParameterForNamedSQL(propName, propValue);

                break;
            }

            case IBATIS_SQL: {
                setParameterForIbatisNamedSQL(propName, propValue);

                break;
            }

            default:
                throw new RuntimeException("Not supported SQL policy: " + sqlPolicy);
        }
    }

    /**
     * Append insert props.
     *
     * @param props
     */
    private void appendInsertProps(final Map<String, Object> props) {
        switch (sqlPolicy) {
            case SQL: {
                int i = 0;
                Object propValue = null;
                for (String propName : props.keySet()) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    propValue = props.get(propName);

                    setParameterForSQL(propValue);
                }

                break;
            }

            case PARAMETERIZED_SQL: {
                int i = 0;
                Object propValue = null;
                for (String propName : props.keySet()) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    propValue = props.get(propName);

                    setParameterForRawSQL(propValue);
                }

                break;
            }

            case NAMED_SQL: {
                int i = 0;
                Object propValue = null;
                for (String propName : props.keySet()) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    propValue = props.get(propName);

                    setParameterForNamedSQL(propName, propValue);
                }

                break;
            }

            case IBATIS_SQL: {
                int i = 0;
                Object propValue = null;
                for (String propName : props.keySet()) {
                    if (i++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    propValue = props.get(propName);

                    setParameterForIbatisNamedSQL(propName, propValue);
                }

                break;
            }

            default:
                throw new RuntimeException("Not supported SQL policy: " + sqlPolicy);
        }
    }

    /**
     *
     * @param cond
     */
    private void appendCondition(final Condition cond) {
        //    if (sb.charAt(sb.length() - 1) != _SPACE) {
        //        sb.append(_SPACE);
        //    }

        if (cond instanceof Binary) {
            final Binary binary = (Binary) cond;
            final String propName = binary.getPropName();

            sb.append(formalizeColumnName(propName));

            sb.append(_SPACE);
            sb.append(binary.getOperator().toString());
            sb.append(_SPACE);

            Object propValue = binary.getPropValue();
            setParameter(propName, propValue);
        } else if (cond instanceof Between) {
            final Between bt = (Between) cond;
            final String propName = bt.getPropName();

            sb.append(formalizeColumnName(propName));

            sb.append(_SPACE);
            sb.append(bt.getOperator().toString());
            sb.append(_SPACE);

            Object minValue = bt.getMinValue();
            if (sqlPolicy == SQLPolicy.NAMED_SQL || sqlPolicy == SQLPolicy.IBATIS_SQL) {
                setParameter("min" + StringUtil.capitalize(propName), minValue);
            } else {
                setParameter(propName, minValue);
            }

            sb.append(_SPACE);
            sb.append(WD.AND);
            sb.append(_SPACE);

            Object maxValue = bt.getMaxValue();
            if (sqlPolicy == SQLPolicy.NAMED_SQL || sqlPolicy == SQLPolicy.IBATIS_SQL) {
                setParameter("max" + StringUtil.capitalize(propName), maxValue);
            } else {
                setParameter(propName, maxValue);
            }
        } else if (cond instanceof In) {
            final In in = (In) cond;
            final String propName = in.getPropName();
            final List<Object> parameters = in.getParameters();

            sb.append(formalizeColumnName(propName));

            sb.append(_SPACE);
            sb.append(in.getOperator().toString());
            sb.append(WD.SPACE_PARENTHESES_L);

            for (int i = 0, len = parameters.size(); i < len; i++) {
                if (i > 0) {
                    sb.append(WD.COMMA_SPACE);
                }

                if (sqlPolicy == SQLPolicy.NAMED_SQL || sqlPolicy == SQLPolicy.IBATIS_SQL) {
                    setParameter(propName + (i + 1), parameters.get(i));
                } else {
                    setParameter(propName, parameters.get(i));
                }
            }

            sb.append(WD._PARENTHESES_R);
        } else if (cond instanceof InSubQuery) {
            final InSubQuery inSubQuery = (InSubQuery) cond;
            final String propName = inSubQuery.getPropName();

            sb.append(formalizeColumnName(propName));

            sb.append(_SPACE);
            sb.append(inSubQuery.getOperator().toString());
            sb.append(WD.SPACE_PARENTHESES_L);

            appendCondition(inSubQuery.getSubQuery());

            sb.append(WD._PARENTHESES_R);
        } else if (cond instanceof Where || cond instanceof Having) {
            final Cell cell = (Cell) cond;

            sb.append(_SPACE);
            sb.append(cell.getOperator().toString());
            sb.append(_SPACE);

            appendCondition(cell.getCondition());
        } else if (cond instanceof Cell) {
            final Cell cell = (Cell) cond;

            sb.append(_SPACE);
            sb.append(cell.getOperator().toString());
            sb.append(_SPACE);

            sb.append(_PARENTHESES_L);
            appendCondition(cell.getCondition());
            sb.append(_PARENTHESES_R);
        } else if (cond instanceof Junction) {
            final Junction junction = (Junction) cond;
            final List<Condition> conditionList = junction.getConditions();

            if (N.isNullOrEmpty(conditionList)) {
                throw new IllegalArgumentException("The junction condition(" + junction.getOperator().toString() + ") doesn't include any element.");
            }

            if (conditionList.size() == 1) {
                appendCondition(conditionList.get(0));
            } else {
                // TODO ((id = :id) AND (gui = :gui)) is not support in Cassandra.
                // only (id = :id) AND (gui = :gui) works.
                // sb.append(_PARENTHESES_L);

                for (int i = 0, size = conditionList.size(); i < size; i++) {
                    if (i > 0) {
                        sb.append(_SPACE);
                        sb.append(junction.getOperator().toString());
                        sb.append(_SPACE);
                    }

                    sb.append(_PARENTHESES_L);

                    appendCondition(conditionList.get(i));

                    sb.append(_PARENTHESES_R);
                }

                // sb.append(_PARENTHESES_R);
            }
        } else if (cond instanceof SubQuery) {
            final SubQuery subQuery = (SubQuery) cond;
            final Condition subCond = subQuery.getCondition();

            if (N.notNullOrEmpty(subQuery.getSql())) {
                sb.append(subQuery.getSql());
            } else {
                if (subQuery.getEntityClass() != null) {
                    if (this instanceof SCSB) {
                        sb.append(SCSB.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof PSC) {
                        sb.append(PSC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof MSC) {
                        sb.append(MSC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof NSC) {
                        sb.append(NSC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof ACSB) {
                        sb.append(ACSB.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof PAC) {
                        sb.append(PAC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof MAC) {
                        sb.append(MAC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof NAC) {
                        sb.append(NAC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof LCSB) {
                        sb.append(LCSB.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof PLC) {
                        sb.append(PLC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof MLC) {
                        sb.append(MLC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else if (this instanceof NLC) {
                        sb.append(NLC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityClass()).append(subCond).sql());
                    } else {
                        throw new RuntimeException("Unsupproted subQuery condition: " + cond);
                    }
                } else {
                    if (this instanceof SCSB) {
                        sb.append(SCSB.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof PSC) {
                        sb.append(PSC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof MSC) {
                        sb.append(MSC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof NSC) {
                        sb.append(NSC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof ACSB) {
                        sb.append(ACSB.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof PAC) {
                        sb.append(PAC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof MAC) {
                        sb.append(MAC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof NAC) {
                        sb.append(NAC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof LCSB) {
                        sb.append(LCSB.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof PLC) {
                        sb.append(PLC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof MLC) {
                        sb.append(MLC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else if (this instanceof NLC) {
                        sb.append(NLC.select(subQuery.getSelectPropNames()).from(subQuery.getEntityName()).append(subCond).sql());
                    } else {
                        throw new RuntimeException("Unsupproted subQuery condition: " + cond);
                    }
                }
            }
        } else if (cond instanceof Expression) {
            // ==== version 1
            // sb.append(cond.toString());

            // ==== version 2
            //    final List<String> words = SQLParser.parse(((Expression) cond).getLiteral());
            //    final Map<String, String> propColumnNameMap = getPropColumnNameMap(entityClass, namingPolicy);
            //
            //    String word = null;
            //
            //    for (int i = 0, size = words.size(); i < size; i++) {
            //        word = words.get(i);
            //
            //        if ((i > 2) && WD.AS.equalsIgnoreCase(words.get(i - 2))) {
            //            sb.append(word);
            //        } else if ((i > 1) && WD.SPACE.equalsIgnoreCase(words.get(i - 1))
            //                && (propColumnNameMap.containsKey(words.get(i - 2)) || propColumnNameMap.containsValue(words.get(i - 2)))) {
            //            sb.append(word);
            //        } else {
            //            sb.append(formalizeColumnName(propColumnNameMap, word));
            //        }
            //    }

            // ==== version 3
            appendStringExpr(((Expression) cond).getLiteral());
        } else {
            throw new IllegalArgumentException("Unsupported condtion: " + cond.toString());
        }
    }

    /**
     * Checks if is sub query.
     *
     * @param columnNames
     * @return true, if is sub query
     */
    private boolean isSubQuery(final String... columnNames) {
        if (columnNames.length == 1) {
            int index = SQLParser.indexWord(columnNames[0], WD.SELECT, 0, false);

            if (index >= 0) {
                index = SQLParser.indexWord(columnNames[0], WD.FROM, index, false);

                return index >= 1;
            }
        }

        return false;
    }

    //    @Override
    //    public int hashCode() {
    //        return sb.hashCode();
    //    }
    //
    //    @Override
    //    public boolean equals(Object obj) {
    //        if (obj == this) {
    //            return true;
    //        }
    //
    //        if (obj instanceof SQLBuilder) {
    //            final SQLBuilder other = (SQLBuilder) obj;
    //
    //            return N.equals(this.sb, other.sb) && N.equals(this.parameters, other.parameters);
    //        }
    //
    //        return false;
    //    }

    public void println() {
        N.println(sql());
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return sql();
    }

    private String formalizeColumnName(final String propName) {
        return entityClass == null ? formalizeColumnName(propName, namingPolicy)
                : formalizeColumnName(getPropColumnNameMap(entityClass, namingPolicy), propName);
    }

    private static String formalizeColumnName(final String word, final NamingPolicy namingPolicy) {
        if (sqlKeyWords.contains(word)) {
            return word;
        } else if (namingPolicy == NamingPolicy.LOWER_CAMEL_CASE) {
            return ClassUtil.formalizePropName(word);
        } else {
            return namingPolicy.convert(word);
        }
    }

    private String formalizeColumnName(final Map<String, String> propColumnNameMap, final String propName) {
        String columnName = propColumnNameMap == null ? null : propColumnNameMap.get(propName);

        if (columnName != null) {
            return columnName;
        }

        if (aliasPropColumnNameMap != null && aliasPropColumnNameMap.size() > 0) {
            int index = propName.indexOf('.');

            if (index > 0) {
                final String alias = propName.substring(0, index);
                final Map<String, String> newPropColumnNameMap = aliasPropColumnNameMap.get(alias);

                if (newPropColumnNameMap != null) {
                    final String newPropName = propName.substring(index + 1);
                    columnName = newPropColumnNameMap.get(newPropName);

                    if (columnName != null) {
                        return alias + "." + columnName;
                    }
                }
            }
        }

        return formalizeColumnName(propName, namingPolicy);
    }

    private static void parseInsertEntity(final SQLBuilder instance, final Object entity, final Set<String> excludedPropNames) {
        if (entity instanceof String) {
            instance.columnNames = N.asArray((String) entity);
        } else if (entity instanceof Map) {
            if (N.isNullOrEmpty(excludedPropNames)) {
                instance.props = (Map<String, Object>) entity;
            } else {
                instance.props = new LinkedHashMap<>((Map<String, Object>) entity);
                Maps.removeKeys(instance.props, excludedPropNames);
            }
        } else {
            final Collection<String> propNames = getInsertPropNames(entity, excludedPropNames);
            final Map<String, Object> map = N.newHashMap(N.initHashCapacity(propNames.size()));
            final EntityInfo entityInfo = ParserUtil.getEntityInfo(entity.getClass());

            for (String propName : propNames) {
                map.put(propName, entityInfo.getPropValue(entity, propName));
            }

            instance.props = map;
        }
    }

    private static Collection<Map<String, Object>> toInsertPropsList(final Collection<?> propsList) {
        final Optional<?> first = N.firstNonNull(propsList);

        if (first.isPresent() && first.get() instanceof Map) {
            return (List<Map<String, Object>>) propsList;
        } else {
            final Class<?> entityClass = first.get().getClass();
            final Collection<String> propNames = getInsertPropNamesByClass(entityClass, null);
            final List<Map<String, Object>> newPropsList = new ArrayList<>(propsList.size());

            for (Object entity : propsList) {
                final Map<String, Object> props = N.newHashMap(N.initHashCapacity(propNames.size()));
                final EntityInfo entityInfo = ParserUtil.getEntityInfo(entityClass);

                for (String propName : propNames) {
                    props.put(propName, entityInfo.getPropValue(entity, propName));
                }

                newPropsList.add(props);
            }

            return newPropsList;
        }
    }

    static void checkMultiSelects(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
        N.checkArgNotNullOrEmpty(multiSelects, "multiSelects");

        for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
            N.checkArgNotNull(tp._1, "Class can't be null in 'multiSelects'");
        }
    }

    /**
     * The Enum SQLPolicy.
     */
    enum SQLPolicy {

        /** The sql. */
        SQL,
        /** The parameterized sql. */
        PARAMETERIZED_SQL,
        /** The named sql. */
        NAMED_SQL,
        /** The ibatis sql. */
        IBATIS_SQL;
    }

    /**
     * Un-parameterized SQL builder with snake case (lower case with underscore) field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * SCSB.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql();
     * // Output: SELECT first_name AS "firstName", last_name AS "lastName" FROM account WHERE id = 1
     * </code>
     * </pre>
     *
     * @deprecated {@code PSC or NSC} is preferred.
     */
    @Deprecated
    public static class SCSB extends SQLBuilder {

        /**
         * Instantiates a new scsb.
         */
        SCSB() {
            super(NamingPolicy.LOWER_CASE_WITH_UNDERSCORE, SQLPolicy.SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static SCSB createInstance() {
            return new SCSB();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.LOWER_CASE_WITH_UNDERSCORE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);

            return instance;
        }
    }

    /**
     * Un-parameterized SQL builder with all capitals case (upper case with underscore) field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(ACSB.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // Output: SELECT FIRST_NAME AS "firstName", LAST_NAME AS "lastName" FROM ACCOUNT WHERE ID = 1
     * </code>
     * </pre>
     *
     * @deprecated {@code PAC or NAC} is preferred.
     */
    @Deprecated
    public static class ACSB extends SQLBuilder {

        /**
         * Instantiates a new acsb.
         */
        ACSB() {
            super(NamingPolicy.UPPER_CASE_WITH_UNDERSCORE, SQLPolicy.SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static ACSB createInstance() {
            return new ACSB();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.UPPER_CASE_WITH_UNDERSCORE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);

            return instance;
        }
    }

    /**
     * Un-parameterized SQL builder with lower camel case field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(LCSB.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT firstName, lastName FROM account WHERE id = 1
     * </code>
     * </pre>
     *
     * @deprecated {@code PLC or NLC} is preferred.
     */
    @Deprecated
    public static class LCSB extends SQLBuilder {

        /**
         * Instantiates a new lcsb.
         */
        LCSB() {
            super(NamingPolicy.LOWER_CAMEL_CASE, SQLPolicy.SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static LCSB createInstance() {
            return new LCSB();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.LOWER_CAMEL_CASE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CAMEL_CASE);

            return instance;
        }
    }

    /**
     * Parameterized('?') SQL builder with snake case (lower case with underscore) field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(PSC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT first_name AS "firstName", last_name AS "lastName" FROM account WHERE id = ?
     * </code>
     * </pre>
     */
    public static class PSC extends SQLBuilder {

        /**
         * Instantiates a new psc.
         */
        PSC() {
            super(NamingPolicy.LOWER_CASE_WITH_UNDERSCORE, SQLPolicy.PARAMETERIZED_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static PSC createInstance() {
            return new PSC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.LOWER_CASE_WITH_UNDERSCORE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);

            return instance;
        }
    }

    /**
     * Parameterized('?') SQL builder with all capitals case (upper case with underscore) field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(PAC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT FIRST_NAME AS "firstName", LAST_NAME AS "lastName" FROM ACCOUNT WHERE ID = ?
     * </code>
     * </pre>
     */
    public static class PAC extends SQLBuilder {

        /**
         * Instantiates a new pac.
         */
        PAC() {
            super(NamingPolicy.UPPER_CASE_WITH_UNDERSCORE, SQLPolicy.PARAMETERIZED_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static PAC createInstance() {
            return new PAC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.UPPER_CASE_WITH_UNDERSCORE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);

            return instance;
        }
    }

    /**
     * Parameterized('?') SQL builder with lower camel case field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(PLC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT firstName, lastName FROM account WHERE id = ?
     * </code>
     * </pre>
     */
    public static class PLC extends SQLBuilder {

        /**
         * Instantiates a new plc.
         */
        PLC() {
            super(NamingPolicy.LOWER_CAMEL_CASE, SQLPolicy.PARAMETERIZED_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static PLC createInstance() {
            return new PLC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.LOWER_CAMEL_CASE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CAMEL_CASE);

            return instance;
        }
    }

    /**
     * Named SQL builder with snake case (lower case with underscore) field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(NSC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT first_name AS "firstName", last_name AS "lastName" FROM account WHERE id = :id
     * </code>
     * </pre>
     */
    public static class NSC extends SQLBuilder {

        /**
         * Instantiates a new nsc.
         */
        NSC() {
            super(NamingPolicy.LOWER_CASE_WITH_UNDERSCORE, SQLPolicy.NAMED_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static NSC createInstance() {
            return new NSC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.LOWER_CASE_WITH_UNDERSCORE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);

            return instance;
        }
    }

    /**
     * Named SQL builder with all capitals case (upper case with underscore) field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(NAC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT FIRST_NAME AS "firstName", LAST_NAME AS "lastName" FROM ACCOUNT WHERE ID = :id
     * </code>
     * </pre>
     */
    public static class NAC extends SQLBuilder {

        /**
         * Instantiates a new nac.
         */
        NAC() {
            super(NamingPolicy.UPPER_CASE_WITH_UNDERSCORE, SQLPolicy.NAMED_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static NAC createInstance() {
            return new NAC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.UPPER_CASE_WITH_UNDERSCORE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);

            return instance;
        }
    }

    /**
     * Named SQL builder with lower camel case field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(NLC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT firstName, lastName FROM account WHERE id = :id
     * </code>
     * </pre>
     */
    public static class NLC extends SQLBuilder {

        /**
         * Instantiates a new nlc.
         */
        NLC() {
            super(NamingPolicy.LOWER_CAMEL_CASE, SQLPolicy.NAMED_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static NLC createInstance() {
            return new NLC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.LOWER_CAMEL_CASE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CAMEL_CASE);

            return instance;
        }
    }

    /**
     * MyBatis-style SQL builder with lower camel case field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(MLC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT first_name AS "firstName", last_name AS "lastName" FROM account WHERE id = #{id}
     * </code>
     * </pre>
     */
    @Deprecated
    public static class MSC extends SQLBuilder {

        /**
         * Instantiates a new msc.
         */
        MSC() {
            super(NamingPolicy.LOWER_CASE_WITH_UNDERSCORE, SQLPolicy.IBATIS_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static MSC createInstance() {
            return new MSC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.LOWER_CASE_WITH_UNDERSCORE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CASE_WITH_UNDERSCORE);

            return instance;
        }
    }

    /**
     * MyBatis-style SQL builder with all capitals case (upper case with underscore) field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(MAC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT FIRST_NAME AS "firstName", LAST_NAME AS "lastName" FROM ACCOUNT WHERE ID = #{id}
     * </code>
     * </pre>
     */
    @Deprecated
    public static class MAC extends SQLBuilder {

        /**
         * Instantiates a new mac.
         */
        MAC() {
            super(NamingPolicy.UPPER_CASE_WITH_UNDERSCORE, SQLPolicy.IBATIS_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static MAC createInstance() {
            return new MAC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.UPPER_CASE_WITH_UNDERSCORE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.UPPER_CASE_WITH_UNDERSCORE);

            return instance;
        }
    }

    /**
     * MyBatis-style SQL builder with lower camel case field/column naming strategy.
     *
     * For example:
     * <pre>
     * <code>
     * N.println(MLC.select("firstName", "lastName").from("account").where(L.eq("id", 1)).sql());
     * // SELECT firstName, lastName FROM account WHERE id = #{id}
     * </code>
     * </pre>
     */
    @Deprecated
    public static class MLC extends SQLBuilder {

        /**
         * Instantiates a new mlc.
         */
        MLC() {
            super(NamingPolicy.LOWER_CAMEL_CASE, SQLPolicy.IBATIS_SQL);
        }

        /**
         * Creates the instance.
         *
         * @return
         */
        static MLC createInstance() {
            return new MLC();
        }

        /**
         * To generate {@code sql} part for the specified {@code cond} only.
         *
         * @param cond
         * @param entityClass
         * @return
         */
        public static SQLBuilder parse(final Condition cond, final Class<?> entityClass) {
            N.checkArgNotNull(cond, "cond");

            final SQLBuilder instance = createInstance();

            instance.entityClass = entityClass;
            instance.op = OperationType.QUERY;
            instance.append(cond);

            return instance;
        }

        /**
         *
         * @param expr
         * @return
         */
        public static SQLBuilder insert(final String expr) {
            return insert(N.asArray(expr));
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder insert(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder insert(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param props
         * @return
         */
        public static SQLBuilder insert(final Map<String, Object> props) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.props = props;

            return instance;
        }

        /**
         *
         * @param entity
         * @return
         */
        public static SQLBuilder insert(final Object entity) {
            return insert(entity, null);
        }

        /**
         *
         * @param entity
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Object entity, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entity.getClass();

            parseInsertEntity(instance, entity, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass) {
            return insert(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insert(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            instance.entityClass = entityClass;
            instance.columnNameList = getInsertPropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass) {
            return insertInto(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder insertInto(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return insert(entityClass, excludedPropNames).into(entityClass);
        }

        /**
         * Generate the MySQL style batch insert sql.
         *
         * @param propsList list of entity or properties map.
         * @return
         */
        @Beta
        public static SQLBuilder batchInsert(final Collection<?> propsList) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.ADD;
            final Optional<?> first = N.firstNonNull(propsList);

            if (first.isPresent()) {
                instance.entityClass = first.get().getClass();
            }

            instance.propsList = toInsertPropsList(propsList);

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        @SafeVarargs
        public static SQLBuilder select(final String... columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final String[] columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNames = columnNames;

            return instance;
        }

        /**
         *
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnNames
         * @return
         */
        public static SQLBuilder select(final String expr, final Collection<String> columnNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnNameList = columnNames;

            return instance;
        }

        /**
         *
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @param columnAliases
         * @return
         */
        public static SQLBuilder select(final String expr, final Map<String, String> columnAliases) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.predicates = expr;
            instance.columnAliases = columnAliases;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass) {
            return select(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return select(entityClass, includeSubEntityProperties, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return select(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder select(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = entityClass;
            instance.columnNameList = getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass) {
            return selectFrom(entityClass, false);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties) {
            return selectFrom(entityClass, includeSubEntityProperties, (Set<String>) null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames) {
            return selectFrom(entityClass, false, excludedPropNames);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        /**
         *
         * @param entityClass
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final String expr) {
            return selectFrom(entityClass, false, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final String expr) {
            return selectFrom(entityClass, includeSubEntityProperties, null, expr);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final Set<String> excludedPropNames, final String expr) {
            return selectFrom(entityClass, false, excludedPropNames, expr);
        }

        /**
         *
         * @param entityClass
         * @param includeSubEntityProperties
         * @param excludedPropNames
         * @param expr <code>ALL | DISTINCT | DISTINCTROW...</code>
         * @return
         */
        public static SQLBuilder selectFrom(final Class<?> entityClass, final boolean includeSubEntityProperties, final Set<String> excludedPropNames,
                final String expr) {
            if (includeSubEntityProperties) {
                final List<String> selectTableNames = getSelectTableNames(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass, selectTableNames);
            } else {
                return select(expr, getSelectPropNamesByClass(entityClass, includeSubEntityProperties, excludedPropNames)).from(entityClass);
            }
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return select(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return select(multiSelects);
        }

        public static SQLBuilder select(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return select(multiSelects);
        }

        public static SQLBuilder select(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final SQLBuilder instance = createInstance();

            instance.op = OperationType.QUERY;
            instance.entityClass = multiSelects.get(0)._1;
            instance.multiSelects = multiSelects;

            return instance;
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Class<?> entityClassB, final String tableAliasB,
                final Class<?> entityClassC, final String tableAliasC) {
            return selectFrom(entityClassA, tableAliasA, null, entityClassB, tableAliasB, null, entityClassC, tableAliasC, null);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final Class<?> entityClassA, final String tableAliasA, final Set<String> excludedPropNamesA,
                final Class<?> entityClassB, final String tableAliasB, final Set<String> excludedPropNamesB, final Class<?> entityClassC,
                final String tableAliasC, final Set<String> excludedPropNamesC) {
            final List<Tuple3<Class<?>, String, Set<String>>> multiSelects = N.asList(
                    Tuple.<Class<?>, String, Set<String>> of(entityClassA, tableAliasA, excludedPropNamesA),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassB, tableAliasB, excludedPropNamesB),
                    Tuple.<Class<?>, String, Set<String>> of(entityClassC, tableAliasC, excludedPropNamesC));

            return selectFrom(multiSelects);
        }

        public static SQLBuilder selectFrom(final List<Tuple3<Class<?>, String, Set<String>>> multiSelects) {
            checkMultiSelects(multiSelects);

            final NamingPolicy namingPolicy = NamingPolicy.LOWER_CAMEL_CASE;

            if (multiSelects.size() == 1) {
                final Tuple3<Class<?>, String, Set<String>> tp = multiSelects.get(0);

                if (N.isNullOrEmpty(tp._2)) {
                    return selectFrom(tp._1, tp._3);
                } else {
                    return select(tp._1, tp._3).from(getTableName(tp._1, namingPolicy) + " " + tp._2);
                }
            } else if (multiSelects.size() == 2) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) + ", "
                        + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2);

                return select(multiSelects).from(fromClause);
            } else if (multiSelects.size() == 3) {
                final Tuple3<Class<?>, String, Set<String>> tpA = multiSelects.get(0);
                final Tuple3<Class<?>, String, Set<String>> tpB = multiSelects.get(1);
                final Tuple3<Class<?>, String, Set<String>> tpC = multiSelects.get(2);

                final String fromClause = getTableName(tpA._1, namingPolicy) + (N.isNullOrEmpty(tpA._2) ? "" : " " + tpA._2) //
                        + ", " + getTableName(tpB._1, namingPolicy) + (N.isNullOrEmpty(tpB._2) ? "" : " " + tpB._2) //
                        + ", " + getTableName(tpC._1, namingPolicy) + (N.isNullOrEmpty(tpC._2) ? "" : " " + tpC._2);

                return select(multiSelects).from(fromClause);
            } else {
                final StringBuilder sb = Objectory.createStringBuilder();
                int idx = 0;

                for (Tuple3<Class<?>, String, Set<String>> tp : multiSelects) {
                    if (idx++ > 0) {
                        sb.append(_COMMA_SPACE);
                    }

                    sb.append(getTableName(tp._1, namingPolicy));

                    if (N.notNullOrEmpty(tp._2)) {
                        sb.append(' ').append(tp._2);
                    }
                }

                String fromClause = sb.toString();

                Objectory.recycle(sb);

                return select(multiSelects).from(fromClause);
            }
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder update(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass) {
            return update(entityClass, null);
        }

        /**
         *
         * @param entityClass
         * @param excludedPropNames
         * @return
         */
        public static SQLBuilder update(final Class<?> entityClass, final Set<String> excludedPropNames) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.UPDATE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CAMEL_CASE);
            instance.columnNameList = getUpdatePropNamesByClass(entityClass, excludedPropNames);

            return instance;
        }

        /**
         *
         * @param tableName
         * @return
         */
        public static SQLBuilder deleteFrom(final String tableName) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.tableName = tableName;

            return instance;
        }

        /**
         *
         * @param entityClass
         * @return
         */
        public static SQLBuilder deleteFrom(final Class<?> entityClass) {
            final SQLBuilder instance = createInstance();

            instance.op = OperationType.DELETE;
            instance.entityClass = entityClass;
            instance.tableName = getTableName(entityClass, NamingPolicy.LOWER_CAMEL_CASE);

            return instance;
        }
    }

    /**
     * The Class SP.
     */
    public static final class SP {

        /** The sql. */
        public final String sql;

        /** The parameters. */
        public final List<Object> parameters;

        /**
         * Instantiates a new sp.
         *
         * @param sql
         * @param parameters
         */
        SP(final String sql, final List<Object> parameters) {
            this.sql = sql;
            this.parameters = ImmutableList.of(parameters);
        }

        /**
         *
         * @return
         * @deprecated useless?
         */
        @Deprecated
        public Pair<String, List<Object>> __() {
            return Pair.of(sql, parameters);
        }

        /**
         *
         * @return
         */
        @Override
        public int hashCode() {
            return N.hashCode(sql) * 31 + N.hashCode(parameters);
        }

        /**
         *
         * @param obj
         * @return true, if successful
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof SP) {
                SP other = (SP) obj;

                return N.equals(other.sql, sql) && N.equals(other.parameters, parameters);
            }

            return false;
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return "{sql=" + sql + ", parameters=" + N.toString(parameters) + "}";
        }
    }
}
