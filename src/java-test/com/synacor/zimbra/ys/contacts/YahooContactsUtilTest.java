/**
 * 
 */
package com.synacor.zimbra.ys.contacts;
import static com.zimbra.common.mailbox.ContactConstants.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.ParsedContact;

/**
 * @author Greg Solovyev
 *
 */
public class YahooContactsUtilTest {

    /**
     * Test method for {@link com.synacor.zimbra.ys.contacts.YahooContactsUtil#parseSimpleField(com.google.gson.JsonObject, java.lang.String, java.util.Map)}.
     */
    @Test
    public void testParseSimpleField() {
        String sampleField = "{"
                + "\"id\": 14392,"
                + "\"type\": \"company\","
                + "\"value\": \"Turbosquid, Inc\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2010-12-01T16:36:06Z\","
                + "\"created\": \"2010-12-01T16:36:06Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/Y7B2YG3W4RG6HESVPWQZXSH6JE/contact/6364/company/14392\"}";
        JsonParser parser = new JsonParser();
        JsonObject fieldObject = parser.parse(sampleField).getAsJsonObject();
        Map<String, String> fields = new HashMap<String, String>();
        YahooContactsUtil.parseSimpleField(fieldObject, A_company, fields);
        assertTrue("should have 'company'", fields.containsKey(A_company));
        assertEquals("wrong company value", "Turbosquid, Inc", fields.get(A_company));
    }

    /**
     * Test method for {@link com.synacor.zimbra.ys.contacts.YahooContactsUtil#parseFlaggedField(com.google.gson.JsonObject, java.util.Map)}.
     */
    @Test
    public void testParseEmailField() {
        String sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"email\","
                + "\"value\": \"vagarwal@zimbra.com\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/email/3657\"}";
        JsonParser parser = new JsonParser();
        JsonObject fieldObject = parser.parse(sampleField).getAsJsonObject();
        Map<String, String> fields = new HashMap<String, String>();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_email, YahooContactsUtil.EMAIL_FIELDS_MAP, fields);
        assertTrue("should have email", fields.containsKey(A_email));
        assertFalse("should not have workEmail1", fields.containsKey(A_workEmail1));
        assertEquals("wrong email value", "vagarwal@zimbra.com", fields.get(A_email));
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"email\","
                + "\"value\": \"vagarwal@zimbra.com\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"WORK\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/email/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_email, YahooContactsUtil.EMAIL_FIELDS_MAP,  fields);
        assertTrue("should have workEmail1", fields.containsKey(A_workEmail1));
        assertFalse(fields.containsKey(A_email));
        assertEquals("wrong email value", "vagarwal@zimbra.com", fields.get(A_workEmail1));
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"email\","
                + "\"value\": \"vagarwal@zimbra.com\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"PERSONAL\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/email/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_email, YahooContactsUtil.EMAIL_FIELDS_MAP, fields);
        assertTrue("should have email", fields.containsKey(A_email));
        assertFalse(fields.containsKey(A_workEmail1));
        assertEquals("wrong email value", "vagarwal@zimbra.com", fields.get(A_email));
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"email\","
                + "\"value\": \"vagarwal@zimbra.com\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"HOME\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/email/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_email, YahooContactsUtil.EMAIL_FIELDS_MAP, fields);
        assertTrue("should have email", fields.containsKey(A_email));
        assertFalse(fields.containsKey(A_workEmail1));
        assertEquals("wrong email value", "vagarwal@zimbra.com", fields.get(A_email));
        //add another personal email
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"email\","
                + "\"value\": \"vikas.agarwal@gmail.com\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/email/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_email, YahooContactsUtil.EMAIL_FIELDS_MAP, fields);
        assertTrue("should have email", fields.containsKey(A_email));
        assertTrue("missing second personal email", fields.containsKey(A_email2));
        assertFalse(fields.containsKey(A_workEmail1));
        assertEquals("wrong email value", "vagarwal@zimbra.com", fields.get(A_email));
        assertEquals("wrong email value", "vikas.agarwal@gmail.com", fields.get(A_email2));
        //add a work email
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"email\","
                + "\"value\": \"agarwal.vikas@synacor.com\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"WORK\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/email/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_email, YahooContactsUtil.EMAIL_FIELDS_MAP, fields);
        assertTrue("should have email", fields.containsKey(A_email));
        assertTrue("should have email2", fields.containsKey(A_email2));
        assertTrue("should have workEmail1", fields.containsKey(A_workEmail1));
        assertEquals("wrong email value", "vagarwal@zimbra.com", fields.get(A_email));
        assertEquals("wrong email value", "vikas.agarwal@gmail.com", fields.get(A_email2));
        assertEquals("wrong email value", "agarwal.vikas@synacor.com", fields.get(A_workEmail1));
        //add second work email
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"email\","
                + "\"value\": \"vikas@zimbra.org\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"WORK\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/email/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_email, YahooContactsUtil.EMAIL_FIELDS_MAP, fields);
        assertTrue("should have email", fields.containsKey(A_email));
        assertTrue("should have email2", fields.containsKey(A_email2));
        assertTrue("should have workEmail1", fields.containsKey(A_workEmail1));
        assertTrue(fields.containsKey(A_workEmail2));
        assertEquals("wrong email value", "vagarwal@zimbra.com", fields.get(A_email));
        assertEquals("wrong email value", "vikas.agarwal@gmail.com", fields.get(A_email2));
        assertEquals("wrong email value", "agarwal.vikas@synacor.com", fields.get(A_workEmail1));
        assertEquals("wrong email value", "vikas@zimbra.org", fields.get(A_workEmail2));
    }

    /**
     * Test method for {@link com.synacor.zimbra.ys.contacts.YahooContactsUtil#parsePhoneField(com.google.gson.JsonObject, java.util.Map)}.
     */
    @Test
    public void testParsePhoneField() {
        String sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"phone\","
                + "\"value\": \"408-124-4477\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/phone/3657\"}";
        JsonParser parser = new JsonParser();
        JsonObject fieldObject = parser.parse(sampleField).getAsJsonObject();
        Map<String, String> fields = new HashMap<String, String>();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_homePhone, YahooContactsUtil.PHONE_FIELDS_MAP, fields);
        assertTrue(fields.containsKey(A_homePhone));
        assertFalse(fields.containsKey(A_workPhone));
        assertEquals("wrong email value", "408-124-4477", fields.get(A_homePhone));
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"phone\","
                + "\"value\": \"408-124-4477\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"WORK\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/phone/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_homePhone, YahooContactsUtil.PHONE_FIELDS_MAP, fields);
        assertTrue(fields.containsKey(A_workPhone));
        assertFalse(fields.containsKey(A_homePhone));
        assertEquals("wrong email value", "408-124-4477", fields.get(A_workPhone));
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"phone\","
                + "\"value\": \"408-124-4477\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"PERSONAL\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/phone/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_homePhone, YahooContactsUtil.PHONE_FIELDS_MAP, fields);
        assertTrue(fields.containsKey(A_homePhone));
        assertFalse(fields.containsKey(A_workPhone));
        assertEquals("wrong email value", "408-124-4477", fields.get(A_homePhone));
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"phone\","
                + "\"value\": \"408-124-4477\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"HOME\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/phone/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_homePhone, YahooContactsUtil.PHONE_FIELDS_MAP, fields);
        assertTrue(fields.containsKey(A_homePhone));
        assertFalse(fields.containsKey(A_workPhone));
        assertEquals("wrong email value", "408-124-4477", fields.get(A_homePhone));
        //add another personal phone
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"phone\","
                + "\"value\": \"(415) 222-44-56\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/phone/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_homePhone, YahooContactsUtil.PHONE_FIELDS_MAP, fields);
        assertTrue(fields.containsKey(A_homePhone));
        assertTrue("missing second personal email", fields.containsKey(A_homePhone2));
        assertFalse(fields.containsKey(A_workPhone));
        assertEquals("wrong email value", "408-124-4477", fields.get(A_homePhone));
        assertEquals("wrong email value", "(415) 222-44-56", fields.get(A_homePhone2));
        //add a work phone
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"phone\","
                + "\"value\": \"(650) 333-44-55\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"WORK\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/phone/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_homePhone, YahooContactsUtil.PHONE_FIELDS_MAP, fields);
        assertTrue(fields.containsKey(A_homePhone));
        assertTrue(fields.containsKey(A_homePhone2));
        assertTrue(fields.containsKey(A_workPhone));
        assertEquals("wrong email value", "408-124-4477", fields.get(A_homePhone));
        assertEquals("wrong email value", "(415) 222-44-56", fields.get(A_homePhone2));
        assertEquals("wrong email value", "(650) 333-44-55", fields.get(A_workPhone));
        //add second work phone
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"phone\","
                + "\"value\": \"6501234567\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"WORK\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/phone/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_homePhone, YahooContactsUtil.PHONE_FIELDS_MAP, fields);
        assertTrue(fields.containsKey(A_homePhone));
        assertTrue(fields.containsKey(A_homePhone2));
        assertTrue(fields.containsKey(A_workPhone));
        assertTrue(fields.containsKey(A_workPhone2));
        assertEquals("wrong email value", "408-124-4477", fields.get(A_homePhone));
        assertEquals("wrong email value", "(415) 222-44-56", fields.get(A_homePhone2));
        assertEquals("wrong email value", "(650) 333-44-55", fields.get(A_workPhone));
        assertEquals("wrong email value", "6501234567", fields.get(A_workPhone2));
        //add mobile phone
        sampleField = "{"
                + "\"id\": 3657,"
                + "\"type\": \"phone\","
                + "\"value\": \"(501) 98734-34\","
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [\"MOBILE\"],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/phone/3657\"}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        YahooContactsUtil.parseFlaggedField(fieldObject, A_homePhone, YahooContactsUtil.PHONE_FIELDS_MAP, fields);
        assertTrue(fields.containsKey(A_homePhone));
        assertTrue(fields.containsKey(A_homePhone2));
        assertTrue(fields.containsKey(A_workPhone));
        assertTrue(fields.containsKey(A_workPhone2));
        assertTrue(fields.containsKey(A_mobilePhone));
        assertEquals("wrong email value", "408-124-4477", fields.get(A_homePhone));
        assertEquals("wrong email value", "(415) 222-44-56", fields.get(A_homePhone2));
        assertEquals("wrong email value", "(650) 333-44-55", fields.get(A_workPhone));
        assertEquals("wrong email value", "6501234567", fields.get(A_workPhone2));
        assertEquals("wrong email value", "(501) 98734-34", fields.get(A_mobilePhone));
    }

    /**
     * Test method for {@link com.synacor.zimbra.ys.contacts.YahooContactsUtil#parseDateField(com.google.gson.JsonObject, java.util.Locale, java.util.Map)}.
     */
    @Test
    public void testParseDateField() {
        String sampleField ="{" 
                + "\"id\": 3658,"
                + "\"type\": \"birthday\","
                + "\"value\": {"
                + " \"day\": \"5\","
                + "\"month\": \"11\","
                + "\"year\": \"2007\""
                + "},"
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/birthday/3658\""
                + "}";
        JsonParser parser = new JsonParser();
        JsonObject fieldObject = parser.parse(sampleField).getAsJsonObject();
        Map<String, String> fields = new HashMap<String, String>();
        YahooContactsUtil.parseDateField(fieldObject, Locale.US, fields);
        assertNotNull("should have birthday", fields.get(A_birthday));
        assertNull("should not have anniversary", fields.get(A_anniversary));
        assertEquals("wrong date value", "11/5/07", fields.get(A_birthday));
        YahooContactsUtil.parseDateField(fieldObject, Locale.UK, fields);
        assertEquals("wrong date value", "05/11/07", fields.get(A_birthday));

        sampleField ="{" 
                + "\"id\": 3658,"
                + "\"type\": \"anniversary\","
                + "\"value\": {"
                + " \"day\": \"15\","
                + "\"month\": \"1\","
                + "\"year\": \"2007\""
                + "},"
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/birthday/3658\""
                + "}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseDateField(fieldObject, Locale.US, fields);
        assertNotNull("should have anniversary", fields.get(A_anniversary));
        assertNull("should not have birthday", fields.get(A_birthday));
        assertEquals("wrong date value", "1/15/07", fields.get(A_anniversary));
        YahooContactsUtil.parseDateField(fieldObject, Locale.UK, fields);
        assertEquals("wrong date value", "15/01/07", fields.get(A_anniversary));

        sampleField ="{" 
                + "\"id\": 3658,"
                + "\"type\": \"blahblah\","
                + "\"value\": {"
                + " \"day\": \"15\","
                + "\"month\": \"1\","
                + "\"year\": \"2007\""
                + "},"
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/birthday/3658\""
                + "}";
        parser = new JsonParser();
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseDateField(fieldObject, Locale.US, fields);
        assertTrue("should not have parsed an unknown date field", fields.isEmpty());
        YahooContactsUtil.parseDateField(fieldObject, Locale.UK, fields);
        assertTrue("should not have parsed an unknown date field", fields.isEmpty());
    }

    /**
     * Test method for {@link com.synacor.zimbra.ys.contacts.YahooContactsUtil#parseNameField(com.google.gson.JsonObject, java.util.Map)}.
     */
    @Test
    public void testParseNameField() {
        String sampleField = "{\"id\":5725,\"type\":\"name\",\"value\":{\"givenName\":\"Jiho\",\"middleName\":\"Sam\",\"familyName\":\"Hahm\",\"prefix\":\"\",\"suffix\":\"\",\"givenNameSound\":\"\",\"familyNameSound\":\"\"},\"editedBy\":\"OWNER\",\"flags\":[],\"categories\":[],\"updated\":\"2008-06-03T08:55:37Z\",\"created\":\"2008-06-03T08:55:37Z\",\"uri\":\"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/2437/name/5725\"}";
        JsonParser parser = new JsonParser();
        JsonObject fieldObject = parser.parse(sampleField).getAsJsonObject();
        Map<String, String> fields = new HashMap<String, String>();
        YahooContactsUtil.parseNameField(fieldObject, fields);
        assertNotNull("missing firstName", fields.get(A_firstName));
        assertEquals("wrong firstName", "Jiho", fields.get(A_firstName));
        assertNotNull("missing lastName", fields.get(A_lastName));
        assertEquals("wrong lastName", "Hahm", fields.get(A_lastName));
        assertNotNull("missing middleName", fields.get(A_middleName));
        assertEquals("wrong middleName", "Sam", fields.get(A_middleName));
        assertFalse("should not have namePrefix", fields.containsKey(A_namePrefix));
        assertFalse("should not have nameSuffix", fields.containsKey(A_nameSuffix));
        assertEquals("should have 3 fields", 3, fields.size());
    }

    /**
     * Test method for {@link com.synacor.zimbra.ys.contacts.YahooContactsUtil#parseAddressField(com.google.gson.JsonObject, java.util.Map)}.
     */
    @Test
    public void testParseAddressField() {
        String sampleField = "{ "
            + "\"id\": 3708, "
            + "\"type\": \"address\","
            + "\"value\": {"
            + "\"street\": \"3026 N Campbell Ave\","
            + "\"city\": \"Tucson\","
            + "\"stateOrProvince\": \"AZ\","
            + "\"postalCode\": \"85719\""
            + "},"
            + "\"editedBy\": \"OWNER\","
            + "\"flags\": ["
            + "\"WORK\""
            + "],"
            + "\"categories\": [],"
            + "\"updated\": \"2009-04-11T22:02:18Z\","
            + "\"created\": \"2008-06-03T08:54:22Z\","
            + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1609/address/3708\"}";
        JsonParser parser = new JsonParser();
        JsonObject fieldObject = parser.parse(sampleField).getAsJsonObject();
        Map<String, String> fields = new HashMap<String, String>();
        YahooContactsUtil.parseAddressField(fieldObject, fields);
        assertNotNull("missing work street", fields.get(A_workStreet));
        assertEquals("wrong work street", "3026 N Campbell Ave", fields.get(A_workStreet));
        assertFalse("should not have homeStreet", fields.containsKey(A_homeStreet));
        assertFalse("shoud not have country", fields.containsKey(A_workCountry));
        assertNotNull("missing city", fields.get(A_workCity));
        assertEquals("wrong city", "Tucson", fields.get(A_workCity));
        assertNotNull("missing state", fields.get(A_workState));
        assertEquals("wrong state", "AZ", fields.get(A_workState));
        assertNotNull("missing zip", fields.get(A_workPostalCode));
        assertEquals("wrong zip", "85719", fields.get(A_workPostalCode));

        sampleField = "{ "
                + "\"id\": 3809, "
                + "\"type\": \"address\","
                + "\"value\": {"
                + "\"street\": \"3016 S Campbell Str\","
                + "\"city\": \"Las Vegas\","
                + "\"stateOrProvince\": \"NV\","
                + "\"postalCode\": \"85717\""
                + "},"
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": ["
                + "\"HOME\""
                + "],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:02:18Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1609/address/3708\"}";
        fieldObject = parser.parse(sampleField).getAsJsonObject();
        fields = new HashMap<String, String>();
        YahooContactsUtil.parseAddressField(fieldObject, fields);
        assertNotNull("missing work street", fields.get(A_homeStreet));
        assertEquals("wrong work street", "3016 S Campbell Str", fields.get(A_homeStreet));
        assertFalse("should not have workStreet", fields.containsKey(A_workStreet));
        assertFalse("shoud not have country", fields.containsKey(A_homeCountry));
        assertNotNull("missing city", fields.get(A_homeCity));
        assertEquals("wrong city", "Las Vegas", fields.get(A_homeCity));
        assertNotNull("missing state", fields.get(A_homeState));
        assertEquals("wrong state", "NV", fields.get(A_homeState));
        assertNotNull("missing zip", fields.get(A_homePostalCode));
        assertEquals("wrong zip", "85717", fields.get(A_homePostalCode));
    }

    /**
     * Test method for {@link com.synacor.zimbra.ys.contacts.YahooContactsUtil#parseValuePart(com.google.gson.JsonObject, java.lang.String, java.lang.String, java.util.Map)}.
     */
    @Test
    public void testParseValuePart1() {
        String sampleValue = "{\"givenName\": \"Alexander\",\"middleName\": \"\",\"familyName\": \"Dusseldorf\",\"prefix\": \"\",\"suffix\": \"\",\"givenNameSound\": \"\",\"familyNameSound\": \"\"}";
        JsonParser parser = new JsonParser();
        JsonObject valueObject = parser.parse(sampleValue).getAsJsonObject();
        Map<String, String> fields = new HashMap<String, String>();
        for (String key : YahooContactsUtil.NAME_FIELDS_MAP.keySet()) {
            YahooContactsUtil.parseValuePart(valueObject, YahooContactsUtil.NAME_FIELDS_MAP.get(key), key, fields);
        }
        assertNotNull("missing firstName", fields.get(A_firstName));
        assertEquals("wrong firstName", "Alexander", fields.get(A_firstName));
        assertNotNull("missing lastName", fields.get(A_lastName));
        assertEquals("wrong lastName", "Dusseldorf", fields.get(A_lastName));
        assertFalse("should not have namePrefix", fields.containsKey(A_namePrefix));
        assertFalse("should not have nameSuffix", fields.containsKey(A_nameSuffix));
        assertEquals("should have 2 fields", 2, fields.size());
    }

    /**
     * Test method for {@link com.synacor.zimbra.ys.contacts.YahooContactsUtil#parseValuePart(com.google.gson.JsonObject, java.util.List, java.lang.String, java.util.Map)}.
     */
    @Test
    public void testParseValuePart2() {
        String sampleValue =  "{\"street\": \"3026 N Campbell Ave\", \"city\": \"Tucson\",\"stateOrProvince\": \"AZ\",\"postalCode\": \"85719\"}";
        JsonParser parser = new JsonParser();
        JsonObject valueObject = parser.parse(sampleValue).getAsJsonObject();
        Map<String, String> fields = new HashMap<String, String>();
        for (String key : YahooContactsUtil.HOME_ADDRESS_FIELDS_MAP.keySet()) {
            YahooContactsUtil.parseValuePart(valueObject, YahooContactsUtil.HOME_ADDRESS_FIELDS_MAP.get(key), key, fields);
        }
        assertNotNull("missing street", fields.get(A_homeStreet));
        assertEquals("wrong street", "3026 N Campbell Ave", fields.get(A_homeStreet));
        assertNotNull("missing city", fields.get(A_homeCity));
        assertEquals("wrong city", "Tucson", fields.get(A_homeCity));
        assertNotNull("missing state", fields.get(A_homeState));
        assertEquals("wrong state", "AZ", fields.get(A_homeState));
        assertNotNull("missing zip", fields.get(A_homePostalCode));
        assertEquals("wrong zip", "85719", fields.get(A_homePostalCode));
        assertFalse("should not have homeCountry", fields.containsKey(A_homeCountry));
        assertFalse("should not have workCountry", fields.containsKey(A_workCountry));
        assertFalse("should not have workStreet", fields.containsKey(A_workStreet));
        assertFalse("should not have workCity", fields.containsKey(A_workCity));

        sampleValue =  "{\"street\": \"3026 N Campbell Ave\", \"city\": \"Tucson\",\"stateOrProvince\": \"AZ\",\"postalCode\": \"85719\", \"country\":\"United States\"}";
        valueObject = parser.parse(sampleValue).getAsJsonObject();
        fields = new HashMap<String, String>();
        for (String key : YahooContactsUtil.WORK_ADDRESS_FIELDS_MAP.keySet()) {
            YahooContactsUtil.parseValuePart(valueObject, YahooContactsUtil.WORK_ADDRESS_FIELDS_MAP.get(key), key, fields);
        }
        assertNotNull("missing street", fields.get(A_workStreet));
        assertEquals("wrong street", "3026 N Campbell Ave", fields.get(A_workStreet));
        assertFalse("should not have homeStreet", fields.containsKey(A_homeStreet));
        assertNotNull("missing country", fields.get(A_workCountry));
        assertEquals("wrong country", "United States", fields.get(A_workCountry));

        sampleValue =  "{\"street\": \"3026 N Campbell Ave\", \"city\": \"Tucson\",\"stateOrProvince\": \"AZ\",\"postalCode\": \"85719\", \"countryCode\":\"US\"}";
        valueObject = parser.parse(sampleValue).getAsJsonObject();
        fields = new HashMap<String, String>();
        for (String key : YahooContactsUtil.OTHER_ADDRESS_FIELDS_MAP.keySet()) {
            YahooContactsUtil.parseValuePart(valueObject, YahooContactsUtil.OTHER_ADDRESS_FIELDS_MAP.get(key), key, fields);
        }
        assertNotNull("missing street", fields.get(A_otherStreet));
        assertEquals("wrong street", "3026 N Campbell Ave", fields.get(A_otherStreet));
        assertFalse("should not have homeStreet", fields.containsKey(A_homeStreet));
        assertNotNull("missing country", fields.get(A_otherCountry));
        assertEquals("wrong country", "US", fields.get(A_otherCountry));
    }

    @Test
    public void testParseYContact() throws ServiceException {
        String sampleValue = "{"
                + "\"isConnection\": false,"
                + "\"id\": 1705,"
                + "\"fields\": ["
                + "{"
                + "\"id\": 3932,"
                + "\"type\": \"name\","
                + "\"value\": {"
                + "\"givenName\": \"\","
                + "\"middleName\": \"\","
                + "\"familyName\": \"\","
                + "\"prefix\": \"\","
                + "\"suffix\": \"\","
                + "\"givenNameSound\": \"\","
                + "\"familyNameSound\": \"\""
                + "},"
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2008-06-03T08:54:28Z\","
                + "\"created\": \"2008-06-03T08:54:28Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/Y7B2YG3W4RG6HESVPWQZXSH6JE/contact/1705/name/3932\""
                + "}"
                + "],"
                + "\"categories\": [],"
                + "\"error\": 0,"
                + "\"restoredId\": 0,"
                + "\"created\": \"2008-06-03T08:54:28Z\","
                + "\"updated\": \"2008-06-03T08:54:28Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/Y7B2YG3W4RG6HESVPWQZXSH6JE/contact/1705\""
                + "}";
        JsonParser parser = new JsonParser();
        JsonObject contactObject = parser.parse(sampleValue).getAsJsonObject();
        ParsedContact contact = YahooContactsUtil.parseYContact(contactObject, null);
        assertNull("should not get a parsed contact for JSON with no values", contact);
        sampleValue = "{"
                + "\"isConnection\": false,"
                + "\"id\": 1705,"
                + "\"fields\": ["
                + "{"
                + "\"id\": 3932,"
                + "\"type\": \"name\","
                + "\"value\": {"
                + "\"givenName\": \"Alexander\","
                + "\"middleName\": \"\","
                + "\"familyName\": \"Solzhenitsyn\","
                + "\"prefix\": \"\","
                + "\"suffix\": \"\","
                + "\"givenNameSound\": \"\","
                + "\"familyNameSound\": \"\""
                + "},"
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2008-06-03T08:54:28Z\","
                + "\"created\": \"2008-06-03T08:54:28Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/Y7B2YG3W4RG6HESVPWQZXSH6JE/contact/1705/name/3932\""
                + "},"
                + "{ "
                + "\"id\": 3809, "
                + "\"type\": \"address\","
                + "\"value\": {"
                + "\"street\": \"3016 S Campbell Str\","
                + "\"city\": \"Las Vegas\","
                + "\"stateOrProvince\": \"NV\","
                + "\"postalCode\": \"85717\""
                + "},"
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": ["
                + "\"HOME\""
                + "],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:02:18Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1609/address/3708\"},"
                +"{" 
                + "\"id\": 3658,"
                + "\"type\": \"birthday\","
                + "\"value\": {"
                + " \"day\": \"5\","
                + "\"month\": \"11\","
                + "\"year\": \"2007\""
                + "},"
                + "\"editedBy\": \"OWNER\","
                + "\"flags\": [],"
                + "\"categories\": [],"
                + "\"updated\": \"2009-04-11T22:01:52Z\","
                + "\"created\": \"2008-06-03T08:54:22Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/ABC83783423948JHDJS/contact/1591/birthday/3658\""
                + "}"
                + "],"
                + "\"categories\": [],"
                + "\"error\": 0,"
                + "\"restoredId\": 0,"
                + "\"created\": \"2008-06-03T08:54:28Z\","
                + "\"updated\": \"2008-06-03T08:54:28Z\","
                + "\"uri\": \"http://social.yahooapis.com/v1/user/Y7B2YG3W4RG6HESVPWQZXSH6JE/contact/1705\""
                + "}";
        contactObject = parser.parse(sampleValue).getAsJsonObject();
        contact = YahooContactsUtil.parseYContact(contactObject, null);
        assertNotNull("should get a non-null parsed contact for JSON with a name", contact);
        assertNotNull("parsed contact should have fields map", contact.getFields());
        assertFalse("parsed contact should have non-empty fields map", contact.getFields().isEmpty());
        assertNotNull("parsed contact should have first name", contact.getFields().get(A_firstName));
        assertNotNull("parsed contact should have last name", contact.getFields().get(A_lastName));
        assertNotNull("parsed contact should have home street address", contact.getFields().get(A_homeStreet));
        assertNotNull("parsed contact should have home city", contact.getFields().get(A_homeCity));
        assertNotNull("parsed contact should have home state", contact.getFields().get(A_homeState));
        assertNotNull("parsed contact should have birthday field", contact.getFields().get(A_birthday));
        assertNull("parsed contact should NOT have anniversary field. Found: " + contact.getFields().get(A_anniversary), contact.getFields().get(A_anniversary));
    }
}
