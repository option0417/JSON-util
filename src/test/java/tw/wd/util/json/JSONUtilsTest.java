package tw.wd.util.json;

import com.fasterxml.jackson.core.JsonParseException;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


public class JSONUtilsTest {
    private static final String JSON_HAS_OBJECT = "{\"name\":\"Hello\", \"phone\":\"0912345678\", \"id\":102030, \"pl1\":{\"pid\":10001,\"pname\":\"test_project1\"}, \"pl2\":{\"pid\":20001,\"pname\":\"test_project2\"}}";
    private static final String JSON_HAS_STRING_ARRAY = "{\"sl\":[\"AString\", \"BString\", \"\", \"null\", \"NULL\", \"NulL\"]}";
    private static final String JSON_HAS_OBJECT_ARRAY = "{\"name\":\"Hello\", \"phone\":\"0912345678\", \"id\":102030, \"pl\":[{\"pid\":10001,\"pname\":\"test_project1\"},{\"pid\":10002,\"pname\":\"test_project2\"}]}";
    private static final String JSON_HAS_OBJECT_AND_OBJECT_ARRAY = "{\"name\":\"Hello\", \"phone\":\"0912345678\", \"id\":102030, \"plo\":{\"pid\":10001,\"pname\":\"test_project1\"}, \"pl\":[{\"pid\":10001,\"pname\":\"test_project1\"},{\"pid\":10002,\"pname\":\"test_project2\"}]}";
    private static final String JSON_TEXT_1   = "{\"a\":\"aaa\", \"b\":\"bbb\", \"c\":\"ccc\"}";
    private static final String[] VALID_FIELDS  = new String[]{"a", "b", "c"};

    @Test
    public void testToMapWithObject() {
        Exception rtnException          = null;
        Map<String, Object> resultMap   = null;

        try {
            resultMap = JSONUtils.toMap(JSON_HAS_OBJECT);
        } catch (Exception e) {
            rtnException = e;
        }

        assertThat(rtnException,                                                        is(nullValue()));
        assertThat((String) resultMap.get("name"),                                      is("Hello"));
        assertThat((String) resultMap.get("phone"),                                     is("0912345678"));
        assertThat((Integer) resultMap.get("id"),                                           is(102030));
        assertThat((Map<String, Object>) resultMap.get("pl1"),                          is(notNullValue()));
        assertThat((Map<String, Object>) resultMap.get("pl2"),                          is(notNullValue()));
        assertThat((Integer) ((Map<String, Object>) resultMap.get("pl1")).get("pid"),       is(10001));
        assertThat((String) ((Map<String, Object>) resultMap.get("pl1")).get("pname"),  is("test_project1"));
        assertThat((Integer) ((Map<String, Object>) resultMap.get("pl2")).get("pid"),       is(20001));
        assertThat((String) ((Map<String, Object>) resultMap.get("pl2")).get("pname"),  is("test_project2"));
    }

    @Test
    public void testToMapWithStringArray() {
        Throwable rtnThrowable          = null;
        Map<String, Object> resultMap   = null;
        List<String> textList           = null;

        try {
            resultMap   = JSONUtils.toMap(JSON_HAS_STRING_ARRAY);
            textList    = (List<String>) resultMap.get("sl");
        } catch (Throwable t) {
            rtnThrowable = t;
            t.printStackTrace();
        }

        assertThat(rtnThrowable,    is(nullValue()));
        assertThat(textList.size(), is(6));
        assertThat(textList.get(0), is("AString"));
        assertThat(textList.get(1), is("BString"));
        assertThat(textList.get(2), is(""));
        assertThat(textList.get(3), is("null"));
        assertThat(textList.get(4), is("NULL"));
        assertThat(textList.get(5), is("NulL"));
    }

    @Test
    public void testToMapWithObjectArray() {
        Exception rtnException          = null;
        Map<String, Object> resultMap   = null;
        List<Object> results            = null;

        try {
            resultMap   = JSONUtils.toMap(JSON_HAS_OBJECT_ARRAY);
            results     = (List<Object>) resultMap.get("pl");
        } catch (Exception e) {
            rtnException = e;
        }

        assertThat(rtnException,                                                is(nullValue()));
        assertThat((String) resultMap.get("name"),                              is("Hello"));
        assertThat((String) resultMap.get("phone"),                             is("0912345678"));
        assertThat((Integer) resultMap.get("id"),                               is(102030));
        assertThat(results.size(),                                              is(2));
        assertThat((Integer) ((Map<String, Object>)results.get(0)).get("pid"),  is(10001));
        assertThat((Integer) ((Map<String, Object>)results.get(1)).get("pid"),  is(10002));
        assertThat((String) ((Map<String, Object>)results.get(0)).get("pname"), is("test_project1"));
        assertThat((String) ((Map<String, Object>)results.get(1)).get("pname"), is("test_project2"));
    }

    @Test
    public void testToMapWithObjectAndObjectArray() {
        Exception rtnException          = null;
        Map<String, Object> resultMap   = null;
        List<Object> resultList         = null;

        try {
            resultMap   = JSONUtils.toMap(JSON_HAS_OBJECT_AND_OBJECT_ARRAY);
            resultList  = (List<Object>) resultMap.get("pl");
        } catch (Exception e) {
            rtnException = e;
        }

        assertThat(rtnException,                                                        is(nullValue()));
        assertThat((String) resultMap.get("name"),                                      is("Hello"));
        assertThat((String) resultMap.get("phone"),                                     is("0912345678"));
        assertThat((Integer) resultMap.get("id"),                                       is(102030));
        assertThat(resultList.size(),                                                   is(2));
        assertThat((Map<String, Object>) resultMap.get("plo"),                          is(notNullValue()));
        assertThat((Integer) ((Map<String, Object>) resultMap.get("plo")).get("pid"),   is(10001));
        assertThat((String) ((Map<String, Object>) resultMap.get("plo")).get("pname"),  is("test_project1"));
        assertThat((Integer) ((Map<String, Object>)resultList.get(0)).get("pid"),       is(10001));
        assertThat((Integer) ((Map<String, Object>)resultList.get(1)).get("pid"),       is(10002));
        assertThat((String) ((Map<String, Object>)resultList.get(0)).get("pname"),      is("test_project1"));
        assertThat((String) ((Map<String, Object>)resultList.get(1)).get("pname"),      is("test_project2"));
    }

    @Test
    public void testIsFieldExist() {
        Exception rtnException = null;
        boolean isValid = false;

        try {
            isValid = JSONUtils.isFieldExist(JSON_TEXT_1, VALID_FIELDS);
        } catch (Exception e) {
            rtnException = e;
        }

        assertThat(rtnException,    is(nullValue()));
        assertThat(isValid,         is(true));
    }

    @Test
    public void testIsFieldExistWithinvalidField() {
        Exception rtnException = null;
        boolean isValid = false;

        try {
            isValid = JSONUtils.isFieldExist(JSON_TEXT_1, "d");
        } catch (Exception e) {
            rtnException = e;
        }

        assertThat(rtnException,    is(nullValue()));
        assertThat(isValid,         is(false));
    }

    @Test
    public void testGetFieldValueToArray() {
        Exception rtnException  = null;
        String[] groupUsers     = null;
        String jsonText         = "{\"gul\":[\"gu001\",\"gu002\",\"gu003\"]}";


        try {
            groupUsers = JSONUtils.getFieldValue(jsonText, "gul", JSONUtils.TYPE_ARRAY_STRING);
        } catch (Exception e) {
            rtnException = e;
        }

        assertThat(rtnException,        is(nullValue()));
        assertThat(groupUsers.length,   is(3));
        assertThat(groupUsers[0],       is("gu001"));
        assertThat(groupUsers[1],       is("gu002"));
        assertThat(groupUsers[2],       is("gu003"));
    }

    @Test
    public void testIsJSONFormat() {
        Exception rtnException  = null;
        String text1            = "{\"a\":[\"A\",\"B\",\"C\"]}";
        String text2            = "{\"a\":[\"A\",\"B\",\"C\"]";
        String text3            = "{\"a\":[\"A\",\"B\",\"C\"}";
        boolean text1Result     = false;
        boolean text2Result     = true;
        boolean text3Result     = true;

        try {
            text1Result = JSONUtils.isJSONFormat(text1);

            try {
                text2Result = JSONUtils.isJSONFormat(text2);
            } catch (JsonParseException e) {
                text2Result = false;
            } catch (Exception e) {
                rtnException = e;
            }

            try {
                text3Result = JSONUtils.isJSONFormat(text3);
            } catch (JsonParseException e) {
                text3Result = false;
            } catch (Exception e) {
                rtnException = e;
            }
        } catch (Exception e) {
            rtnException = e;
        }

        assertThat(rtnException,    is(nullValue()));
        assertThat(text1Result,     is(true));
        assertThat(text2Result,     is(false));
        assertThat(text3Result,     is(false));
    }
}
