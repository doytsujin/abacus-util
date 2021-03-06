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
import java.lang.reflect.Constructor;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.landawn.abacus.annotation.AccessFieldByMethod;
import com.landawn.abacus.annotation.Column;
import com.landawn.abacus.annotation.Internal;
import com.landawn.abacus.annotation.JsonXmlConfig;
import com.landawn.abacus.annotation.JsonXmlField;
import com.landawn.abacus.annotation.Table;
import com.landawn.abacus.annotation.Transient;
import com.landawn.abacus.annotation.Type.EnumBy;
import com.landawn.abacus.annotation.Type.Scope;
import com.landawn.abacus.core.DirtyMarkerUtil;
import com.landawn.abacus.logging.Logger;
import com.landawn.abacus.logging.LoggerFactory;
import com.landawn.abacus.type.ObjectType;
import com.landawn.abacus.type.Type;
import com.landawn.abacus.type.TypeFactory;
import com.landawn.abacus.util.CharacterWriter;
import com.landawn.abacus.util.ClassUtil;
import com.landawn.abacus.util.DateUtil;
import com.landawn.abacus.util.ImmutableList;
import com.landawn.abacus.util.ImmutableMap;
import com.landawn.abacus.util.Multiset;
import com.landawn.abacus.util.N;
import com.landawn.abacus.util.NamingPolicy;
import com.landawn.abacus.util.ObjectPool;
import com.landawn.abacus.util.Splitter;
import com.landawn.abacus.util.StringUtil;
import com.landawn.abacus.util.WD;
import com.landawn.abacus.util.u.Optional;

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
    private static final char PROP_NAME_SEPARATOR = '.';

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

    private static final int defaultNameIndex = NamingPolicy.LOWER_CAMEL_CASE.ordinal();

    /** The Constant entityInfoPool. */
    // ...
    private static final Map<Class<?>, EntityInfo> entityInfoPool = new ObjectPool<>(POOL_SIZE);

    static boolean isJsonXmlSerializable(final Field field, final JsonXmlConfig jsonXmlConfig) {
        if (field == null) {
            return true;
        }

        if (Modifier.isStatic(field.getModifiers())) {
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

        if (jsonXmlConfig != null && N.notNullOrEmpty(jsonXmlConfig.ignoredFields())) {
            String fieldName = field.getName();

            for (String ignoreFieldName : jsonXmlConfig.ignoredFields()) {
                if (fieldName.equals(ignoreFieldName) || fieldName.matches(ignoreFieldName)) {
                    return false;
                }
            }
        }

        return true;
    }

    static String getDateFormat(final Field field, final JsonXmlConfig jsonXmlConfig) {
        if (field != null) {
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
        }

        if (jsonXmlConfig != null && N.notNullOrEmpty(jsonXmlConfig.dateFormat())) {
            return jsonXmlConfig.dateFormat();
        }

        return null;
    }

    static String getTimeZone(final Field field, final JsonXmlConfig jsonXmlConfig) {
        if (field != null) {
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
        }

        if (jsonXmlConfig != null && N.notNullOrEmpty(jsonXmlConfig.timeZone())) {
            return jsonXmlConfig.timeZone();
        }

        return null;
    }

    static String getNumberFormat(final Field field, final JsonXmlConfig jsonXmlConfig) {
        if (field != null) {
            if (field.isAnnotationPresent(JsonXmlField.class) && N.notNullOrEmpty(field.getAnnotation(JsonXmlField.class).numberFormat())) {
                return field.getAnnotation(JsonXmlField.class).numberFormat();
            }
        }

        if (jsonXmlConfig != null && N.notNullOrEmpty(jsonXmlConfig.numberFormat())) {
            return jsonXmlConfig.numberFormat();
        }

        return null;
    }

    static EnumBy getEnumerated(final Field field, final JsonXmlConfig jsonXmlConfig) {
        if (field != null) {
            if (field.isAnnotationPresent(JsonXmlField.class) && field.getAnnotation(JsonXmlField.class).enumerated() != null) {
                return field.getAnnotation(JsonXmlField.class).enumerated();
            }
        }

        if (jsonXmlConfig != null && jsonXmlConfig.enumerated() != null) {
            return jsonXmlConfig.enumerated();
        }

        return EnumBy.NAME;
    }

    static boolean isJsonRawValue(final Field field, final JsonXmlConfig jsonXmlConfig) {
        boolean isJsonRawValue = false;

        if (field != null && field.isAnnotationPresent(JsonXmlField.class)) {
            isJsonRawValue = field.getAnnotation(JsonXmlField.class).isJsonRawValue();

            if (isJsonRawValue && !CharSequence.class.isAssignableFrom(field.getType())) {
                throw new IllegalArgumentException("'isJsonRawValue' can only be applied to CharSequence type field");
            }
        }

        return isJsonRawValue;
    }

    static JsonNameTag[] getJsonNameTags(final String name) {
        final JsonNameTag[] result = new JsonNameTag[NamingPolicy.values().length];

        for (NamingPolicy np : NamingPolicy.values()) {
            result[np.ordinal()] = new JsonNameTag(convertName(name, np));
        }

        return result;
    }

    static XmlNameTag[] getXmlNameTags(final String name, final String typeName, final boolean isEntity) {
        final XmlNameTag[] result = new XmlNameTag[NamingPolicy.values().length];

        for (NamingPolicy np : NamingPolicy.values()) {
            result[np.ordinal()] = new XmlNameTag(convertName(name, np), typeName, isEntity);
        }

        return result;
    }

    static JsonNameTag[] getJsonNameTags(final String propName, final Field field) {
        String jsonXmlFieldName = null;

        if (field != null) {
            if (field.isAnnotationPresent(JsonXmlField.class) && N.notNullOrEmpty(field.getAnnotation(JsonXmlField.class).name())) {
                jsonXmlFieldName = field.getAnnotation(JsonXmlField.class).name();
            } else {
                try {
                    if (field.isAnnotationPresent(com.alibaba.fastjson.annotation.JSONField.class)
                            && N.notNullOrEmpty(field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).name())) {
                        jsonXmlFieldName = field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).name();
                    }
                } catch (Throwable e) {
                    // ignore
                }

                if (N.isNullOrEmpty(jsonXmlFieldName)) {
                    try {
                        if (field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonProperty.class)
                                && N.notNullOrEmpty(field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class).value())) {
                            jsonXmlFieldName = field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class).value();
                        }
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            }
        }

        final JsonNameTag[] result = new JsonNameTag[NamingPolicy.values().length];

        for (NamingPolicy np : NamingPolicy.values()) {
            result[np.ordinal()] = new JsonNameTag(N.isNullOrEmpty(jsonXmlFieldName) ? convertName(propName, np) : jsonXmlFieldName);
        }

        return result;
    }

    static XmlNameTag[] getXmlNameTags(final String propName, final Field field, final String typeName, final boolean isEntity) {
        String jsonXmlFieldName = null;

        if (field != null) {
            if (field.isAnnotationPresent(JsonXmlField.class) && N.notNullOrEmpty(field.getAnnotation(JsonXmlField.class).name())) {
                jsonXmlFieldName = field.getAnnotation(JsonXmlField.class).name();
            } else {
                try {
                    if (field.isAnnotationPresent(com.alibaba.fastjson.annotation.JSONField.class)
                            && N.notNullOrEmpty(field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).name())) {
                        jsonXmlFieldName = field.getAnnotation(com.alibaba.fastjson.annotation.JSONField.class).name();
                    }
                } catch (Throwable e) {
                    // ignore
                }

                if (N.isNullOrEmpty(jsonXmlFieldName)) {
                    try {
                        if (field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonProperty.class)
                                && N.notNullOrEmpty(field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class).value())) {
                            jsonXmlFieldName = field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class).value();
                        }
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            }
        }

        final XmlNameTag[] result = new XmlNameTag[NamingPolicy.values().length];

        for (NamingPolicy np : NamingPolicy.values()) {
            result[np.ordinal()] = new XmlNameTag(N.isNullOrEmpty(jsonXmlFieldName) ? convertName(propName, np) : jsonXmlFieldName, typeName, isEntity);
        }

        return result;
    }

    private static String convertName(final String name, final NamingPolicy namingPolicy) {
        return namingPolicy == null || namingPolicy == NamingPolicy.LOWER_CAMEL_CASE ? name : namingPolicy.convert(name);
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

    static int hashCode(char[] a) {
        int result = 1;

        for (char e : a) {
            result = 31 * result + e;
        }

        return result;
    }

    static int hashCode(char[] a, int from, int to) {
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

        final NamingPolicy jsonXmlNamingPolicy;

        /** The type name. */
        final String typeName;

        /** The json info. */
        final JsonNameTag[] jsonNameTags;

        /** The xml info. */
        final XmlNameTag[] xmlNameTags;

        /** The prop infos. */
        final PropInfo[] propInfos;

        /** The jsonXmlSerializablePropInfos. */
        final PropInfo[] jsonXmlSerializablePropInfos;

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

        public final Optional<String> tableName;

        /**
         * Instantiates a new entity info.
         *
         * @param cls
         */
        @SuppressWarnings("deprecation")
        public EntityInfo(Class<?> cls) {
            name = ClassUtil.formalizePropName(cls.getSimpleName());
            this.cls = (Class<Object>) cls;
            type = N.typeOf(cls);
            typeName = type.name();

            this.annotations = ImmutableMap.of(getAnnotations(cls));

            final JsonXmlConfig jsonXmlConfig = (JsonXmlConfig) annotations.get(JsonXmlConfig.class);
            this.jsonXmlNamingPolicy = jsonXmlConfig == null || jsonXmlConfig.namingPolicy() == null ? NamingPolicy.LOWER_CAMEL_CASE
                    : jsonXmlConfig.namingPolicy();

            jsonNameTags = getJsonNameTags(name);
            xmlNameTags = getXmlNameTags(name, typeName, true);

            final List<String> propNameList = ClassUtil.getPropNameList(cls);
            final List<PropInfo> seriPropInfoList = new ArrayList<>();
            final List<PropInfo> nonTransientSeriPropInfoList = new ArrayList<>();
            final List<PropInfo> transientSeriPropInfoList = new ArrayList<>();

            propInfos = new PropInfo[propNameList.size()];
            propInfoMap = new ObjectPool<>((propNameList.size() + 1) * 2);
            propInfoQueueMap = new ObjectPool<>((propNameList.size() + 1) * 2);
            hashPropInfoMap = new ObjectPool<>((propNameList.size() + 1) * 2);

            PropInfo propInfo = null;
            int i = 0;

            final Multiset<Integer> multiSet = new Multiset<>(propNameList.size() + 16);
            int maxLength = 0;
            Field field = null;
            Method getMethod = null;

            for (String propName : propNameList) {
                field = ClassUtil.getPropField(cls, propName);
                getMethod = ClassUtil.getPropGetMethod(cls, propName);

                propInfo = ASMUtil.isASMAvailable() ? new ASMPropInfo(propName, field, getMethod, jsonXmlConfig, annotations)
                        : new PropInfo(propName, field, getMethod, jsonXmlConfig, annotations);

                propInfos[i++] = propInfo;
                propInfoMap.put(propName, propInfo);
                String jsonTagName = null;

                for (JsonNameTag nameTag : propInfo.jsonNameTags) {
                    jsonTagName = new String(nameTag.name);

                    if (!propInfoMap.containsKey(jsonTagName)) {
                        propInfoMap.put(jsonTagName, propInfo);
                    }
                }

                if (propInfo.columnName.isPresent() && !propInfoMap.containsKey(propInfo.columnName.get())) {
                    propInfoMap.put(propInfo.columnName.get(), propInfo);
                }

                if (isJsonXmlSerializable(propInfo.field, jsonXmlConfig) == false) {
                    // skip
                } else {
                    seriPropInfoList.add(propInfo);

                    if (propInfo.isTransient) {
                        transientSeriPropNameSet.add(propName);

                        transientSeriPropInfoList.add(propInfo);
                    } else {
                        nonTransientSeriPropInfoList.add(propInfo);
                    }
                }

                multiSet.add(propInfo.jsonNameTags[defaultNameIndex].name.length);
                maxLength = Math.max(propInfo.jsonNameTags[defaultNameIndex].name.length, maxLength);
            }

            jsonXmlSerializablePropInfos = seriPropInfoList.toArray(new PropInfo[seriPropInfoList.size()]);
            nonTransientSeriPropInfos = nonTransientSeriPropInfoList.toArray(new PropInfo[nonTransientSeriPropInfoList.size()]);
            transientSeriPropInfos = transientSeriPropInfoList.toArray(new PropInfo[transientSeriPropInfoList.size()]);

            propInfoArray = new PropInfo[maxLength + 1];

            for (PropInfo e : propInfos) {
                hashPropInfoMap.put(ParserUtil.hashCode(e.jsonNameTags[defaultNameIndex].name), e);

                if (multiSet.get(e.jsonNameTags[defaultNameIndex].name.length) == 1) {
                    propInfoArray[e.jsonNameTags[defaultNameIndex].name.length] = e;
                }
            }

            this.propInfoList = ImmutableList.of(propInfos);

            String tmpTableName = null;

            if (this.annotations.containsKey(Table.class)) {
                tmpTableName = ((Table) this.annotations.get(Table.class)).value();

                if (N.isNullOrEmpty(tmpTableName)) {
                    tmpTableName = ((Table) this.annotations.get(Table.class)).name();
                }
            } else {
                try {
                    if (this.annotations.containsKey(javax.persistence.Table.class)) {
                        tmpTableName = ((javax.persistence.Table) this.annotations.get(javax.persistence.Table.class)).name();
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }

            this.tableName = N.isNullOrEmpty(tmpTableName) ? Optional.<String> empty() : Optional.ofNullable(tmpTableName);
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
                        hashPropInfoMap.put(ParserUtil.hashCode(propInfo.jsonNameTags[defaultNameIndex].name), propInfo);
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
        @SuppressWarnings("rawtypes")
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
                                if (propInfo.type.isCollection()) {
                                    subPropValue = N.newInstance(propInfo.type.getElementType().clazz());
                                    final Collection c = (Collection) N.newInstance(propInfo.type.clazz());
                                    c.add(subPropValue);
                                    propInfo.setPropValue(propEntity, c);
                                } else {
                                    subPropValue = N.newInstance(propInfo.clazz);
                                    propInfo.setPropValue(propEntity, subPropValue);
                                }
                            } else if (propInfo.type.isCollection()) {
                                if (propInfo.type.isList()) {
                                    subPropValue = ((List) subPropValue).get(0);
                                } else {
                                    subPropValue = N.firstOrNullIfEmpty((Collection) subPropValue);
                                }
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

                        if (propInfo.type.isCollection()) {
                            propClass = propInfo.type.getElementType().clazz();
                        } else {
                            propClass = propInfo.clazz;
                        }
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
                propInfo = hashPropInfoMap.get(ParserUtil.hashCode(cbuf, from, to));
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

                final char[] tmp = propInfo.jsonNameTags[defaultNameIndex].name;

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

        final JsonNameTag[] jsonNameTags;

        final XmlNameTag[] xmlNameTags;

        final boolean isFieldAccessible;

        /** The is dirty mark. */
        final boolean isDirtyMark;

        /** The date format. */
        final String dateFormat;

        /** The time zone. */
        final TimeZone timeZone;

        final ZoneId zoneId;

        final DateTimeFormatter dateTimeFormatter;

        final boolean isJsonRawValue;

        final JodaDateTimeFormatterHolder jodaDTFH;

        /** The is long date format. */
        final boolean isLongDateFormat;

        /** The number format. */
        final NumberFormat numberFormat;

        /** The has format. */
        final boolean hasFormat;

        public final boolean isTransient;

        public final boolean isMarkedToColumn;

        public final Optional<String> columnName;

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
            xmlNameTags = null;
            jsonNameTags = null;
            isDirtyMark = false;
            isFieldAccessible = false;
            dateFormat = null;
            timeZone = null;
            zoneId = null;
            dateTimeFormatter = null;
            isJsonRawValue = false;
            jodaDTFH = null;
            isLongDateFormat = false;
            numberFormat = null;
            hasFormat = false;

            isTransient = false;
            isMarkedToColumn = false;
            columnName = Optional.<String> empty();
        }

        @SuppressWarnings("deprecation")
        public PropInfo(final String propName, final Field field, final Method getMethod, final JsonXmlConfig jsonXmlConfig,
                final ImmutableMap<Class<? extends Annotation>, Annotation> classAnnotations) {
            this.declaringClass = (Class<Object>) (field != null ? field.getDeclaringClass() : getMethod.getDeclaringClass());
            this.isDirtyMark = DirtyMarkerUtil.isDirtyMarker(declaringClass);
            this.field = field;
            this.name = propName;
            this.getMethod = getMethod;
            this.setMethod = ClassUtil.getPropSetMethod(declaringClass, propName);
            this.annotations = ImmutableMap.of(getAnnotations());
            this.isTransient = annotations.containsKey(Transient.class) || (field != null && Modifier.isTransient(field.getModifiers()));

            this.clazz = (Class<Object>) (field == null ? (setMethod == null ? getMethod.getReturnType() : setMethod.getParameterTypes()[0]) : field.getType());
            this.type = getType(getAnnoType(this.field, this.getMethod, this.setMethod, clazz, jsonXmlConfig), this.field, this.getMethod, this.setMethod,
                    clazz, declaringClass);
            this.jsonXmlType = getType(getJsonXmlAnnoType(this.field, this.getMethod, this.setMethod, clazz, jsonXmlConfig), this.field, this.getMethod,
                    this.setMethod, clazz, declaringClass);
            this.dbType = getType(getDBAnnoType(this.field, this.getMethod, this.setMethod, clazz), this.field, this.getMethod, this.setMethod, clazz,
                    declaringClass);

            this.jsonNameTags = getJsonNameTags(propName, field);
            this.xmlNameTags = getXmlNameTags(propName, field, jsonXmlType.name(), false);

            if (field != null && !this.annotations.containsKey(AccessFieldByMethod.class) && !classAnnotations.containsKey(AccessFieldByMethod.class)) {
                ClassUtil.setAccessibleQuietly(field, true);
            }

            isFieldAccessible = isDirtyMark == false && field != null && field.isAccessible();

            String timeZoneStr = StringUtil.trim(getTimeZone(field, jsonXmlConfig));
            String dateFormatStr = StringUtil.trim(getDateFormat(field, jsonXmlConfig));
            this.dateFormat = N.isNullOrEmpty(dateFormatStr) ? null : dateFormatStr;
            this.timeZone = N.isNullOrEmpty(timeZoneStr) ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneStr);
            this.zoneId = timeZone.toZoneId();
            this.dateTimeFormatter = N.isNullOrEmpty(dateFormat) ? null : DateTimeFormatter.ofPattern(dateFormat).withZone(zoneId);
            this.isJsonRawValue = isJsonRawValue(field, jsonXmlConfig);

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

            String numberFormatStr = StringUtil.trim(getNumberFormat(field, jsonXmlConfig));
            this.numberFormat = N.isNullOrEmpty(numberFormatStr) ? null : new DecimalFormat(numberFormatStr);

            this.hasFormat = N.notNullOrEmpty(dateFormat) || numberFormat != null;

            String tmpColumnName = null;
            boolean tmpIsMarkedToColumn = false;

            if (this.annotations.containsKey(Column.class)) {
                tmpIsMarkedToColumn = true;

                tmpColumnName = ((Column) this.annotations.get(Column.class)).value();

                if (N.isNullOrEmpty(tmpColumnName)) {
                    tmpColumnName = ((Column) this.annotations.get(Column.class)).name();
                }
            } else {
                try {
                    if (this.annotations.containsKey(javax.persistence.Column.class)) {
                        tmpIsMarkedToColumn = true;

                        tmpColumnName = ((javax.persistence.Column) this.annotations.get(javax.persistence.Column.class)).name();
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }

            this.isMarkedToColumn = tmpIsMarkedToColumn;

            this.columnName = N.isNullOrEmpty(tmpColumnName) ? Optional.<String> empty() : Optional.ofNullable(tmpColumnName);
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
            } else if (isJsonRawValue) {
                if (x == null) {
                    writer.write(NULL_CHAR_ARRAY);
                } else {
                    writer.write(((CharSequence) x).toString());
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

        private String getAnnoType(final Field field, final Method getMethod, final Method setMethod, final Class<?> propClass,
                final JsonXmlConfig jsonXmlConfig) {
            final com.landawn.abacus.annotation.Type typeAnno = getAnnotation(com.landawn.abacus.annotation.Type.class);

            if (typeAnno != null && (typeAnno.scope() == Scope.ALL || typeAnno.scope() == Scope.PARSER)) {
                final String typeName = getTypeName(typeAnno, propClass);

                if (N.notNullOrEmpty(typeName)) {
                    return typeName;
                }
            }

            final JsonXmlField jsonXmlFieldAnno = getAnnotation(JsonXmlField.class);

            if (jsonXmlFieldAnno != null) {
                if (N.notNullOrEmpty(jsonXmlFieldAnno.type())) {
                    return jsonXmlFieldAnno.type();
                } else if (propClass.isEnum()) {
                    return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(getEnumerated(field, jsonXmlConfig) == EnumBy.ORDINAL) + ")";
                }
            }

            if (jsonXmlConfig != null && propClass.isEnum()) {
                return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(getEnumerated(field, jsonXmlConfig) == EnumBy.ORDINAL) + ")";
            }

            return null;
        }

        private String getJsonXmlAnnoType(final Field field, final Method getMethod, final Method setMethod, final Class<?> propClass,
                final JsonXmlConfig jsonXmlConfig) {
            final JsonXmlField jsonXmlFieldAnno = getAnnotation(JsonXmlField.class);

            if (jsonXmlFieldAnno != null) {
                if (N.notNullOrEmpty(jsonXmlFieldAnno.type())) {
                    return jsonXmlFieldAnno.type();
                } else if (propClass.isEnum()) {
                    return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(getEnumerated(field, jsonXmlConfig) == EnumBy.ORDINAL) + ")";
                }
            }

            if (jsonXmlConfig != null && propClass.isEnum()) {
                return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(getEnumerated(field, jsonXmlConfig) == EnumBy.ORDINAL) + ")";
            }

            final com.landawn.abacus.annotation.Type typeAnno = getAnnotation(com.landawn.abacus.annotation.Type.class);

            if (typeAnno != null && (typeAnno.scope() == Scope.ALL || typeAnno.scope() == Scope.PARSER)) {
                final String typeName = getTypeName(typeAnno, propClass);

                if (N.notNullOrEmpty(typeName)) {
                    return typeName;
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
        private String getDBAnnoType(final Field field, final Method getMethod, final Method setMethod, final Class<?> propClass) {
            final com.landawn.abacus.annotation.Type typeAnno = getAnnotation(com.landawn.abacus.annotation.Type.class);

            if (typeAnno != null && (typeAnno.scope() == Scope.ALL || typeAnno.scope() == Scope.DB)) {
                final String typeName = getTypeName(typeAnno, propClass);

                if (N.notNullOrEmpty(typeName)) {
                    return typeName;
                }
            }

            return null;
        }

        private String getTypeName(final com.landawn.abacus.annotation.Type typeAnno, final Class<?> propClass) {
            @SuppressWarnings("deprecation")
            final Optional<String> typeName = N.firstNonEmpty(typeAnno.value(), typeAnno.name());

            @SuppressWarnings("rawtypes")
            final Class<? extends Type> typeClass = typeAnno.clazz();

            if (typeClass != null && !typeClass.equals(Type.class)) {
                Type<?> type = null;
                @SuppressWarnings("rawtypes")
                Constructor<? extends Type> constructor = null;

                if (typeName.isPresent()) {
                    constructor = ClassUtil.getDeclaredConstructor(typeClass, String.class);

                    if (constructor != null) {
                        ClassUtil.setAccessible(constructor, true);
                        type = ClassUtil.invokeConstructor(constructor, typeName.get());
                    } else {
                        constructor = ClassUtil.getDeclaredConstructor(typeClass);

                        if (constructor == null) {
                            throw new IllegalArgumentException("No default constructor found in type class: " + typeClass);
                        }

                        ClassUtil.setAccessible(constructor, true);
                        type = ClassUtil.invokeConstructor(constructor);
                    }
                } else {
                    constructor = ClassUtil.getDeclaredConstructor(typeClass);

                    if (constructor == null) {
                        throw new IllegalArgumentException("No default constructor found in type class: " + typeClass);
                    }

                    ClassUtil.setAccessible(constructor, true);
                    type = ClassUtil.invokeConstructor(constructor);
                }

                try {
                    TypeFactory.registerType(type);
                } catch (Exception e) {
                    // ignore.
                }

                return type.name();
            } else if (typeName.isPresent()) {
                return typeName.get();
            } else if (propClass.isEnum()) {
                return ClassUtil.getCanonicalClassName(propClass) + "(" + String.valueOf(typeAnno.enumerated() == EnumBy.ORDINAL) + ")";
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
                final String parameterizedTypeName = field != null ? ClassUtil.getParameterizedTypeNameByField(field)
                        : ClassUtil.getParameterizedTypeNameByMethod((setMethod == null) ? getMethod : setMethod);

                return N.typeOf(parameterizedTypeName);
            } else {
                Type<T> type = null;

                try {
                    type = N.typeOf(annoType);
                } catch (Exception e) {
                    // ignore
                }

                if ((type == null || type.getClass().equals(ObjectType.class)) && N.notNullOrEmpty(ClassUtil.getPackageName(entityClass))) {
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

                    type = N.typeOf(sb.toString());
                }

                return type;
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

        public ASMPropInfo(final String name, final Field field, final Method getMethod, final JsonXmlConfig jsonXmlConfig,
                final ImmutableMap<Class<? extends Annotation>, Annotation> classAnnotations) {
            super(name, field, getMethod, jsonXmlConfig, classAnnotations);

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

    static class JsonNameTag {
        final char[] name;
        final char[] nameWithColon;
        final char[] nameNull;
        final char[] quotedName;
        final char[] quotedNameWithColon;
        final char[] quotedNameNull;

        public JsonNameTag(String name) {
            this.name = name.toCharArray();
            this.nameWithColon = (name + ":").toCharArray();
            this.nameNull = (name + ":null").toCharArray();
            this.quotedName = ("\"" + name + "\"").toCharArray();
            this.quotedNameWithColon = ("\"" + name + "\":").toCharArray();
            this.quotedNameNull = ("\"" + name + "\":null").toCharArray();
        }

        @Override
        public int hashCode() {
            return (name == null) ? 0 : N.hashCode(name);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof JsonNameTag && N.equals(((JsonNameTag) obj).name, name));
        }

        @Override
        public String toString() {
            return N.toString(name);
        }
    }

    static class XmlNameTag {
        final char[] name;
        final char[] epStart;
        final char[] epStartWithType;
        final char[] epEnd;
        final char[] epNull;
        final char[] epNullWithType;
        final char[] namedStart;
        final char[] namedStartWithType;
        final char[] namedEnd;
        final char[] namedNull;
        final char[] namedNullWithType;

        public XmlNameTag(String name, String typeName, boolean isEntity) {
            this.name = name.toCharArray();

            final String typeAttr = typeName.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

            if (isEntity) {
                this.epStart = ("<entity name=\"" + name + "\">").toCharArray();
                this.epStartWithType = ("<entity name=\"" + name + "\" type=\"" + typeAttr + "\">").toCharArray();
                this.epEnd = ("</entity>").toCharArray();
                this.epNull = ("<entity name=\"" + name + "\" isNull=\"true\" />").toCharArray();
                this.epNullWithType = ("<entity name=\"" + name + "\" type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
            } else {
                this.epStart = ("<property name=\"" + name + "\">").toCharArray();
                this.epStartWithType = ("<property name=\"" + name + "\" type=\"" + typeAttr + "\">").toCharArray();
                this.epEnd = ("</property>").toCharArray();
                this.epNull = ("<property name=\"" + name + "\" isNull=\"true\" />").toCharArray();
                this.epNullWithType = ("<property name=\"" + name + "\" type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
            }

            this.namedStart = ("<" + name + ">").toCharArray();
            this.namedStartWithType = ("<" + name + " type=\"" + typeAttr + "\">").toCharArray();
            this.namedEnd = ("</" + name + ">").toCharArray();
            this.namedNull = ("<" + name + " isNull=\"true\" />").toCharArray();
            this.namedNullWithType = ("<" + name + " type=\"" + typeAttr + "\" isNull=\"true\" />").toCharArray();
        }

        @Override
        public int hashCode() {
            return (name == null) ? 0 : N.hashCode(name);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof XmlNameTag && N.equals(((XmlNameTag) obj).name, name));
        }

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
