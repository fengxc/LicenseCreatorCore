package util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Jackson JSON工具类
 *
 * @author 2020/10/29
 */
public class JacksonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_FORMAT = "yyyy-MM-dd";
    public final static String TIME_FORMAT = "HH:mm:ss";
    /**
     * 默认日期格式化对象
     */
    private static ThreadLocal<SimpleDateFormat> formatLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat(DATE_TIME_FORMAT));

    static {
        //设置地点为中国
        objectMapper.setLocale(Locale.CHINA);
        //在反序列化时设置时间为东八区
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        //在反序列化时忽略在 json 中存在但 Java 对象不存在的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //在序列化时日期格式默认为 yyyy-MM-dd'T'HH:mm:ss.SSS
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //在序列化时自定义默认时间日期格式（如果pojo使用了@JsonFormat注解指定日期格式，则会覆盖默认设置）
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_TIME_FORMAT));
        //在序列化时忽略值为 null 的属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //POJO无public的属性或方法时，不报错
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        //设置按照字段名进行序列化
        objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        //针对于JDK新时间类。序列化时带有T的问题，自定义格式化字符串
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern((TIME_FORMAT))));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
        objectMapper.registerModule(javaTimeModule);
    }

    /**
     * 获取objectMapper对象
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    //============================================== 序列化/反序列化 方法封装 =============================================================
    public static boolean isJSONString(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        if (!str.startsWith("{") || !str.endsWith("}")) {
            return false;
        }
        try {
            objectMapper.readTree(str);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }

    }

    /**
     * 普通JSON序列化
     *
     * @param obj 目标对象
     * @return String
     */
    public static String toJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 格式化/美化/优雅 JSON序列化
     *
     * @param obj 目标对象
     * @return String
     */
    public static String toPrettyJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * JSON字符串反序列化为Java对象
     *
     * @param jsonString JSON字符串
     * @param classType  对象类型
     * @return T
     */
    public static <T> T toJavaObject(String jsonString, Class<T> classType) {
        if (isEmpty(jsonString)) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, classType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Object类型的对象转为指定的Java对象
     *
     * @param object    当前对象
     * @param classType 目标对象类型
     * @return T
     */
    public static <T> T toJavaObject(Object object, Class<T> classType) {
        if (object == null || isEmpty(object.toString())) {
            return null;
        }
        if (classType == null) {
            throw new IllegalArgumentException("classType is null!");
        }
        try {
            if (object instanceof String) {
                return objectMapper.readValue((String) object, classType);
            }
            return objectMapper.readValue(toJSONString(object), classType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * JSON字符串反序列化为Map集合
     *
     * @param jsonString JSON字符串
     * @param keyType    key的泛型类型
     * @param valueType  value的泛型类型
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> toMap(String jsonString, Class<K> keyType, Class<V> valueType) {
        if (isEmpty(jsonString)) {
            return null;
        }
        if (keyType == null || valueType == null) {
            throw new IllegalArgumentException("keyType or valueType is null!");
        }
        try {
            //第二参数是 map 的 key 的类型，第三参数是 map 的 value 的类型
            MapType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, keyType, valueType);
            return objectMapper.readValue(jsonString, javaType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * JSON字符串反序列化为Map集合
     *
     * @param jsonString JSON字符串
     * @return Map<String, Object>
     */
    public static Map<String, Object> toMap(String jsonString) {
        return toMap(jsonString, String.class, Object.class);
    }

    /**
     * Object类型的对象转为Map集合
     *
     * @param object    当前对象
     * @param keyType   map的Key类型
     * @param valueType map的Value类型
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> toMap(Object object, Class<K> keyType, Class<V> valueType) {
        if (object == null || isEmpty(object.toString())) {
            return null;
        }
        if (keyType == null || valueType == null) {
            throw new IllegalArgumentException("keyType or valueType is null!");
        }
        try {
            //第二参数是 map 的 key 的类型，第三参数是 map 的 value 的类型
            MapType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, keyType, valueType);
            if (object instanceof String) {
                return objectMapper.readValue((String) object, javaType);
            }
            return objectMapper.readValue(toJSONString(object), javaType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Map<String, Object> toMap(Object object) {
        return toMap(object, String.class, Object.class);
    }

    /**
     * JSON字符串反序列化为List集合
     *
     * @param jsonString JSON字符串
     * @param classType  List的泛型
     * @return List<T>
     */
    public static <T> List<T> toList(String jsonString, Class<T> classType) {
        if (isEmpty(jsonString)) {
            return null;
        }
        if (classType == null) {
            throw new IllegalArgumentException("classType is null!");
        }
        try {
            CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, classType);
            return objectMapper.readValue(jsonString, javaType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<Object> toList(String jsonString) {
        return toList(jsonString, Object.class);
    }

    /**
     * Object类型的对象转为List集合
     *
     * @param object    当前对象
     * @param classType List的泛型
     * @return List<T>
     */
    public static <T> List<T> toList(Object object, Class<T> classType) {
        if (object == null || isEmpty(object.toString())) {
            return null;
        }
        if (classType == null) {
            throw new IllegalArgumentException("classType is null!");
        }
        try {
            CollectionType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, classType);
            if (object instanceof String) {
                return objectMapper.readValue((String) object, javaType);
            }
            return objectMapper.readValue(toJSONString(object), javaType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<Object> toList(Object object) {
        return toList(object, Object.class);
    }

    /**
     * JSON数组字符串反序列化为泛型为<Map<K,V>的List集合
     *
     * @param jsonArrayString JSON数组字符串
     * @param keyType         Map的Key类型
     * @param valueType       Map的Value类型
     * @return List<Map < K, V>>
     */
    public static <K, V> List<Map<K, V>> toMapList(String jsonArrayString, Class<K> keyType, Class<V> valueType) {
        if (isEmpty(jsonArrayString)) {
            return null;
        }
        if (keyType == null || valueType == null) {
            throw new IllegalArgumentException("keyType or valueType is null!");
        }
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(Map.class, keyType, valueType);
            CollectionType collectionType = typeFactory.constructCollectionType(List.class, mapType);
            return objectMapper.readValue(jsonArrayString, collectionType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<Map<String, Object>> toMapList(String jsonArrayString) {
        return toMapList(jsonArrayString, String.class, Object.class);
    }

    /**
     * Object类型的对象转为泛型为<Map<K,V>的List集合
     *
     * @param object    当前对象
     * @param keyType   Map的Key类型
     * @param valueType Map的Value类型
     * @return List<Map < K, V>>
     */
    public static <K, V> List<Map<K, V>> toMapList(Object object, Class<K> keyType, Class<V> valueType) {
        if (object == null || isEmpty(object.toString())) {
            return null;
        }
        if (keyType == null || valueType == null) {
            throw new IllegalArgumentException("keyType or valueType is null!");
        }
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(Map.class, keyType, valueType);
            CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, mapType);
            if (object instanceof String) {
                return objectMapper.readValue((String) object, collectionType);
            }
            return objectMapper.readValue(toJSONString(object), collectionType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<Map<String, Object>> toMapList(Object object) {
        return toMapList(object, String.class, Object.class);
    }

    /**
     * JSON字符串转换为JSON
     *
     * @param jsonString JSON对象字符串
     * @return JsonNode
     */
    public static JsonNode toJSON(String jsonString) {
        if (isEmpty(jsonString)) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * JSON字符串转换为JSON对象
     *
     * @param jsonString JSON对象字符串
     * @return ObjectNode
     */
    public static ObjectNode parseJSONObject(String jsonString) {
        if (isEmpty(jsonString)) {
            return null;
        }
        try {
            return (ObjectNode) objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Object类型转换为JSON对象
     *
     * @param object 当前对象
     * @return ObjectNode
     */
    public static ObjectNode parseJSONObject(Object object) {
        if (object == null || isEmpty(object.toString())) {
            return null;
        }
        try {
            if (ObjectNode.class.isAssignableFrom(object.getClass())) {
                return (ObjectNode) object;
            }
            if (object instanceof String) {
                return (ObjectNode) objectMapper.readTree((String) object);
            }
            return (ObjectNode) objectMapper.readTree(toJSONString(object));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * JSON字符串转换为JSON数组
     *
     * @param jsonString JSON数组字符串
     * @return ArrayNode
     */
    public static ArrayNode parseJSONArray(String jsonString) {
        if (isEmpty(jsonString)) {
            return newJSONArray();
        }
        try {
            return (ArrayNode) objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Object类型转换为JSON数组
     *
     * @param object 当前对象
     * @return ArrayNode
     */
    public static ArrayNode parseJSONArray(Object object) {
        if (object == null || isEmpty(object.toString())) {
            return null;
        }
        try {
            if (ArrayNode.class.isAssignableFrom(object.getClass())) {
                return (ArrayNode) object;
            }
            if (object instanceof String) {
                return (ArrayNode) objectMapper.readTree((String) object);
            }
            return (ArrayNode) objectMapper.readTree(toJSONString(object));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //============================================== 获取value值方法封装 =============================================================

    /**
     * 获取JSON对象 ObjectNode
     *
     * @param jsonNode json对象
     * @param key      JSON key
     * @return ObjectNode
     */
    public static ObjectNode getJSONObject(JsonNode jsonNode, String key) {
        if (jsonNode == null) {
            return null;
        }
        return (ObjectNode) Optional.ofNullable(jsonNode.get(key)).orElse(null);
    }

    /**
     * 获取JSON数组 ArrayNode
     *
     * @param jsonNode json对象
     * @param key      JSON key
     * @return ArrayNode
     */
    public static ArrayNode getJSONArray(JsonNode jsonNode, String key) {
        if (jsonNode == null) {
            return null;
        }
        return (ArrayNode) Optional.ofNullable(jsonNode.get(key)).orElse(null);
    }

    /**
     * 获取String类型的value
     *
     * @param jsonNode     json对象
     * @param key          JSON key
     * @param defaultValue 默认value
     * @return String
     */
    public static String getString(JsonNode jsonNode, String key, String defaultValue) {
        if (jsonNode == null) {
            return null;
        }
        return Optional.ofNullable(jsonNode.get(key)).filter(x -> !x.isNull()).map(JacksonUtils::getJsonNodeString).orElse(defaultValue);
    }

    public static String getString(JsonNode jsonNode, String key) {
        return getString(jsonNode, key, null);
    }

    /**
     * 获取Byte类型的value
     *
     * @param jsonNode     json对象
     * @param key          JSON key
     * @param defaultValue 默认value
     * @return Byte
     */
    public static Byte getByte(JsonNode jsonNode, String key, Byte defaultValue) {
        if (jsonNode == null) {
            return null;
        }
        return Optional.ofNullable(jsonNode.get(key)).filter(x -> !x.isNull()).map(x -> Byte.valueOf(getJsonNodeString(x))).orElse(defaultValue);
    }

    public static Byte getByte(JsonNode jsonNode, String key) {
        return getByte(jsonNode, key, null);
    }

    /**
     * 获取Short类型的value
     *
     * @param jsonNode     json对象
     * @param key          JSON key
     * @param defaultValue 默认value
     * @return Short
     */
    public static Short getShort(JsonNode jsonNode, String key, Short defaultValue) {
        if (jsonNode == null) {
            return null;
        }
        return Optional.ofNullable(jsonNode.get(key)).filter(x -> !x.isNull()).filter(x -> !x.isNull()).map(x -> Short.valueOf(getJsonNodeString(x))).orElse(defaultValue);
    }

    public static Short getShort(JsonNode jsonNode, String key) {
        return getShort(jsonNode, key, null);
    }

    /**
     * 获取Integer类型的value
     *
     * @param jsonNode     json对象
     * @param key          JSON key
     * @param defaultValue 默认value
     * @return Integer
     */
    public static Integer getInteger(JsonNode jsonNode, String key, Integer defaultValue) {
        if (jsonNode == null) {
            return null;
        }
        return Optional.ofNullable(jsonNode.get(key)).filter(x -> !x.isNull()).map(x -> Integer.valueOf(getJsonNodeString(x))).orElse(defaultValue);
    }

    public static Integer getInteger(JsonNode jsonNode, String key) {
        return getInteger(jsonNode, key, null);
    }

    /**
     * 获取Long类型的value
     *
     * @param jsonNode     json对象
     * @param key          JSON key
     * @param defaultValue 默认value
     * @return Long
     */
    public static Long getLong(JsonNode jsonNode, String key, Long defaultValue) {
        if (jsonNode == null) {
            return null;
        }
        return Optional.ofNullable(jsonNode.get(key)).filter(x -> !x.isNull()).map(x -> Long.valueOf(getJsonNodeString(x))).orElse(defaultValue);
    }

    public static Long getLong(JsonNode jsonNode, String key) {
        return getLong(jsonNode, key, null);
    }

    /**
     * 获取Double类型的value
     *
     * @param jsonNode     json对象
     * @param key          JSON key
     * @param defaultValue 默认value
     * @return Double
     */
    public static Double getDouble(JsonNode jsonNode, String key, Double defaultValue) {
        if (jsonNode == null) {
            return null;
        }
        return Optional.ofNullable(jsonNode.get(key)).filter(x -> !x.isNull()).map(x -> Double.valueOf(getJsonNodeString(x))).orElse(defaultValue);
    }

    public static Double getDouble(JsonNode jsonNode, String key) {
        return getDouble(jsonNode, key, null);
    }

    /**
     * 获取Boolean类型的value
     *
     * @param jsonNode     json对象
     * @param key          JSON key
     * @param defaultValue 默认value
     * @return boolean
     */
    public static boolean getBoolean(JsonNode jsonNode, String key, boolean defaultValue) {
        if (jsonNode == null) {
            return false;
        }
        return Optional.ofNullable(jsonNode.get(key)).filter(x -> !x.isNull()).map(x -> Boolean.valueOf(getJsonNodeString(x))).orElse(defaultValue);
    }

    public static boolean getBoolean(JsonNode jsonNode, String key) {
        return getBoolean(jsonNode, key, false);
    }

    /**
     * 获取Date类型的value
     *
     * @param jsonNode json对象
     * @param key      JSON key
     * @param format   日期格式对象
     * @return Date
     */
    public static Date getDate(JsonNode jsonNode, String key, SimpleDateFormat format) {
        if (jsonNode == null || format == null) {
            return null;
        }
        String dateStr = Optional.ofNullable(jsonNode.get(key)).filter(x -> !x.isNull()).map(JacksonUtils::getJsonNodeString).orElse(null);
        if (dateStr == null) {
            return null;
        }
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Date getDate(JsonNode jsonNode, String key) {
        return getDate(jsonNode, key, formatLocal.get());
    }

    //============================================== 创建JSON对象/JSON数组 方法封装 =============================================================

    /**
     * 创建一个JSON对象
     *
     * @return ObjectNode
     */
    public static ObjectNode newJSONObject() {
        return objectMapper.createObjectNode();
    }

    /**
     * 创建一个JSON数组
     *
     * @return ArrayNode
     */
    public static ArrayNode newJSONArray() {
        return objectMapper.createArrayNode();
    }

    /**
     * 判断JSON属性个数是否为0
     *
     * @param jsonNode JsonNode
     * @return boolean
     */
    public static boolean isEmpty(JsonNode jsonNode) {
        return jsonNode == null || jsonNode.size() == 0;
    }

    public static boolean isNotEmpty(JsonNode jsonNode) {
        return !isEmpty(jsonNode);
    }

    /**
     * 判断字符串是否为null或者空字符串
     *
     * @param jsonString 字符串
     * @return boolean
     */
    private static boolean isEmpty(String jsonString) {
        return jsonString == null || jsonString.trim().length() <= 0;
    }

    /**
     * 去掉JsonNode toString后字符串的两端的双引号（直接用JsonNode的toString方法获取到的值做后续处理，Jackson去获取String类型的方法有坑）
     *
     * @param jsonNode JsonNode
     * @return String
     */
    private static String getJsonNodeString(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        //去掉字符串左右的引号
        String quotesLeft = "^[\"]";
        String quotesRight = "[\"]$";
        return jsonNode.toString().replaceAll(quotesLeft, "").replaceAll(quotesRight, "");
    }
}
