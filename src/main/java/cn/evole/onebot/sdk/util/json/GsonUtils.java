package cn.evole.onebot.sdk.util.json;

import cn.evole.onebot.sdk.util.StringUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/1 16:36
 * Version: 1.0
 */
public class GsonUtils {
    //线程安全的
    private static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization() //当Map的key为复杂对象时,需要开启该方法
//                .serializeNulls() //当字段值为空或null时，依然对该字段进行转换
//                .excludeFieldsWithoutExposeAnnotation()//打开Export注解，但打开了这个注解,副作用，要转换和不转换都要加注解
            .setDateFormat("yyyy-MM-dd HH:mm:ss")//序列化日期格式  "yyyy-MM-dd"
            .setPrettyPrinting() //自动格式化换行
            .disableHtmlEscaping() //防止特殊字符出现乱码
            .create();
    private static final Gson GSON_NULL = new GsonBuilder().enableComplexMapKeySerialization() //当Map的key为复杂对象时,需要开启该方法
            .serializeNulls() //当字段值为空或null时，依然对该字段进行转换
//                .excludeFieldsWithoutExposeAnnotation()//打开Export注解，但打开了这个注解,副作用，要转换和不转换都要加注解
            .setDateFormat("yyyy-MM-dd HH:mm:ss")//序列化日期格式  "yyyy-MM-dd"
            .setPrettyPrinting() //自动格式化换行
            .disableHtmlEscaping() //防止特殊字符出现乱码
            .create(); // 不过滤空值

    //获取gson解析器
    public static Gson getGson() {
        return GSON;
    }

    //获取gson解析器 有空值 解析
    public static Gson getNullGson() {
        return GSON_NULL;
    }


    /**
     * 根据对象返回json  过滤空值字段
     */
    public static String toJsonStringIgnoreNull(Object object) {
        return GSON.toJson(object);
    }

    /**
     * 根据对象返回json  不过滤空值字段
     */
    public static String toJsonString(Object object) {
        return GSON_NULL.toJson(object);
    }


    /**
     * 将字符串转化对象
     *
     * @param json     源字符串
     * @param classOfT 目标对象类
     * @param <T> T
     * @return T
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    /**
     * 将json转化为对应的实体对象
     * new TypeToken<List<T>>() {}.getType()
     * new TypeToken<Map<String, T>>() {}.getType()
     * new TypeToken<List<Map<String, T>>>() {}.getType()
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    /**
     * 将json转化为对应的实体对象
     * new TypeToken<List<T>>() {}.getType()
     * new TypeToken<Map<String, T>>() {}.getType()
     * new TypeToken<List<Map<String, T>>>() {}.getType()
     */
    public static <T> T fromJson(JsonElement json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

    /**
     * 转成list
     * @param gsonString  string类型json
     * @param cls  class
     * @return 对象 T
     */
    public static <T> List<T> convertToList(String gsonString, Class<T> cls) {
        return GSON.fromJson(gsonString, new TypeToken<List<T>>() {
        }.getType());
    }

    /**
     * 转成list中有map的
     * @param gsonString  string类型json
     * @return List<Map<String, T></>>
     */
    public static <T> List<Map<String, T>> convertToListMaps(String gsonString) {
        return GSON.fromJson(gsonString, new TypeToken<List<Map<String, String>>>() {
        }.getType());
    }

    /**
     * 转成map
     * @param gsonString  string类型json
     * @return  Map<String, T></>
     */
    public static <T> Map<String, T> convertToMaps(String gsonString) {
        return GSON.fromJson(gsonString, new TypeToken<Map<String, T>>() {
        }.getType());
    }


    /**
     * Does the given JsonObject contain a string field with the given name?
     */
    public static boolean isStringValue(JsonObject pJson, String pMemberName) {
        return isValidPrimitive(pJson, pMemberName) && pJson.getAsJsonPrimitive(pMemberName).isString();
    }

    /**
     * Is the given JsonElement a string?
     */
    public static boolean isStringValue(JsonElement pJson) {
        return pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isString();
    }

    public static boolean isNumberValue(JsonObject pJson, String pMemberName) {
        return isValidPrimitive(pJson, pMemberName) && pJson.getAsJsonPrimitive(pMemberName).isNumber();
    }

    public static boolean isNumberValue(JsonElement pJson) {
        return pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber();
    }

    public static boolean isBooleanValue(JsonObject pJson, String pMemberName) {
        return isValidPrimitive(pJson, pMemberName) && pJson.getAsJsonPrimitive(pMemberName).isBoolean();
    }

    public static boolean isBooleanValue(JsonElement pJson) {
        return pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isBoolean();
    }

    /**
     * Does the given JsonObject contain an array field with the given name?
     */
    public static boolean isArrayNode(JsonObject pJson, String pMemberName) {
        return isValidNode(pJson, pMemberName) && pJson.get(pMemberName).isJsonArray();
    }

    public static boolean isObjectNode(JsonObject pJson, String pMemberName) {
        return isValidNode(pJson, pMemberName) && pJson.get(pMemberName).isJsonObject();
    }

    /**
     * Does the given JsonObject contain a field with the given name whose type is primitive (String, Java primitive, or Java primitive wrapper)?
     */
    public static boolean isValidPrimitive(JsonObject pJson, String pMemberName) {
        return isValidNode(pJson, pMemberName) && pJson.get(pMemberName).isJsonPrimitive();
    }

    /**
     * Does the given JsonObject contain a field with the given name?
     */
    public static boolean isValidNode(@Nullable JsonObject pJson, String pMemberName) {
        if (pJson == null) {
            return false;
        } else {
            return pJson.get(pMemberName) != null;
        }
    }

    public static JsonElement getNonNull(JsonObject pJson, String pMemberName) {
        JsonElement jsonelement = pJson.get(pMemberName);
        if (jsonelement != null && !jsonelement.isJsonNull()) {
            return jsonelement;
        } else {
            throw new JsonSyntaxException("Missing field " + pMemberName);
        }
    }

    /**
     * Gets the string value of the given JsonElement.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static String convertToString(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive()) {
            return pJson.getAsString();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a string, was " + getType(pJson));
        }
    }

    /**
     * Gets the string value of the field on the JsonObject with the given name.
     */
    public static String getAsString(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToString(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a string");
        }
    }

    /**
     * Gets the string value of the field on the JsonObject with the given name, or the given default value if the field is missing.
     */
    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static String getAsString(JsonObject pJson, String pMemberName, @Nullable String pFallback) {
        return pJson.has(pMemberName) ? convertToString(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    /**
     * Gets the boolean value of the given JsonElement.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static boolean convertToBoolean(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive()) {
            return pJson.getAsBoolean();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Boolean, was " + getType(pJson));
        }
    }

    /**
     * Gets the boolean value of the field on the JsonObject with the given name.
     */
    public static boolean getAsBoolean(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToBoolean(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Boolean");
        }
    }

    /**
     * Gets the boolean value of the field on the JsonObject with the given name, or the given default value if the field is missing.
     */
    public static boolean getAsBoolean(JsonObject pJson, String pMemberName, boolean pFallback) {
        return pJson.has(pMemberName) ? convertToBoolean(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static double convertToDouble(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsDouble();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Double, was " + getType(pJson));
        }
    }

    public static double getAsDouble(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToDouble(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Double");
        }
    }

    public static double getAsDouble(JsonObject pJson, String pMemberName, double pFallback) {
        return pJson.has(pMemberName) ? convertToDouble(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    /**
     * Gets the float value of the given JsonElement.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static float convertToFloat(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsFloat();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Float, was " + getType(pJson));
        }
    }

    /**
     * Gets the float value of the field on the JsonObject with the given name.
     */
    public static float getAsFloat(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToFloat(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Float");
        }
    }

    /**
     * Gets the float value of the field on the JsonObject with the given name, or the given default value if the field is missing.
     */
    public static float getAsFloat(JsonObject pJson, String pMemberName, float pFallback) {
        return pJson.has(pMemberName) ? convertToFloat(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    /**
     * Gets a long from a JSON element and validates that the value is actually a number.
     */
    public static long convertToLong(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsLong();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Long, was " + getType(pJson));
        }
    }

    /**
     * Gets a long from a JSON element, throws an error if the member does not exist.
     */
    public static long getAsLong(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToLong(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Long");
        }
    }

    public static long getAsLong(JsonObject pJson, String pMemberName, long pFallback) {
        return pJson.has(pMemberName) ? convertToLong(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    /**
     * Gets the integer value of the given JsonElement.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static int convertToInt(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsInt();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Int, was " + getType(pJson));
        }
    }

    /**
     * Gets the integer value of the field on the JsonObject with the given name.
     */
    public static int getAsInt(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToInt(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Int");
        }
    }

    /**
     * Gets the integer value of the field on the JsonObject with the given name, or the given default value if the field is missing.
     */
    public static int getAsInt(JsonObject pJson, String pMemberName, int pFallback) {
        return pJson.has(pMemberName) ? convertToInt(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static byte convertToByte(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsByte();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Byte, was " + getType(pJson));
        }
    }

    public static byte getAsByte(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToByte(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Byte");
        }
    }

    public static byte getAsByte(JsonObject pJson, String pMemberName, byte pFallback) {
        return pJson.has(pMemberName) ? convertToByte(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static char convertToCharacter(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsCharacter();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Character, was " + getType(pJson));
        }
    }

    public static char getAsCharacter(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToCharacter(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Character");
        }
    }

    public static char getAsCharacter(JsonObject pJson, String pMemberName, char pFallback) {
        return pJson.has(pMemberName) ? convertToCharacter(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static BigDecimal convertToBigDecimal(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsBigDecimal();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a BigDecimal, was " + getType(pJson));
        }
    }

    public static BigDecimal getAsBigDecimal(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToBigDecimal(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a BigDecimal");
        }
    }

    public static BigDecimal getAsBigDecimal(JsonObject pJson, String pMemberName, BigDecimal pFallback) {
        return pJson.has(pMemberName) ? convertToBigDecimal(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static BigInteger convertToBigInteger(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsBigInteger();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a BigInteger, was " + getType(pJson));
        }
    }

    public static BigInteger getAsBigInteger(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToBigInteger(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a BigInteger");
        }
    }

    public static BigInteger getAsBigInteger(JsonObject pJson, String pMemberName, BigInteger pFallback) {
        return pJson.has(pMemberName) ? convertToBigInteger(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static short convertToShort(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
            return pJson.getAsShort();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a Short, was " + getType(pJson));
        }
    }

    public static short getAsShort(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToShort(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Short");
        }
    }

    public static short getAsShort(JsonObject pJson, String pMemberName, short pFallback) {
        return pJson.has(pMemberName) ? convertToShort(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    /**
     * Gets the given JsonElement as a JsonObject.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static JsonObject convertToJsonObject(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonObject()) {
            return pJson.getAsJsonObject();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a JsonObject, was " + getType(pJson));
        }
    }

    public static JsonObject getAsJsonObject(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToJsonObject(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a JsonObject");
        }
    }

    /**
     * Gets the JsonObject field on the JsonObject with the given name, or the given default value if the field is missing.
     */
    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static JsonObject getAsJsonObject(JsonObject pJson, String pMemberName, @Nullable JsonObject pFallback) {
        return pJson.has(pMemberName) ? convertToJsonObject(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    /**
     * Gets the given JsonElement as a JsonArray.  Expects the second parameter to be the name of the element's field if an error message needs to be thrown.
     */
    public static JsonArray convertToJsonArray(JsonElement pJson, String pMemberName) {
        if (pJson.isJsonArray()) {
            return pJson.getAsJsonArray();
        } else {
            throw new JsonSyntaxException("Expected " + pMemberName + " to be a JsonArray, was " + getType(pJson));
        }
    }

    /**
     * Gets the JsonArray field on the JsonObject with the given name.
     */
    public static JsonArray getAsJsonArray(JsonObject pJson, String pMemberName) {
        if (pJson.has(pMemberName)) {
            return convertToJsonArray(pJson.get(pMemberName), pMemberName);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a JsonArray");
        }
    }

    /**
     * Gets the JsonArray field on the JsonObject with the given name, or the given default value if the field is missing.
     */
    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static JsonArray getAsJsonArray(JsonObject pJson, String pMemberName, @Nullable JsonArray pFallback) {
        return pJson.has(pMemberName) ? convertToJsonArray(pJson.get(pMemberName), pMemberName) : pFallback;
    }

    public static <T> T convertToObject(@Nullable JsonElement pJson, String pMemberName, JsonDeserializationContext pContext, Class<? extends T> pAdapter) {
        if (pJson != null) {
            return pContext.deserialize(pJson, pAdapter);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName);
        }
    }

    public static <T> T getAsObject(JsonObject pJson, String pMemberName, JsonDeserializationContext pContext, Class<? extends T> pAdapter) {
        if (pJson.has(pMemberName)) {
            return convertToObject(pJson.get(pMemberName), pMemberName, pContext, pAdapter);
        } else {
            throw new JsonSyntaxException("Missing " + pMemberName);
        }
    }

    @Nullable
    @Contract("_,_,!null,_,_->!null;_,_,null,_,_->_")
    public static <T> T getAsObject(
            JsonObject pJson, String pMemberName, @Nullable T pFallback, JsonDeserializationContext pContext, Class<? extends T> pAdapter
    ) {
        return (T)(pJson.has(pMemberName) ? convertToObject(pJson.get(pMemberName), pMemberName, pContext, pAdapter) : pFallback);
    }

    /**
     * Gets a human-readable description of the given JsonElement's type.  For example: "a number (4)"
     */
    public static String getType(@Nullable JsonElement pJson) {
        String s = StringUtils.abbreviateMiddle(String.valueOf(pJson), "...", 10);
        if (pJson == null) {
            return "null (missing)";
        } else if (pJson.isJsonNull()) {
            return "null (json)";
        } else if (pJson.isJsonArray()) {
            return "an array (" + s + ")";
        } else if (pJson.isJsonObject()) {
            return "an object (" + s + ")";
        } else {
            if (pJson.isJsonPrimitive()) {
                JsonPrimitive jsonprimitive = pJson.getAsJsonPrimitive();
                if (jsonprimitive.isNumber()) {
                    return "a number (" + s + ")";
                }

                if (jsonprimitive.isBoolean()) {
                    return "a boolean (" + s + ")";
                }
            }

            return s;
        }
    }

    @Nullable
    public static <T> T fromNullableJson(Gson pGson, Reader pReader, Class<T> pAdapter, boolean pLenient) {
        try {
            JsonReader jsonreader = new JsonReader(pReader);
            jsonreader.setLenient(pLenient);
            return pGson.getAdapter(pAdapter).read(jsonreader);
        } catch (IOException ioexception) {
            throw new JsonParseException(ioexception);
        }
    }

    public static <T> T fromJson(Gson pGson, Reader pReader, Class<T> pAdapter, boolean pLenient) {
        T t = fromNullableJson(pGson, pReader, pAdapter, pLenient);
        if (t == null) {
            throw new JsonParseException("JSON data was null or empty");
        } else {
            return t;
        }
    }

    @Nullable
    public static <T> T fromNullableJson(Gson pGson, Reader pReader, TypeToken<T> pType, boolean pLenient) {
        try {
            JsonReader jsonreader = new JsonReader(pReader);
            jsonreader.setLenient(pLenient);
            return pGson.getAdapter(pType).read(jsonreader);
        } catch (IOException ioexception) {
            throw new JsonParseException(ioexception);
        }
    }

    public static <T> T fromJson(Gson pGson, Reader pReader, TypeToken<T> pType, boolean pLenient) {
        T t = fromNullableJson(pGson, pReader, pType, pLenient);
        if (t == null) {
            throw new JsonParseException("JSON data was null or empty");
        } else {
            return t;
        }
    }

    @Nullable
    public static <T> T fromNullableJson(Gson pGson, String pJson, TypeToken<T> pType, boolean pLenient) {
        return fromNullableJson(pGson, new StringReader(pJson), pType, pLenient);
    }

    public static <T> T fromJson(Gson pGson, String pJson, Class<T> pAdapter, boolean pLenient) {
        return fromJson(pGson, new StringReader(pJson), pAdapter, pLenient);
    }

    @Nullable
    public static <T> T fromNullableJson(Gson pGson, String pJson, Class<T> pAdapter, boolean pLenient) {
        return fromNullableJson(pGson, new StringReader(pJson), pAdapter, pLenient);
    }

    public static <T> T fromJson(Gson pGson, Reader pReader, TypeToken<T> pType) {
        return fromJson(pGson, pReader, pType, false);
    }

    @Nullable
    public static <T> T fromNullableJson(Gson pGson, String pJson, TypeToken<T> pType) {
        return fromNullableJson(pGson, pJson, pType, false);
    }

    public static <T> T fromJson(Gson pGson, Reader pReader, Class<T> pJsonClass) {
        return fromJson(pGson, pReader, pJsonClass, false);
    }

    public static <T> T fromJson(Gson pGson, String pJson, Class<T> pAdapter) {
        return fromJson(pGson, pJson, pAdapter, false);
    }

    public static JsonObject parse(String pJson, boolean pLenient) {
        return parse(new StringReader(pJson), pLenient);
    }

    public static JsonObject parse(Reader pReader, boolean pLenient) {
        return fromJson(GSON, pReader, JsonObject.class, pLenient);
    }

    public static JsonObject parse(String pJson) {
        return parse(pJson, false);
    }

    public static JsonObject parse(Reader pReader) {
        return parse(pReader, false);
    }

    public static JsonArray parseArray(String pString) {
        return parseArray(new StringReader(pString));
    }

    public static JsonArray parseArray(Reader pReader) {
        return fromJson(GSON, pReader, JsonArray.class, false);
    }

    public static String toStableString(JsonElement pJson) {
        StringWriter stringwriter = new StringWriter();
        JsonWriter jsonwriter = new JsonWriter(stringwriter);

        try {
            writeValue(jsonwriter, pJson, Comparator.naturalOrder());
        } catch (IOException ioexception) {
            throw new AssertionError(ioexception);
        }

        return stringwriter.toString();
    }

    public static void writeValue(JsonWriter pWriter, @Nullable JsonElement pJsonElement, @Nullable Comparator<String> pSorter) throws IOException {
        if (pJsonElement == null || pJsonElement.isJsonNull()) {
            pWriter.nullValue();
        } else if (pJsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonprimitive = pJsonElement.getAsJsonPrimitive();
            if (jsonprimitive.isNumber()) {
                pWriter.value(jsonprimitive.getAsNumber());
            } else if (jsonprimitive.isBoolean()) {
                pWriter.value(jsonprimitive.getAsBoolean());
            } else {
                pWriter.value(jsonprimitive.getAsString());
            }
        } else if (pJsonElement.isJsonArray()) {
            pWriter.beginArray();

            for(JsonElement jsonelement : pJsonElement.getAsJsonArray()) {
                writeValue(pWriter, jsonelement, pSorter);
            }

            pWriter.endArray();
        } else {
            if (!pJsonElement.isJsonObject()) {
                throw new IllegalArgumentException("Couldn't write " + pJsonElement.getClass());
            }

            pWriter.beginObject();

            for(Map.Entry<String, JsonElement> entry : sortByKeyIfNeeded(pJsonElement.getAsJsonObject().entrySet(), pSorter)) {
                pWriter.name(entry.getKey());
                writeValue(pWriter, entry.getValue(), pSorter);
            }

            pWriter.endObject();
        }
    }

    private static Collection<Map.Entry<String, JsonElement>> sortByKeyIfNeeded(
            Collection<Map.Entry<String, JsonElement>> pEntries, @Nullable Comparator<String> pSorter
    ) {
        if (pSorter == null) {
            return pEntries;
        } else {
            List<Map.Entry<String, JsonElement>> list = new ArrayList<>(pEntries);
            list.sort(Map.Entry.comparingByKey(pSorter));
            return list;
        }
    }

}
