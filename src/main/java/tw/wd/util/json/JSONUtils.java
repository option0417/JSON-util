package tw.wd.util.json;

import com.fasterxml.jackson.core.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class JSONUtils {
    public static final byte TYPE_INT           = 0x01;
    public static final byte TYPE_LONG          = 0x02;
    public static final byte TYPE_STRING        = 0x03;
    public static final byte TYPE_ARRAY_INT     = 0x04;
    public static final byte TYPE_ARRAY_LONG    = 0x05;
    public static final byte TYPE_ARRAY_STRING  = 0x06;


	public static Map<String, Object> toMap(String jsonText) throws Exception {
        if (jsonText == null || jsonText.length() == 0) {
            return new HashMap<String, Object>();
        }

        return toMap(new JsonFactory().createParser(jsonText));
	}

    public static Map<String, Object> toMap(JsonParser jsonParser) throws Exception {
        if (jsonParser == null) {
            return new HashMap<String, Object>();
        }

        Map<String, Object> jsonMap 	= new HashMap<String, Object>();
        String key                      = null;
        Object value                    = null;
        JsonToken jsonToken 			= null;


        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            jsonParser.nextToken();
        }

        while ((jsonToken = jsonParser.nextToken()) != JsonToken.END_OBJECT) {
            if (jsonToken == JsonToken.START_OBJECT) {
                value = toMap(jsonParser);
            } else if (jsonToken == JsonToken.START_ARRAY) {
                value = toArray(jsonParser);
            } else if (jsonToken == JsonToken.FIELD_NAME) {
                key = jsonParser.getCurrentName();
            } else {
                value = toValueObject(jsonParser);
            }

            if (key != null && value != null) {
                jsonMap.put(key, value);
                key     = null;
                value   = null;
            }
        }

        return jsonMap;
    }

    private static List<Object> toArray(JsonParser jsonParser) throws Exception {
        List<Object> valueList   = new ArrayList<Object>();
        JsonToken jsonToken         = null;
        Object value                = null;

        if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
            jsonParser.nextToken();
        }

        while ((jsonToken = jsonParser.nextToken()) != JsonToken.END_ARRAY) {
            if (jsonToken == JsonToken.START_OBJECT) {
                value = toMap(jsonParser);
            } else if (jsonToken == JsonToken.START_ARRAY) {
                value = toArray(jsonParser);
            } else {
                value = toValueObject(jsonParser);
            }

            if (value != null) {
                valueList.add(value);
            }
        }

        return valueList;
    }

    private static Object toValueObject(JsonParser jsonParser) throws Exception {
        JsonToken jsonToken = jsonParser.getCurrentToken();

        if (JsonToken.VALUE_STRING == jsonToken) {
            return jsonParser.getText();
        } else if (JsonToken.VALUE_NUMBER_INT == jsonToken) {
            try {
                return jsonParser.getIntValue();
            } catch (JsonParseException e) {
                return jsonParser.getLongValue();
            }
        } else if (JsonToken.VALUE_TRUE == jsonToken || JsonToken.VALUE_FALSE == jsonToken) {
            return jsonParser.getBooleanValue();
        } else if (JsonToken.VALUE_NUMBER_FLOAT == jsonToken) {
            return jsonParser.getFloatValue();
        } else if (JsonToken.VALUE_EMBEDDED_OBJECT == jsonToken) {
            return jsonParser.getEmbeddedObject();
        } else if (JsonToken.VALUE_NULL == jsonToken) {
            return "";
        } else {
            return null;
        }
    }
	
	public static String toJSON(String fieldName, Object fieldValue) {
		Map<String, Object> jsonMap = new HashMap<String, Object>(1);
		jsonMap.put(fieldName, fieldValue);
		return toJSON(jsonMap);
	}

    public static boolean isFieldExist(String jsonText, String... fields) throws IOException {
        Set<String> fieldSet    = new HashSet<String>();
        JsonFactory jsonFactory = new JsonFactory();
        JsonParser jsonParser   = jsonFactory.createParser(jsonText);
        JsonToken jsonToken     = null;
        String fieldName        = null;

        for (String s : fields) {
            fieldSet.add(s);
        }

        while ((jsonToken = jsonParser.nextToken()) != JsonToken.END_OBJECT) {
            if (jsonToken == JsonToken.START_OBJECT) {
                continue;
            } else if (jsonToken == JsonToken.FIELD_NAME) {
                fieldName = jsonParser.getCurrentName();

                if (!fieldSet.remove(fieldName)) {
                    return false;
                }
            }
        }

        return fieldSet.size() == 0 ? true : false;
    }
	
	public static String toJSON(Map<String, Object> valueMap) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();		
		JsonFactory jsonFactory = new JsonFactory();
		try {
			JsonGenerator jsonGenerator = jsonFactory.createGenerator(byteArrayOutputStream, JsonEncoding.UTF8);
			jsonGenerator.writeStartObject();
			
			Iterator<Entry<String, Object>> iter = valueMap.entrySet().iterator();			
			while (iter.hasNext()) {
				Entry<String, Object> entry = iter.next();
				
				jsonGenerator.writeFieldName(entry.getKey());				
				writeFieldValue(jsonGenerator, entry.getValue());
			}
			
			jsonGenerator.flush();
			jsonGenerator.close();
			return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return new String(byteArrayOutputStream.toByteArray());
		} finally {
			try {
				byteArrayOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void writeFieldValue(JsonGenerator jsonGenerator, Object value) throws IOException {

        if (value == null) {
            jsonGenerator.writeNull();
        } else {
            Class<?> valueClazz = value.getClass();

            if (String.class.isAssignableFrom(valueClazz)) {
                byte[] textBytes = ((String)value).getBytes("UTF-8");
                jsonGenerator.writeUTF8String(textBytes, 0, textBytes.length);
            } else if (Integer.class.isAssignableFrom(valueClazz)) {
                jsonGenerator.writeNumber((Integer)value);
            } else if (String[].class.isAssignableFrom(valueClazz)) {
                jsonGenerator.writeStartArray();

                String[] values = (String[])value;
                for (String s : values) {
                    writeFieldValue(jsonGenerator, s);
                }

                jsonGenerator.writeEndArray();
            } else if (Long.class.isAssignableFrom(valueClazz)) {
                jsonGenerator.writeNumber((Long)value);
            } else if (Boolean.class.isAssignableFrom(valueClazz)) {
                jsonGenerator.writeBoolean((Boolean)value);
            } else if (Double.class.isAssignableFrom(valueClazz)) {
                jsonGenerator.writeNumber((Double)value);
            } else if (JSONTranslatable.class.isAssignableFrom(valueClazz)) {
                JSONTranslatable jsonTranslatable = (JSONTranslatable) value;
                jsonGenerator.writeRawValue(jsonTranslatable.toJSON());
            } else if (JSONTranslatable[].class.isAssignableFrom(valueClazz)) {
                jsonGenerator.writeStartArray();

                JSONTranslatable[] jsonTranslatables = (JSONTranslatable[]) value;

                for (JSONTranslatable jsonTranslatable : jsonTranslatables) {
                    jsonGenerator.writeRawValue(jsonTranslatable.toJSON());
                }

                jsonGenerator.writeEndArray();
            } else if (List.class.isAssignableFrom(valueClazz)) {
                jsonGenerator.writeStartArray();

                List<?> values      = (List<?>)value;
                Iterator<?> iter    = values.iterator();

                while (iter.hasNext()) {
                    writeFieldValue(jsonGenerator, iter.next());
                }

                jsonGenerator.writeEndArray();
            } else if (Map.class.isAssignableFrom(valueClazz)) {
                jsonGenerator.writeStartObject();

                Map<String, Object> valueMap = (Map<String, Object>) value;
                jsonGenerator.writeRawValue(JSONUtils.toJSON(valueMap));

                jsonGenerator.writeEndObject();
            } else if (byte.class.isAssignableFrom(valueClazz)) {
	            jsonGenerator.writeNumber((Integer) value);
            }
        }
	}


    public static Object[] getFieldValue(String jsonText, String[] fieldNames, byte[] fieldTypes) throws IOException {
        if (fieldNames.length == fieldTypes.length) {

            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jsonParser   = jsonFactory.createParser(jsonText);
            JsonToken jsonToken     = null;
            int arrayLength         = fieldNames.length;
            Object[] rtnValue       = new Object[arrayLength];

            while ((jsonToken = jsonParser.nextToken()) != JsonToken.END_OBJECT) {
                if (jsonToken == JsonToken.START_OBJECT) {
                    continue;
                } else if (jsonToken == JsonToken.FIELD_NAME) {

                    String currFieldName = jsonParser.getCurrentName();

                    for (int idx = 0; idx < arrayLength; idx++) {

                        String fieldName = fieldNames[idx];

                        if (currFieldName.equals(fieldName)) {

                            jsonParser.nextToken();
                            rtnValue[idx] = getFieldValue(jsonParser, fieldTypes[idx]);

                        }
                    }
                }
            }

            return rtnValue;
        } else {
            throw new RuntimeException("Length of FieldName and FieldType not equal.");
        }
    }

    public static <T> T getFieldValue(String jsonText, String fieldName, byte fieldType) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        JsonParser jsonParser   = jsonFactory.createParser(jsonText);
        JsonToken jsonToken     = null;

        while ((jsonToken = jsonParser.nextToken()) != JsonToken.END_OBJECT) {
            if (jsonToken == JsonToken.START_OBJECT) {
                continue;
            } else if (jsonToken == JsonToken.FIELD_NAME) {
                String currFieldName = jsonParser.getCurrentName();

                if (fieldName.equals(currFieldName)) {
                    jsonParser.nextToken();
                    return getFieldValue(jsonParser, fieldType);
                }
            }
        }

        return null;
    }

    public static <T> T getFieldValue(JsonParser jsonParser, byte fieldType) throws IOException {
        switch (fieldType) {
            case TYPE_INT:
                return (T)Integer.valueOf(jsonParser.getIntValue());
            case TYPE_LONG:
                return (T)Long.valueOf(jsonParser.getLongValue());
            case TYPE_STRING:
                return (T)jsonParser.getText();
            case TYPE_ARRAY_INT:
                return (T)writeIntValueToArray(jsonParser);
            case TYPE_ARRAY_LONG:
                return (T)writeLongValueToArray(jsonParser);
            case TYPE_ARRAY_STRING:
                return (T)writeStringValueToArray(jsonParser);
        }
        return null;
    }

    private static int[] writeIntValueToArray(JsonParser jsonParser) throws IOException {
        List<Integer> integerList = null;

        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {

            integerList = new ArrayList<Integer>();

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        integerList.add(jsonParser.getIntValue());
                    }
                }
            }

            int[] intVals = new int[integerList.size()];
            for (int idx = 0; idx < integerList.size(); idx++) {
                intVals[idx] = integerList.get(idx);
            }

            return intVals;
        } else {
            return new int[0];
        }
    }

    private static long[] writeLongValueToArray(JsonParser jsonParser) throws IOException {
        List<Long> longList = null;

        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {

            longList = new ArrayList<Long>();

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        longList.add(jsonParser.getLongValue());
                    }
                }
            }

            long[] longVals = new long[longList.size()];
            for (int idx = 0; idx < longList.size(); idx++) {
                longVals[idx] = longList.get(idx);
            }
            return longVals;
        } else {
            return new long[0];
        }
    }

    private static String[] writeStringValueToArray(JsonParser jsonParser) throws IOException {
        List<String> strList = null;

        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {

            strList = new ArrayList<String>();

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                strList.add(jsonParser.getText());
            }

            String[] strVals = new String[strList.size()];
            for (int idx = 0; idx < strList.size(); idx++) {
                strVals[idx] = strList.get(idx);
            }
            return strVals;
        } else {
            return new String[0];
        }
    }

    public static boolean isJSONFormat(String text) throws Exception {
        JsonFactory jsonFactory             = new JsonFactory();
        JsonParser jsonParser               = jsonFactory.createParser(text);
        JsonToken processJsonToken          = null;
        boolean isStartChecked              = false;
        Stack<JsonToken> jsonTokensStack    = new Stack<JsonToken>();


        while ((processJsonToken = jsonParser.nextToken()) != null) {
            if (JsonToken.START_OBJECT == processJsonToken) {
                jsonTokensStack.push(JsonToken.START_OBJECT);
            } else if (JsonToken.START_ARRAY == processJsonToken) {
                jsonTokensStack.push(JsonToken.START_ARRAY);
            } else if (JsonToken.END_OBJECT == processJsonToken) {
                if (jsonTokensStack.pop() != JsonToken.START_OBJECT) {
                    return false;
                }
            } else if (JsonToken.END_ARRAY == processJsonToken) {
                if (jsonTokensStack.pop() != JsonToken.START_ARRAY) {
                    return false;
                }
            }
        }

        return true;
    }

    public interface JSONTranslatable {
        /**
         * Trans pbject to json-format text.
         * @return
         */
        public String toJSON();

        /**
         * Trans json-format text to specific object.
         * @param jsonText
         * @param <T>
         * @return
         */
        public <T> T fromJSON(String jsonText);
    }
}
