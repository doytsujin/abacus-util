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

package com.landawn.abacus.parser;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.landawn.abacus.annotation.AccessFieldByMethod;
import com.landawn.abacus.annotation.Column;
import com.landawn.abacus.annotation.Internal;
import com.landawn.abacus.annotation.JsonXmlField;
import com.landawn.abacus.annotation.Table;
import com.landawn.abacus.annotation.Type.EnumBy;
import com.landawn.abacus.annotation.Type.Scope;
import com.landawn.abacus.core.DirtyMarkerUtil;
import com.landawn.abacus.logging.Logger;
import com.landawn.abacus.logging.LoggerFactory;
import com.landawn.abacus.type.Type;
import com.landawn.abacus.util.CharacterWriter;
import com.landawn.abacus.util.ClassUtil;
import com.landawn.abacus.util.DateUtil;
import com.landawn.abacus.util.HBaseColumn;
import com.landawn.abacus.util.ImmutableList;
import com.landawn.abacus.util.ImmutableMap;
import com.landawn.abacus.util.Multiset;
import com.landawn.abacus.util.N;
import com.landawn.abacus.util.ObjectPool;
import com.landawn.abacus.util.Splitter;
import com.landawn.abacus.util.StringUtil;
import com.landawn.abacus.util.WD;

// TODO: Auto-generated Javadoc
/**
 * The Class ParserUtil.
 *
 * @author Haiyang Li
 * @since 0.8
 */
@Internal
public final class ParserUtil {

    /** The Constant logger. */
    static final Logger logger = LoggerFactory.getLogger(ParserUtil.class);

    /** The Constant PROP_INFO_MASK. */
    private static final PropInfo PROP_INFO_MASK = new PropInfo("PROP_INFO_MASK");

    /** The Constant PROP_NAME_SEPARATOR. */
    private static final String PROP_NAME_SEPARATOR = ".".intern();

    /** The Constant GET. */
    // ...
    private static final String GET = "get".intern();

    /** The Constant SET. */
    private static final String SET = "set".intern();

    /** The Constant IS. */
    private static final String IS = "is".intern();

    /** The Constant HAS. */
    private static final String HAS = "has".intern();

    /** The Constant POOL_SIZE. */
    private static final int POOL_SIZE = 1024;

    /** The Constant entityInfoPool. */
    // ...
    private static final Map<Class<?>, EntityInfo> entityInfoPool = new ObjectPool<>(POOL_SIZE);

    /**
     * Checks if is serializable.
     *
     * @param field
     * @return true, if is serializable
     */
    static boolean isSerializable(final Field field) {
        if (field == null || Modifier.isStatic(field.getModifiers())) {
            return false;
        }

        if (field.isAnnotationPresent(JsonXmlField.class) && field.getAnnotation(JsonXmlField.class).ignore()) {
            return false;
        }

        try {
            if (field.isAnnotationPresent(com.alibaba.fastjson.annotation.JSONField.class)
                    && field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).serialize() == false) {
                return false;
            }
        } catch (Throwable e) {
            // ignore
        }

        try {
            if (field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIgnore.class)
                    && field.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class).value()) {
                return false;
            }
        } catch (Throwable e) {
            // ignore
        }

        return true;
    }

    /**
     * Gets the date format.
     *
     * @param field
     * @return
     */
    static String getDateFormat(final Field field) {
        if (field == null) {
            return null;
        }

        if (field.isAnnotationPresent(JsonXmlField.class) && N.notNullOrEmpty(field.getAnnotation(JsonXmlField.class).dateFormat())) {
            return field.getAnnotation(JsonXmlField.class).dateFormat();
        }

        try {
            if (field.isAnnotationPresent(com.alibaba.fastjson.annotation.JSONField.class)
                    && N.notNullOrEmpty(field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).format())) {
                return field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).format();
            }
        } catch (Throwable e) {
            // ignore
        }

        try {
            if (field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonFormat.class)
                    && N.notNullOrEmpty(field.getAnnotation(com.fasterxml.jackson.annotation.JsonFormat.class).pattern())) {
                return field.getAnnotation(com.fasterxml.jackson.annotation.JsonFormat.class).pattern();
            }
        } catch (Throwable e) {
            // ignore
        }

        return null;
    }

    /**
     * Gets the time zone.
     *
     * @param field
     * @return
     */
    static String getTimeZone(final Field field) {
        if (field == null) {
            return null;
        }

        if (field.isAnnotationPresent(JsonXmlField.class) && N.notNullOrEmpty(field.getAnnotation(JsonXmlField.class).timeZone())) {
            return field.getAnnotation(JsonXmlField.class).timeZone();
        }

        try {
            if (field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonFormat.class)
                    && N.notNullOrEmpty(field.getAnnotation(com.fasterxml.jackson.annotation.JsonFormat.class).timezone())) {
                return field.getAnnotation(com.fasterxml.jackson.annotation.JsonFormat.class).timezone();
            }
        } catch (Throwable e) {
            // ignore
        }

        return null;
    }

    /**
     * Gets the number format.
     *
     * @param field
     * @return
     */
    static String getNumberFormat(final Field field) {
        if (field == null) {
            return null;
        }

        if (field.isAnnotationPresent(JsonXmlField.class) && N.notNullOrEmpty(field.getAnnotation(JsonXmlField.class).numberFormat())) {
            return field.getAnnotation(JsonXmlField.class).numberFormat();
        }

        return null;
    }

    /**
     * Gets the JSON name.
     *
     * @param propName
     * @param field
     * @return
     */
    static String getJsonXmlName(final String propName, final Field field) {
        if (field == null) {
            return propName;
        }

        if (field.isAnnotationPresent(JsonXmlField.class) && N.notNullOrEmpty(field.getAnnotation(JsonXmlField.class).name())) {
            return field.getAnnotation(JsonXmlField.class).name();
        }

        try {
            if (field.isAnnotationPresent(com.alibaba.fastjson.annotation.JSONField.class)
                    && N.notNullOrEmpty(field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).name())) {
                return field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).name();
            }
        } catch (Throwable e) {
            // ignore
        }

        try {
            if (field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonProperty.class)
                    && N.notNullOrEmpty(field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class).value())) {
                return field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class).value();
            }
        } catch (Throwable e) {
            // ignore
        }

        return propName;
    }

    /**
     * Gets the entity info.
     *
     * @param cls
     * @return
     */
    public static EntityInfo getEntityInfo(Class<?> cls) {
        if (!ClassUtil.isEntity(cls)) {
            throw new IllegalArgumentException(
                    "No property getter/setter method or public field found in the specified entity: " + ClassUtil.getCanonicalClassName(cls));
        }

        EntityInfo entityInfo = entityInfoPool.get(cls);

        if (entityInfo == null) {
            synchronized (entityInfoPool) {
                entityInfo = entityInfoPool.get(cls);

                if (entityInfo == null) {
                    entityInfo = new EntityInfo(cls);
                    entityInfoPool.put(cls, entityInfo);
                }
            }
        }

        return entityInfo;
    }

    /**
     * Refresh entity prop info.
     *
     * @param cls
     * @deprecated internal use only.
     */
    @Deprecated
    @Internal
    public static void refreshEntityPropInfo(Class<?> cls) {
        synchronized (entityInfoPool) {
            entityInfoPool.remove(cls);
        }
    }

    /**
     * The Class EntityInfo.
     */
    public static class EntityInfo implements JSONReader.SymbolReader {

        /** The name. */
        final String name;

        /** The cls. */
        public final Class<Object> cls;

        /** The type. */
        public final Type<Object> type;

        /** The prop info list. */
        public final ImmutableList<PropInfo> propInfoList;

        public final ImmutableMap<Class<? extends Annotation>, Annotation> annotations;

        /** The type name. */
        final String typeName;

        /** The json info. */
        final JSONInfo jsonInfo;

        /** The xml info. */
        final XMLInfo xmlInfo;

        /** The prop infos. */
        final PropInfo[] propInfos;

        /** The seri prop infos. */
        final PropInfo[] seriPropInfos;

        /** The non transient seri prop infos. */
        final PropInfo[] nonTransientSeriPropInfos;

        /** The transient seri prop infos. */
        final PropInfo[] transientSeriPropInfos;

        /** The transient seri prop name set. */
        final Set<String> transientSeriPropNameSet = N.newHashSet();

        /** The prop info map. */
        private final Map<String, PropInfo> propInfoMap;

        /** The prop info queue map. */
        private final Map<String, List<PropInfo>> propInfoQueueMap;

        /** The prop info array. */
        private final PropInfo[] propInfoArray;

        /** The hash prop info map. */
        private final Map<Integer, PropInfo> hashPropInfoMap;

        public final String tableName;

        /**
         * Instantiates a new entity info.
         *
         * @param cls
         */
        public EntityInfo(Class<?> cls) {
            name = ClassUtil.formalizePropName(cls.getSimpleName());
            this.cls = (Class<Object>) cls;
            type = N.typeOf(cls);
            typeName = type.name();
            jsonInfo = new JSONInfo(name);
            xmlInfo = new XMLInfo(name, typeName, true);

            this.annotations = ImmutableMap.of(getAnnotations(cls));

            final List<String> propNameList = ClassUtil.getPropNameList(cls);
            final List<PropInfo> seriPropInfoList = new ArrayList<>();
            final List<PropInfo> nonTransientSeriPropInfoList = new ArrayList<>();
            final List<PropInfo> transientSeriPropInfoList = new ArrayList<>();

            propInfos = new PropInfo[propNameList.size()];
            propInfoMap = new ObjectPool<>((propNameList.size() + 1) * 2);
            propInfoQueueMap = new ObjectPool<>((propNameList.size() + 1) * 2);
            hashPropInfoMap = new ObjectPool<>((propNameList.size() + 1) * 2);

            PropInfo propInfo = null;
            String xmlPropName = null;
            String jsonPropName = null;
            int i = 0;

            final Multiset<Integer> multiSet = new Multiset<>(propNameList.size() + 16);
            int maxLength = 0;
            Field field = null;
            Method getMethod = null;

            for (String propName : propNameList) {
                field = ClassUtil.getPropField(cls, propName);
                getMethod = ClassUtil.getPropGetMethod(cls, propName);
                xmlPropName = getJsonXmlName(propName, field);
                jsonPropName = getJsonXmlName(propName, field);

                propInfo = ASMUtil.isASMAvailable() ? new ASMPropInfo(propName, xmlPropName, jsonPropName, field, getMethod, annotations)
                        : new PropInfo(propName, xmlPropName, jsonPropName, field, getMethod, annotations);

                propInfos[i++] = propInfo;
                propInfoMap.put(propName, propInfo);
                propInfoMap.put(xmlPropName, propInfo);
                propInfoMap.put(jsonPropName, propInfo);

                if (N.notNullOrEmpty(propInfo.columnName) && !propInfoMap.containsKey(propInfo.columnName)) {
                    propInfoMap.put(propInfo.columnName, propInfo);
                }

                if (isSerializable(propInfo.field) == false) {
                    // skip
                } else {
                    seriPropInfoList.add(propInfo);

                    if (propInfo.field != null && Modifier.isTransient(propInfo.field.getModifiers())) {
                        transientSeriPropNameSet.add(propName);

                        transientSeriPropInfoList.add(propInfo);
                    } else {
                        nonTransientSeriPropInfoList.add(propInfo);
                    }
                }

                multiSet.add(propInfo.jsonInfo.name.length);
                maxLength = Math.max(propInfo.jsonInfo.name.length, maxLength);
            }

            seriPropInfos = seriPropInfoList.toArray(new PropInfo[seriPropInfoList.size()]);
            nonTransientSeriPropInfos = nonTransientSeriPropInfoList.toArray(new PropInfo[nonTransientSeriPropInfoList.size()]);
            transientSeriPropInfos = transientSeriPropInfoList.toArray(new PropInfo[transientSeriPropInfoList.size()]);

            propInfoArray = new PropInfo[maxLength + 1];

            for (PropInfo e : propInfos) {
                hashPropInfoMap.put(hashCode(e.jsonInfo.name), e);

                if (multiSet.get(e.jsonInfo.name.length) == 1) {
                    propInfoArray[e.jsonInfo.name.length] = e;
                }
            }

            this.propInfoList = ImmutableList.of(propInfos);

            String tmpTableName = null;

            if (this.annotations.containsKey(Table.class)) {
                tmpTableName = ((Table) this.annotations.get(Table.class)).value();
            } else {
                try {
                    if (this.annotations.containsKey(javax.persistence.Table.class)) {
                        tmpTableName = ((javax.persistence.Table) this.annotations.get(javax.persistence.Table.class)).name();
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }

            this.tableName = tmpTableName;
        }

        /**
         * Gets the prop info.
         *
         * @param propName
         * @return
         */
        public PropInfo getPropInfo(String propName) {
            // slower?
            // int len = propName.length();
            //
            // if (len < propInfoArray.length && propInfoArray[len] != null &&
            // propInfoArray[len].name.equals(propName)) {
            // return propInfoArray[len];
            // }

            PropInfo propInfo = propInfoMap.get(propName);

            if (propInfo == null) {
                if (propInfo == null) {
                    Method method = ClassUtil.getPropGetMethod(cls, propName);

                    if (method != null) {
                        propInfo = propInfoMap.get(ClassUtil.getPropNameByMethod(method));
                    }
                }

                if (propInfo == null) {
                    for (String key : propInfoMap.keySet()) {
                        if (isPropName(cls, propName, key)) {
                            propInfo = propInfoMap.get(key);

                            break;
                        }
                    }

                    if ((propInfo == null) && !propName.equalsIgnoreCase(ClassUtil.formalizePropName(propName))) {
                        propInfo = getPropInfo(ClassUtil.formalizePropName(propName));
                    }
                }

                // set method mask to avoid query next time.
                if (propInfo == null) {
                    propInfo = PROP_INFO_MASK;
                } else {
                    if (propInfo == PROP_INFO_MASK) {
                        // ignore.
                    } else {
                        hashPropInfoMap.put(hashCode(propInfo.jsonInfo.name), propInfo);
                    }
                }

                //                if (propInfo == PROP_INFO_MASK) {
                //                    // logger.warn("No property info found by name: " + propName + " in class: " + cls.getCanonicalName());
                //                    String msg = "No property info found by name: " + propName + " in class: " + cls.getCanonicalName() + ". The defined properties are: "
                //                            + propInfoMap;
                //                    logger.error(msg, new RuntimeException(msg));
                //                }

                propInfoMap.put(propName, propInfo);
            }

            return (propInfo == PROP_INFO_MASK) ? null : propInfo;
        }

        /**
         * Gets the prop value.
         *
         * @param <T>
         * @param obj
         * @param propName
         * @return
         */
        @SuppressWarnings("unchecked")
        public <T> T getPropValue(Object obj, String propName) {
            PropInfo propInfo = getPropInfo(propName);

            if (propInfo == null) {
                final List<PropInfo> propInfoQueue = getPropInfoQueue(propName);

                if (propInfoQueue.size() == 0) {
                    throw new RuntimeException("No property method found with property name: " + propName + " in class: " + cls.getCanonicalName());
                } else {
                    Object propEntity = obj;

                    for (int i = 0, len = propInfoQueue.size(); i < len; i++) {
                        propEntity = propInfoQueue.get(i).getPropValue(propEntity);

                        if (propEntity == null) {
                            return (T) propInfoQueue.get(len - 1).type.defaultValue();
                        }
                    }

                    return (T) propEntity;
                }
            } else {
                return propInfo.getPropValue(obj);
            }
        }

        public void setPropValue(final Object obj, final String propName, final Object propValue) {
            setPropValue(obj, propName, propValue, false);
        }

        /**
         * Sets the prop value.
         *
         * @param obj
         * @param propName
         * @param propValue
         * @param ignoreUnknownProperty
         */
        public boolean setPropValue(final Object obj, final String propName, final Object propValue, final boolean ignoreUnknownProperty) {
            PropInfo propInfo = getPropInfo(propName);

            if (propInfo == null) {
                final List<PropInfo> propInfoQueue = getPropInfoQueue(propName);

                if (propInfoQueue.size() == 0) {
                    if (!ignoreUnknownProperty) {
                        throw new RuntimeException("No property method found with property name: " + propName + " in class: " + cls.getCanonicalName());
                    } else {
                        return false;
                    }
                } else {
                    Object propEntity = obj;
                    Object subPropValue = null;

                    for (int i = 0, len = propInfoQueue.size(); i < len; i++) {
                        propInfo = propInfoQueue.get(i);

                        if (i == (len - 1)) {
                            propInfo.setPropValue(propEntity, propValue);
                        } else {
                            subPropValue = propInfo.getPropValue(propEntity);

                            if (subPropValue == null) {
                                subPropValue = N.newInstance(propInfo.clazz);
                                propInfo.setPropValue(propEntity, subPropValue);
                            }

                            propEntity = subPropValue;
                        }
                    }
                }
            } else {
                propInfo.setPropValue(obj, propValue);
            }

            return true;
        }

        /**
         * Checks if is prop name.
         *
         * @param cls
         * @param inputPropName
         * @param propNameByMethod
         * @return true, if is prop name
         */
        private boolean isPropName(Class<?> cls, String inputPropName, String propNameByMethod) {
            if (inputPropName.length() > 128) {
                throw new RuntimeException("The property name execeed 128: " + inputPropName);
            }

            inputPropName = inputPropName.trim();

            return inputPropName.equalsIgnoreCase(propNameByMethod) || inputPropName.replace(WD.UNDERSCORE, N.EMPTY_STRING).equalsIgnoreCase(propNameByMethod)
                    || inputPropName.equalsIgnoreCase(ClassUtil.getSimpleClassName(cls) + WD._PERIOD + propNameByMethod)
                    || (inputPropName.startsWith(GET) && inputPropName.substring(3).equalsIgnoreCase(propNameByMethod))
                    || (inputPropName.startsWith(SET) && inputPropName.substring(3).equalsIgnoreCase(propNameByMethod))
                    || (inputPropName.startsWith(IS) && inputPropName.substring(2).equalsIgnoreCase(propNameByMethod))
                    || (inputPropName.startsWith(HAS) && inputPropName.substring(2).equalsIgnoreCase(propNameByMethod));
        }

        /**
         * Gets the prop info queue.
         *
         * @param propName
         * @return
         */
        private List<PropInfo> getPropInfoQueue(String propName) {
            List<PropInfo> propInfoQueue = propInfoQueueMap.get(propName);

            if (propInfoQueue == null) {
                propInfoQueue = new ArrayList<>();

                final String[] strs = Splitter.with(PROP_NAME_SEPARATOR).splitToArray(propName);

                if (strs.length > 1) {
                    Class<?> propClass = cls;

                    PropInfo propInfo = null;

                    for (int i = 0, len = strs.length; i < len; i++) {
                        propInfo = getEntityInfo(propClass).getPropInfo(strs[i]);

                        if (propInfo == null) {
                            propInfoQueue.clear();

                            break;
                        }

                        propInfoQueue.add(propInfo);

                        propClass = propInfo.clazz;
                    }
                }

                propInfoQueueMap.put(propName, propInfoQueue);
            }

            return propInfoQueue;
        }

        /**
         * Read prop info.
         *
         * @param cbuf
         * @param from
         * @param to
         * @return
         */
        @Override
        public PropInfo readPropInfo(final char[] cbuf, int from, int to) {
            int len = to - from;

            if (len == 0) {
                return null;
            }

            PropInfo propInfo = null;

            if (len < propInfoArray.length) {
                propInfo = propInfoArray[len];
            }

            if (propInfo == null) {
                propInfo = hashPropInfoMap.get(hashCode(cbuf, from, to));
            }

            if (propInfo != null) {
                // TODO skip the comparison for performance improvement or:
                //
                // for (int i = 0; i < len; i += 2) {
                // if (cbuf[i + from] == propInfo.jsonInfo.name[i]) {
                // // continue;
                // } else {
                // propInfo = null;
                //
                // break;
                // }
                // }
                //

                final char[] tmp = propInfo.jsonInfo.name;

                if (tmp.length == len) {
                    for (int i = 0; i < len; i++) {
                        if (cbuf[i + from] == tmp[i]) {
                            // continue;
                        } else {
                            return null;
                        }
                    }
                }
            }

            return propInfo;
        }

        // @Override
        // public PropInfo readPropInfo(String str, int from, int to) {
        // if (N.isCharsOfStringReadable()) {
        // return readPropInfo(StrUtil.getCharsForReadOnly(str), from, to);
        // }
        //
        // int len = to - from;
        //
        // if (len == 0) {
        // return null;
        // }
        //
        // PropInfo propInfo = null;
        //
        // if (len < propInfoArray.length) {
        // propInfo = propInfoArray[len];
        // }
        //
        // if (propInfo == null) {
        // propInfo = hashPropInfoMap.get(hashCode(str, from, to));
        // }
        //
        // if (propInfo != null) {
        // // TODO skip the comparison for performance improvement or:
        // //
        // // for (int i = 0; i < len; i += 2) {
        // // if (str.charAt(i + from) == propInfo.jsonInfo.name[i]) {
        // // // continue;
        // // } else {
        // // propInfo = null;
        // //
        // // break;
        // // }
        // // }
        //
        // final char[] tmp = propInfo.jsonInfo.name;
        // for (int i = 0; i < len; i++) {
        // if (str.charAt(i + from) == tmp[i]) {
        // // continue;
        // } else {
        // return null;
        // }
        // }
        // }
        //
        // return propInfo;
        // }

        public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
            return annotations.containsKey(annotationClass);
        }

        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return (T) annotations.get(annotationClass);
        }

        private Map<Class<? extends Annotation>, Annotation> getAnnotations(final Class<?> cls) {
            final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

            final Set<Class<?>> classes = ClassUtil.getAllSuperTypes(cls);
            N.reverse(classes);
            classes.add(cls);

            for (Class<?> clazz : classes) {
                if (N.notNullOrEmpty(clazz.getAnnotations())) {
                    for (Annotation anno : clazz.getAnnotations()) {
                        annotations.put(anno.annotationType(), anno);
                    }
                }
            }

            return annotations;
        }

        /**
         *
         * @return
         */
        @Override
        public int hashCode() {
            return (cls == null) ? 0 : cls.hashCode();
        }

        /**
         *
         * @param obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof EntityInfo && N.equals(((EntityInfo) obj).cls, cls));
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return ClassUtil.getCanonicalClassName(cls);
        }

        /**
         *
         * @param a
         * @return
         */
        private int hashCode(char[] a) {
            int result = 1;

            for (char e : a) {
                result = 31 * result + e;
            }

            return result;
        }

        /**
         *
         * @param a
         * @param from
         * @param to
         * @return
         */
        private int hashCode(char[] a, int from, int to) {
            return N.hashCode(a, from, to);
        }

        // private int hashCode(String str, int from, int to) {
        // int result = 1;
        //
        // for (int i = from; i < to; i++) {
        // result = 31 * result + str.charAt(i);
        // }
        //
        // return result;
        // }
    }

    /**
     * The Class PropInfo.
     */
    public static class PropInfo {

        /** The Constant NULL_CHAR_ARRAY. */
        static final char[] NULL_CHAR_ARRAY = "null".toCharArray();

        public final Class<Object> declaringClass;

        /** The name. */
        public final String name;

        /** The clazz. */
        public final Class<Object> clazz;

        /** The type. */
        public final Type<Object> type;

        /** The field. */
        public final Field field;

        /** The get method. */
        public final Method getMethod;

        /** The set method. */
        public final Method setMethod;

        public final ImmutableMap<Class<? extends Annotation>, Annotation> annotations;

        /** The json xml type. */
        public final Type<Object> jsonXmlType;

        /** The db type. */
        public final Type<Object> dbType;

        /** The json info. */
        final JSONInfo jsonInfo;

        /** The xml info. */
        final XMLInfo xmlInfo;

        final boolean isFieldAccessible;

        /** The is dirty mark. */
        final boolean isDirtyMark;

        /** The date format. */
        final String dateFormat;

        /** The time zone. */
        final TimeZone timeZone;

        final ZoneId zoneId;

        final DateTimeFormatter dateTimeFormatter;

        final JodaDateTimeFormatterHolder jodaDTFH;

        /** The is long date format. */
        final boolean isLongDateFormat;

        /** The number format. */
        final NumberFormat numberFormat;

        /** The has format. */
        final boolean hasFormat;

        public final String columnName;

        /**
         * Instantiates a new prop info.
         *
         * @param propName
         */
        PropInfo(String propName) {
            this.declaringClass = null;
            this.name = propName;
            field = null;
            getMethod = null;
            setMethod = null;
            annotations = ImmutableMap.empty();
            clazz = null;
            type = null;

            jsonXmlType = null;
            dbType = null;
            xmlInfo = null;
            jsonInfo = null;
            isDirtyMark = false;
            isFieldAccessible = false;
            dateFormat = null;
            timeZone = null;
            zoneId = null;
            dateTimeFormatter = null;
            jodaDTFH = null;
            isLongDateFormat = false;
            numberFormat = null;
            hasFormat = false;

            columnName = null;
        }

        /**
         * Instantiates a new prop info.
         *
         * @param propName
         * @param xmlPropName
         * @param jsonPropName
         * @param getMethod
         * @param classAnnotations
         */
        public PropInfo(final String propName, final String xmlPropName, final String jsonPropName, final Field field, final Method getMethod,
                final ImmutableMap<Class<? extends Annotation>, Annotation> classAnnotations) {
            this.declaringClass = (Class<Object>) (field != null ? field.getDeclaringClass() : getMethod.getDeclaringClass());
            this.isDirtyMark = DirtyMarkerUtil.isDirtyMarker(declaringClass);
            this.field = field;
            this.name = propName;
            this.getMethod = getMethod;
            this.setMethod = ClassUtil.getPropSetMethod(declaringClass, propName);
            this.annotations = ImmutableMap.of(getAnnotations());
            this.clazz = (Class<Object>) (field == null ? (setMethod == null ? getMethod.getReturnType() : setMethod.getParameterTypes()[0]) : field.getType());
            this.type = getType(getAnnoType(this.field, this.getMethod, this.setMethod, clazz), this.field, this.getMethod, this.setMethod, clazz,
                    declaringClass);
            this.jsonXmlType = getType(getJsonXmlAnnoType(this.field, this.getMethod, this.setMethod, clazz), this.field, this.getMethod, this.setMethod, clazz,
                    declaringClass);
            this.dbType = getType(getDBAnnoType(this.field, this.getMethod, this.setMethod, clazz), this.field, this.getMethod, this.setMethod, clazz,
                    declaringClass);

            this.jsonInfo = new JSONInfo(jsonPropName);
            this.xmlInfo = new XMLInfo(xmlPropName, jsonXmlType.name(), false);

            if (field != null && !this.annotations.containsKey(AccessFieldByMethod.class) && !classAnnotations.containsKey(AccessFieldByMethod.class)) {
                ClassUtil.setAccessibleQuietly(field, true);
            }

            isFieldAccessible = isDirtyMark == false && field != null && field.isAccessible();

            String timeZoneStr = StringUtil.trim(getTimeZone(field));
            String dateFormatStr = StringUtil.trim(getDateFormat(field));
            this.dateFormat = N.isNullOrEmpty(dateFormatStr) ? null : dateFormatStr;
            this.timeZone = N.isNullOrEmpty(timeZoneStr) ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneStr);
            this.zoneId = timeZone.toZoneId();
            this.dateTimeFormatter = N.isNullOrEmpty(dateFormat) ? null : DateTimeFormatter.ofPattern(dateFormat).withZone(zoneId);

            JodaDateTimeFormatterHolder tmpJodaDTFH = null;

            try {
                if (Class.forName("org.joda.time.DateTime") != null) {
                    tmpJodaDTFH = new JodaDateTimeFormatterHolder(dateFormat, timeZone);
                }
            } catch (Throwable e) {
                // ignore.
            }

            this.jodaDTFH = tmpJodaDTFH;

            this.isLongDateFormat = N.notNullOrEmpty(dateFormat) && "long".equalsIgnoreCase(dateFormat);

            if (isLongDateFormat && (java.time.LocalTime.class.isAssignableFrom(clazz) || java.time.LocalDate.class.isAssignableFrom(clazz))) {
                throw new UnsupportedOperationException("Date format can't be 'long' for type java.time.LocalTime/LocalDate");
            }

            String numberFormatStr = StringUtil.trim(getNumberFormat(field));
            this.numberFormat = N.isNullOrEmpty(numberFormatStr) ? null : new DecimalFormat(numberFormatStr);

            this.hasFormat = N.notNullOrEmpty(dateFormat) || numberFormat != null;

            String tmpColumnName = null;

            if (this.annotations.containsKey(Column.class)) {
                tmpColumnName = ((Column) this.annotations.get(Column.class)).value();
            } else {
                try {
                    if (this.annotations.containsKey(javax.persistence.Column.class)) {
                        tmpColumnName = ((javax.persistence.Column) this.annotations.get(javax.persistence.Column.class)).name();
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }

            this.columnName = tmpColumnName;
        }

        /**
         * Gets the prop value.
         *
         * @param <T>
         * @param obj
         * @return
         */
        @SuppressWarnings("unchecked")
        public <T> T getPropValue(Object obj) {
            try {
                return (T) (isFieldAccessible ? field.get(obj) : getMethod.invoke(obj));
            } catch (Exception e) {
                throw N.toRuntimeException(e);
            }
        }

        /**
         * Sets the prop value.
         *
         * @param obj
         * @param propValue
         */
        public void setPropValue(final Object obj, Object propValue) {
            propValue = propValue == null ? type.defaultValue() : propValue;

            try {
                if (isFieldAccessible) {
                    field.set(obj, propValue);
                } else if (setMethod != null) {
                    setMethod.invoke(obj, propValue);
                } else if (getMethod != null) {
                    ClassUtil.setPropValueByGet(obj, getMethod, propValue);
                } else {
                    field.set(obj, propValue);
                }
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(e, "Failed to set value for field: {} in class: {}", field, declaringClass);
                }

                propValue = N.convert(propValue, jsonXmlType);

                try {
                    if (isFieldAccessible) {
                        field.set(obj, propValue);
                    } else if (setMethod != null) {
                        setMethod.invoke(obj, propValue);
                    } else {
                        field.set(obj, propValue);
                    }
                } catch (Exception e2) {
                    throw N.toRuntimeException(e);
                }
            }
        }

        static final Map<Class<?>, DateTimeReaderWriter<?>> propFuncMap = new HashMap<>();

        static {
            propFuncMap.put(java.util.Date.class, new DateTimeReaderWriter<java.util.Date>() {
                @Override
                public java.util.Date read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        return new java.util.Date(N.parseLong(strValue));
                    } else {
                        return DateUtil.parseJUDate(strValue, propInfo.dateFormat, propInfo.timeZone);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.util.Date x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        writer.write(x.getTime());
                    } else {
                        DateUtil.format(writer, x, propInfo.dateFormat, propInfo.timeZone);
                    }
                }
            });

            propFuncMap.put(java.util.Calendar.class, new DateTimeReaderWriter<java.util.Calendar>() {
                @Override
                public java.util.Calendar read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        final Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(N.parseLong(strValue));
                        calendar.setTimeZone(propInfo.timeZone);
                        return calendar;
                    } else {
                        return DateUtil.parseCalendar(strValue, propInfo.dateFormat, propInfo.timeZone);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.util.Calendar x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        writer.write(x.getTimeInMillis());
                    } else {
                        DateUtil.format(writer, x, propInfo.dateFormat, propInfo.timeZone);
                    }

                }
            });

            propFuncMap.put(java.sql.Timestamp.class, new DateTimeReaderWriter<java.sql.Timestamp>() {
                @Override
                public java.sql.Timestamp read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        return new java.sql.Timestamp(N.parseLong(strValue));
                    } else {
                        return DateUtil.parseTimestamp(strValue, propInfo.dateFormat, propInfo.timeZone);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.sql.Timestamp x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        writer.write(x.getTime());
                    } else {
                        DateUtil.format(writer, x, propInfo.dateFormat, propInfo.timeZone);
                    }
                }
            });

            propFuncMap.put(java.sql.Date.class, new DateTimeReaderWriter<java.sql.Date>() {
                @Override
                public java.sql.Date read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        return new java.sql.Date(N.parseLong(strValue));
                    } else {
                        return DateUtil.parseDate(strValue, propInfo.dateFormat, propInfo.timeZone);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.sql.Date x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        writer.write(x.getTime());
                    } else {
                        DateUtil.format(writer, x, propInfo.dateFormat, propInfo.timeZone);
                    }
                }
            });

            propFuncMap.put(java.sql.Time.class, new DateTimeReaderWriter<java.sql.Time>() {
                @Override
                public java.sql.Time read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        return new java.sql.Time(N.parseLong(strValue));
                    } else {
                        return DateUtil.parseTime(strValue, propInfo.dateFormat, propInfo.timeZone);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.sql.Time x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        writer.write(x.getTime());
                    } else {
                        DateUtil.format(writer, x, propInfo.dateFormat, propInfo.timeZone);
                    }
                }
            });

            propFuncMap.put(java.time.LocalDateTime.class, new DateTimeReaderWriter<java.time.LocalDateTime>() {
                @Override
                public java.time.LocalDateTime read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        return new java.sql.Timestamp(N.parseLong(strValue)).toLocalDateTime();
                    } else {
                        return java.time.LocalDateTime.parse(strValue, propInfo.dateTimeFormatter);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.time.LocalDateTime x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        writer.write(x.atZone(propInfo.zoneId).toInstant().toEpochMilli());
                    } else {
                        propInfo.dateTimeFormatter.formatTo(x, writer);
                    }
                }
            });

            propFuncMap.put(java.time.LocalDate.class, new DateTimeReaderWriter<java.time.LocalDate>() {
                @Override
                public java.time.LocalDate read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        // return new java.sql.Date(N.parseLong(strValue)).toLocalDate();
                        throw new UnsupportedOperationException("Date format can't be 'long' for type java.time.LocalDate");
                    } else {
                        return java.time.LocalDate.parse(strValue, propInfo.dateTimeFormatter);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.time.LocalDate x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        // writer.write(x.atStartOfDay(propInfo.zoneId).toInstant().toEpochMilli());
                        throw new UnsupportedOperationException("Date format can't be 'long' for type java.time.LocalDate");
                    } else {
                        propInfo.dateTimeFormatter.formatTo(x, writer);
                    }
                }
            });

            propFuncMap.put(java.time.LocalTime.class, new DateTimeReaderWriter<java.time.LocalTime>() {
                @Override
                public java.time.LocalTime read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        // return new java.sql.Time(N.parseLong(strValue)).toLocalTime();
                        throw new UnsupportedOperationException("Date format can't be 'long' for type java.time.LocalTime");
                    } else {
                        return java.time.LocalTime.parse(strValue, propInfo.dateTimeFormatter);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.time.LocalTime x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        // writer.write(java.sql.Time.valueOf(x).getTime());
                        throw new UnsupportedOperationException("Date format can't be 'long' for type java.time.LocalTime");
                    } else {
                        propInfo.dateTimeFormatter.formatTo(x, writer);
                    }
                }
            });

            propFuncMap.put(java.time.ZonedDateTime.class, new DateTimeReaderWriter<java.time.ZonedDateTime>() {
                @Override
                public java.time.ZonedDateTime read(PropInfo propInfo, String strValue) {
                    if (propInfo.isLongDateFormat) {
                        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(N.parseLong(strValue)), propInfo.zoneId);
                    } else {
                        return ZonedDateTime.parse(strValue, propInfo.dateTimeFormatter);
                    }
                }

                @Override
                public void write(PropInfo propInfo, java.time.ZonedDateTime x, CharacterWriter writer) throws IOException {
                    if (propInfo.isLongDateFormat) {
                        writer.write(x.toInstant().toEpochMilli());
                    } else {
                        propInfo.dateTimeFormatter.formatTo(x, writer);
                    }
                }
            });

            try {
                if (Class.forName("org.joda.time.DateTime") != null) {
                    propFuncMap.put(org.joda.time.DateTime.class, new DateTimeReaderWriter<org.joda.time.DateTime>() {
                        @Override
                        public org.joda.time.DateTime read(PropInfo propInfo, String strValue) {
                            if (propInfo.isLongDateFormat) {
                                final org.joda.time.DateTime dt = new org.joda.time.DateTime(N.parseLong(strValue));
                                return dt.getZone().equals(propInfo.jodaDTFH.dtz) ? dt : dt.withZone(propInfo.jodaDTFH.dtz);
                            } else {
                                return propInfo.jodaDTFH.dtf.parseDateTime(strValue);
                            }
                        }

                        @Override
                        public void write(PropInfo propInfo, org.joda.time.DateTime x, CharacterWriter writer) throws IOException {
                            if (propInfo.isLongDateFormat) {
                                writer.write(x.getMillis());
                            } else {
                                propInfo.jodaDTFH.dtf.printTo(writer, x);
                            }
                        }
                    });

                    propFuncMap.put(org.joda.time.MutableDateTime.class, new DateTimeReaderWriter<org.joda.time.MutableDateTime>() {
                        @Override
                        public org.joda.time.MutableDateTime read(PropInfo propInfo, String strValue) {
                            if (propInfo.isLongDateFormat) {
                                final org.joda.time.MutableDateTime dt = new org.joda.time.MutableDateTime(N.parseLong(strValue));

                                if (!propInfo.jodaDTFH.dtz.equals(dt.getZone())) {
                                    dt.setZone(propInfo.jodaDTFH.dtz);
                                }

                                return dt;
                            } else {
                                return propInfo.jodaDTFH.dtf.parseMutableDateTime(strValue);
                            }
                        }

                        @Override
                        public void write(PropInfo propInfo, org.joda.time.MutableDateTime x, CharacterWriter writer) throws IOException {
                            if (propInfo.isLongDateFormat) {
                                writer.write(x.getMillis());
                            } else {
                                propInfo.jodaDTFH.dtf.printTo(writer, x);
                            }
                        }
                    });
                }
            } catch (Throwable e) {
                // ignore.
            }
        }

        /**
         * Read prop value.
         *
         * @param strValue
         * @return
         */
        public Object readPropValue(String strValue) {
            if (N.notNullOrEmpty(dateFormat)) {
                final DateTimeReaderWriter<?> func = propFuncMap.get(clazz);

                if (func == null) {
                    //    if (isLongDateFormat) {
                    //        return type.valueOf(strValue);
                    //    } else {
                    //        return type.valueOf(DateUtil.parseJUDate(strValue, this.dateFormat).getTime());
                    //    }

                    throw new UnsupportedOperationException("'DateFormat' annotation for field: " + field
                            + " is only supported for types: java.util.Date/Calendar, java.sql.Date/Time/Timestamp, java.time.LocalDateTime/LocalDate/LocalTime/ZonedDateTime, not supported for: "
                            + ClassUtil.getCanonicalClassName(clazz));
                }

                return func.read(this, strValue);
            } else {
                return jsonXmlType.valueOf(strValue);
            }
        }

        /**
         * Write prop value.
         *
         * @param writer
         * @param x
         * @param config
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void writePropValue(CharacterWriter writer, Object x, SerializationConfig<?> config) throws IOException {
            if (hasFormat) {
                if (x == null) {
                    writer.write(NULL_CHAR_ARRAY);
                } else if (dateFormat != null) {
                    boolean isQuote = (config != null) && (config.getStringQuotation() != 0);

                    if (isQuote) {
                        writer.write(config.getStringQuotation());
                    }

                    @SuppressWarnings("rawtypes")
                    final DateTimeReaderWriter func = propFuncMap.get(clazz);

                    if (func == null) {
                        //    if (isLongDateFormat) {
                        //        return type.valueOf(strValue);
                        //    } else {
                        //        return type.valueOf(DateUtil.parseJUDate(strValue, this.dateFormat).getTime());
                        //    }

                        throw new UnsupportedOperationException("'DateFormat' annotation for field: " + field
                                + " is only supported for types: java.util.Date/Calendar, java.sql.Date/Time/Timestamp, java.time.LocalDateTime/LocalDate/LocalTime/ZonedDateTime, not supported for: "
                                + ClassUtil.getCanonicalClassName(clazz));
                    }

                    func.write(this, x, writer);

                    if (isQuote) {
                        writer.write(config.getStringQuotation());
                    }
                } else {
                    writer.write(numberFormat.format(x));
                }
            } else {
                jsonXmlType.writeCharacter(writer, x, config);
            }
        }

        public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
            return annotations.containsKey(annotationClass);
        }

        public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
            return (T) annotations.get(annotationClass);
        }

        private Map<Class<? extends Annotation>, Annotation> getAnnotations() {
            final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

            if (field != null && N.notNullOrEmpty(field.getAnnotations())) {
                for (Annotation anno : field.getAnnotations()) {
                    annotations.put(anno.annotationType(), anno);
                }
            }

            if (getMethod != null && N.notNullOrEmpty(getMethod.getAnnotations())) {
                for (Annotation anno : getMethod.getAnnotations()) {
                    annotations.put(anno.annotationType(), anno);
                }
            }

            if (setMethod != null && N.notNullOrEmpty(setMethod.getAnnotations())) {
                for (Annotation anno : setMethod.getAnnotations()) {
                    annotations.put(anno.annotationType(), anno);
                }
            }

            return annotations;
        }

        /**
         * Gets the json xml anno type.
         *
         * @param field
         * @param getMethod
         * @param setMethod
         * @param propClass
         * @return
         */
        @SuppressWarnings("deprecation")
        private String getAnnoType(final Field field, final Method getMethod, final Method setMethod, final Class<?> propClass) {
            final com.landawn.abacus.annotation.Type typeAnno = getAnnotation(com.landawn.abacus.annotation.Type.class);

            if (typeAnno != null && (typeAnno.scope() == Scope.ALL || typeAnno.scope() == Scope.PARSER)) {
                if (N.notNullOrEmpty(typeAnno.value())) {
                    return typeAnno.value();
                } else if (N.notNullOrEmpty(typeAnno.name())) {
                    return typeAnno.name();
                } else if (propClass.isEnum()) {
                    return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(typeAnno.enumerated() == EnumBy.ORDINAL) + ")";
                }
            }

            final JsonXmlField jsonXmlFieldAnno = getAnnotation(JsonXmlField.class);

            if (jsonXmlFieldAnno != null) {
                if (N.notNullOrEmpty(jsonXmlFieldAnno.type())) {
                    return jsonXmlFieldAnno.type();
                } else if (propClass.isEnum()) {
                    return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(jsonXmlFieldAnno.enumerated() == EnumBy.ORDINAL) + ")";
                }
            }

            return null;
        }

        /**
         * Gets the json xml anno type.
         *
         * @param field
         * @param getMethod
         * @param setMethod
         * @param propClass
         * @return
         */
        @SuppressWarnings("deprecation")
        private String getJsonXmlAnnoType(final Field field, final Method getMethod, final Method setMethod, final Class<?> propClass) {
            final JsonXmlField jsonXmlFieldAnno = getAnnotation(JsonXmlField.class);

            if (jsonXmlFieldAnno != null) {
                if (N.notNullOrEmpty(jsonXmlFieldAnno.type())) {
                    return jsonXmlFieldAnno.type();
                } else if (propClass.isEnum()) {
                    return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(jsonXmlFieldAnno.enumerated() == EnumBy.ORDINAL) + ")";
                }
            }

            final com.landawn.abacus.annotation.Type typeAnno = getAnnotation(com.landawn.abacus.annotation.Type.class);

            if (typeAnno != null && (typeAnno.scope() == Scope.ALL || typeAnno.scope() == Scope.PARSER)) {
                if (N.notNullOrEmpty(typeAnno.value())) {
                    return typeAnno.value();
                } else if (N.notNullOrEmpty(typeAnno.name())) {
                    return typeAnno.name();
                } else if (propClass.isEnum()) {
                    return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(typeAnno.enumerated() == EnumBy.ORDINAL) + ")";
                }
            }

            return null;
        }

        /**
         * Gets the DB anno type.
         *
         * @param field
         * @param getMethod
         * @param setMethod
         * @param propClass
         * @return
         */
        @SuppressWarnings("deprecation")
        private String getDBAnnoType(final Field field, final Method getMethod, final Method setMethod, final Class<?> propClass) {
            final com.landawn.abacus.annotation.Type typeAnno = getAnnotation(com.landawn.abacus.annotation.Type.class);

            if (typeAnno != null && (typeAnno.scope() == Scope.ALL || typeAnno.scope() == Scope.DB)) {
                if (N.notNullOrEmpty(typeAnno.value())) {
                    return typeAnno.value();
                } else if (N.notNullOrEmpty(typeAnno.name())) {
                    return typeAnno.name();
                } else if (propClass.isEnum()) {
                    return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(typeAnno.enumerated() == EnumBy.ORDINAL) + ")";
                }
            }

            return null;
        }

        /**
         * Gets the type.
         *
         * @param <T>
         * @param annoType
         * @param field
         * @param getMethod
         * @param setMethod
         * @param propClass
         * @param entityClass
         * @return
         */
        private <T> Type<T> getType(final String annoType, final Field field, final Method getMethod, final Method setMethod, final Class<?> propClass,
                final Class<?> entityClass) {
            if (N.isNullOrEmpty(annoType)) {
                final Class<?>[] typeArgs = field == null ? ClassUtil.getTypeArgumentsByMethod((setMethod == null) ? getMethod : setMethod)
                        : ClassUtil.getTypeArgumentsByField(field);

                if (typeArgs.length == 0) {
                    return N.typeOf(propClass);
                } else if (typeArgs.length == 1 && typeArgs[0].equals(HBaseColumn.class)) {
                    Method addMethod = ClassUtil.getDeclaredMethod(entityClass, "add" + setMethod.getName().substring(3), HBaseColumn.class);

                    String typeName = ClassUtil.getCanonicalClassName(propClass) + WD.LESS_THAN + ClassUtil.getSimpleClassName(HBaseColumn.class) + WD.LESS_THAN
                            + ClassUtil.getCanonicalClassName(ClassUtil.getTypeArgumentsByMethod(addMethod)[0]) + WD.GREATER_THAN + WD.GREATER_THAN;

                    return N.typeOf(typeName);
                } else if (typeArgs.length == 2 && typeArgs[1].equals(HBaseColumn.class)) {
                    Method addMethod = ClassUtil.getDeclaredMethod(entityClass, "add" + setMethod.getName().substring(3), HBaseColumn.class);

                    String typeName = ClassUtil.getCanonicalClassName(propClass) + WD.LESS_THAN + ClassUtil.getCanonicalClassName(typeArgs[0]) + WD.COMMA_SPACE
                            + ClassUtil.getSimpleClassName(HBaseColumn.class) + WD.LESS_THAN
                            + ClassUtil.getCanonicalClassName(ClassUtil.getTypeArgumentsByMethod(addMethod)[0]) + WD.GREATER_THAN + WD.GREATER_THAN;

                    return N.typeOf(typeName);
                } else {
                    final String parameterizedTypeName = field != null ? ClassUtil.getParameterizedTypeNameByField(field)
                            : ClassUtil.getParameterizedTypeNameByMethod((setMethod == null) ? getMethod : setMethod);
                    return N.typeOf(parameterizedTypeName);
                }
            } else if (N.isNullOrEmpty(ClassUtil.getPackageName(entityClass))) {
                return N.typeOf(annoType);
            } else {
                final String pkgName = ClassUtil.getPackageName(entityClass);
                final StringBuilder sb = new StringBuilder();
                int start = 0;

                for (int i = 0, len = annoType.length(); i < len; i++) {
                    char ch = annoType.charAt(i);

                    if (ch == '<' || ch == '>' || ch == ' ' || ch == ',') {
                        String str = annoType.substring(start, i);

                        if (str.length() > 0 && N.typeOf(str).clazz().equals(Object.class) && !N.typeOf(pkgName + "." + str).clazz().equals(Object.class)) {
                            sb.append(pkgName + "." + str);
                        } else {
                            sb.append(str);
                        }

                        sb.append(ch);
                        start = i + 1;
                    }
                }

                if (start < annoType.length()) {
                    String str = annoType.substring(start);

                    if (N.typeOf(str).clazz().equals(Object.class) && !N.typeOf(pkgName + "." + str).clazz().equals(Object.class)) {
                        sb.append(pkgName + "." + str);
                    } else {
                        sb.append(str);
                    }
                }

                return N.typeOf(sb.toString());
            }
        }

        /**
         *
         * @return
         */
        @Override
        public int hashCode() {
            return ((name == null) ? 0 : name.hashCode()) * 31 + ((field == null) ? 0 : field.hashCode());
        }

        /**
         *
         * @param obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            return this == obj || ((obj instanceof PropInfo) && ((PropInfo) obj).name.equals(name)) && N.equals(((PropInfo) obj).field, field);
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * The Class ASMPropInfo.
     */
    static class ASMPropInfo extends PropInfo {

        /** The method access. */
        final com.esotericsoftware.reflectasm.MethodAccess methodAccess;

        /** The get method access index. */
        final int getMethodAccessIndex;

        /** The set method access index. */
        final int setMethodAccessIndex;

        /** The field access. */
        final com.esotericsoftware.reflectasm.FieldAccess fieldAccess;

        /** The field access index. */
        final int fieldAccessIndex;

        /**
         * Instantiates a new ASM prop info.
         *
         * @param name
         * @param xmlPropName
         * @param jsonPropName
         * @param field
         * @param getMethod
         * @param classAnnotations
         */
        public ASMPropInfo(final String name, final String xmlPropName, final String jsonPropName, final Field field, final Method getMethod,
                final ImmutableMap<Class<? extends Annotation>, Annotation> classAnnotations) {
            super(name, xmlPropName, jsonPropName, field, getMethod, classAnnotations);

            methodAccess = com.esotericsoftware.reflectasm.MethodAccess.get(declaringClass);
            getMethodAccessIndex = getMethod == null ? -1 : methodAccess.getIndex(getMethod.getName(), 0);
            setMethodAccessIndex = setMethod == null ? -1 : methodAccess.getIndex(setMethod.getName(), setMethod.getParameterTypes());
            fieldAccess = com.esotericsoftware.reflectasm.FieldAccess.get(declaringClass);
            fieldAccessIndex = (field == null || this.isFieldAccessible == false || Modifier.isPrivate(field.getModifiers())
                    || Modifier.isFinal(field.getModifiers())) ? -1 : fieldAccess.getIndex(field.getName());
        }

        /**
         * Gets the prop value.
         *
         * @param <T>
         * @param obj
         * @return
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getPropValue(Object obj) {
            return (T) ((fieldAccessIndex > -1) ? fieldAccess.get(obj, fieldAccessIndex) : methodAccess.invoke(obj, getMethodAccessIndex));
        }

        /**
         * Sets the prop value.
         *
         * @param obj
         * @param propValue
         */
        @Override
        public void setPropValue(final Object obj, Object propValue) {
            propValue = propValue == null ? type.defaultValue() : propValue;

            try {
                if (fieldAccessIndex > -1) {
                    fieldAccess.set(obj, fieldAccessIndex, propValue);
                } else if (setMethodAccessIndex > -1) {
                    methodAccess.invoke(obj, setMethodAccessIndex, propValue);
                } else if (getMethod != null) {
                    ClassUtil.setPropValueByGet(obj, getMethod, propValue);
                } else {
                    field.set(obj, propValue);
                }
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(e, "Failed to set value for field: {} in class: {}", field, declaringClass);
                }

                propValue = N.convert(propValue, jsonXmlType);

                if (fieldAccessIndex > -1) {
                    fieldAccess.set(obj, fieldAccessIndex, propValue);
                } else if (setMethodAccessIndex > -1) {
                    methodAccess.invoke(obj, setMethodAccessIndex, propValue);
                } else {
                    try {
                        field.set(obj, propValue);
                    } catch (Exception e2) {
                        N.toRuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * The Class JSONInfo.
     */
    static class JSONInfo {

        /** The name. */
        final char[] name;

        /** The name. */
        final char[] _name;

        /** The name with colon. */
        final char[] nameWithColon;

        /** The name with colon. */
        final char[] _nameWithColon;

        /** The name null. */
        final char[] nameNull;

        /** The name null. */
        final char[] _nameNull;

        /** The quoted name. */
        final char[] quotedName;

        /** The quoted name. */
        final char[] _quotedName;

        /** The quoted name with colon. */
        final char[] quotedNameWithColon;

        /** The quoted name with colon. */
        final char[] _quotedNameWithColon;

        /** The quoted name null. */
        final char[] quotedNameNull;

        /** The quoted name null. */
        final char[] _quotedNameNull;

        /**
         * Instantiates a new JSON info.
         *
         * @param name
         */
        public JSONInfo(String name) {
            final String lowerCaseName = ClassUtil.toLowerCaseWithUnderscore(name);
            this.name = name.toCharArray();
            this._name = lowerCaseName.toCharArray();
            this.nameWithColon = (name + ":").toCharArray();
            this._nameWithColon = (lowerCaseName + ":").toCharArray();
            this.nameNull = (name + ":null").toCharArray();
            this._nameNull = (lowerCaseName + ":null").toCharArray();
            this.quotedName = ("\"" + name + "\"").toCharArray();
            this._quotedName = ("\"" + lowerCaseName + "\"").toCharArray();
            this.quotedNameWithColon = ("\"" + name + "\":").toCharArray();
            this._quotedNameWithColon = ("\"" + lowerCaseName + "\":").toCharArray();
            this.quotedNameNull = ("\"" + name + "\":null").toCharArray();
            this._quotedNameNull = ("\"" + lowerCaseName + "\":null").toCharArray();
        }

        /**
         *
         * @return
         */
        @Override
        public int hashCode() {
            return (name == null) ? 0 : N.hashCode(name);
        }

        /**
         *
         * @param obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof JSONInfo && N.equals(((JSONInfo) obj).name, name));
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return N.toString(name);
        }
    }

    /**
     * The Class XMLInfo.
     */
    static class XMLInfo {

        /** The name. */
        final char[] name;

        /** The name. */
        final char[] _name;

        /** The ep start. */
        final char[] epStart;

        /** The ep start. */
        final char[] _epStart;

        /** The ep start with type. */
        final char[] epStartWithType;

        /** The ep start with type. */
        final char[] _epStartWithType;

        /** The ep end. */
        final char[] epEnd;

        /** The ep null. */
        final char[] epNull;

        /** The ep null. */
        final char[] _epNull;

        /** The ep null with type. */
        final char[] epNullWithType;

        /** The ep null with type. */
        final char[] _epNullWithType;

        /** The named start. */
        final char[] namedStart;

        /** The named start. */
        final char[] _namedStart;

        /** The named start with type. */
        final char[] namedStartWithType;

        /** The named start with type. */
        final char[] _namedStartWithType;

        /** The named end. */
        final char[] namedEnd;

        /** The named end. */
        final char[] _namedEnd;

        /** The named null. */
        final char[] namedNull;

        /** The named null. */
        final char[] _namedNull;

        /** The named null with type. */
        final char[] namedNullWithType;

        /** The named null with type. */
        final char[] _namedNullWithType;

        /**
         * Instantiates a new XML info.
         *
         * @param name
         * @param typeName
         * @param isEntity
         */
        public XMLInfo(String name, String typeName, boolean isEntity) {
            final String lowerCaseName = ClassUtil.toLowerCaseWithUnderscore(name);

            this.name = name.toCharArray();
            this._name = lowerCaseName.toCharArray();

            final String typeAttr = typeName.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

            if (isEntity) {
                this.epStart = ("<entity name=\"" + name + "\">").toCharArray();
                this._epStart = ("<entity name=\"" + lowerCaseName + "\">").toCharArray();
                this.epStartWithType = ("<entity name=\"" + name + "\" type=\"" + typeAttr + "\">").toCharArray();
                this._epStartWithType = ("<entity name=\"" + lowerCaseName + "\" type=\"" + typeAttr + "\">").toCharArray();
                this.epEnd = ("</entity>").toCharArray();
                this.epNull = ("<entity name=\"" + name + "\" isNull=\"true\" />").toCharArray();
                this._epNull = ("<entity name=\"" + lowerCaseName + "\" isNull=\"true\" />").toCharArray();
                this.epNullWithType = ("<entity name=\"" + name + "\" type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
                this._epNullWithType = ("<entity name=\"" + lowerCaseName + "\" type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
            } else {
                this.epStart = ("<property name=\"" + name + "\">").toCharArray();
                this._epStart = ("<property name=\"" + lowerCaseName + "\">").toCharArray();
                this.epStartWithType = ("<property name=\"" + name + "\" type=\"" + typeAttr + "\">").toCharArray();
                this._epStartWithType = ("<property name=\"" + lowerCaseName + "\" type=\"" + typeAttr + "\">").toCharArray();
                this.epEnd = ("</property>").toCharArray();
                this.epNull = ("<property name=\"" + name + "\" isNull=\"true\" />").toCharArray();
                this._epNull = ("<property name=\"" + lowerCaseName + "\" isNull=\"true\" />").toCharArray();
                this.epNullWithType = ("<property name=\"" + name + "\" type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
                this._epNullWithType = ("<property name=\"" + lowerCaseName + "\" type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
            }

            this.namedStart = ("<" + name + ">").toCharArray();
            this._namedStart = ("<" + lowerCaseName + ">").toCharArray();
            this.namedStartWithType = ("<" + name + " type=\"" + typeAttr + "\">").toCharArray();
            this._namedStartWithType = ("<" + lowerCaseName + " type=\"" + typeAttr + "\">").toCharArray();
            this.namedEnd = ("</" + name + ">").toCharArray();
            this._namedEnd = ("</" + lowerCaseName + ">").toCharArray();
            this.namedNull = ("<" + name + " isNull=\"true\" />").toCharArray();
            this._namedNull = ("<" + lowerCaseName + " isNull=\"true\" />").toCharArray();
            this.namedNullWithType = ("<" + name + " type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
            this._namedNullWithType = ("<" + lowerCaseName + " type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
        }

        /**
         *
         * @return
         */
        @Override
        public int hashCode() {
            return (name == null) ? 0 : N.hashCode(name);
        }

        /**
         *
         * @param obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof XMLInfo && N.equals(((XMLInfo) obj).name, name));
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return N.toString(name);
        }
    }

    static interface DateTimeReaderWriter<T> {
        T read(PropInfo propInfo, String strValue);

        void write(PropInfo propInfo, T x, CharacterWriter writer) throws IOException;
    }

    static class JodaDateTimeFormatterHolder {
        final org.joda.time.DateTimeZone dtz;
        final org.joda.time.format.DateTimeFormatter dtf;

        JodaDateTimeFormatterHolder(final String dateFormat, final TimeZone timeZone) {
            this.dtz = org.joda.time.DateTimeZone.forTimeZone(timeZone);
            this.dtf = org.joda.time.format.DateTimeFormat.forPattern(dateFormat).withZone(dtz);
        }
    }
}