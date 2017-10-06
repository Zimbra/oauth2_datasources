package com.synacor.zimbra.ys.contacts;

import static com.zimbra.common.mailbox.ContactConstants.A_company;
import static com.zimbra.common.mailbox.ContactConstants.A_homeURL;
import static com.zimbra.common.mailbox.ContactConstants.A_imAddress1;
import static com.zimbra.common.mailbox.ContactConstants.A_jobTitle;
import static com.zimbra.common.mailbox.ContactConstants.A_nickname;
import static com.zimbra.common.mailbox.ContactConstants.A_notes;
import static com.zimbra.common.mailbox.ContactConstants.A_otherCustom1;
import static com.zimbra.common.mailbox.ContactConstants.A_otherURL;
import static com.zimbra.common.mailbox.ContactConstants.A_workURL;
import static com.zimbra.common.mailbox.ContactConstants.A_anniversary;
import static com.zimbra.common.mailbox.ContactConstants.A_birthday;
import static com.zimbra.common.mailbox.ContactConstants.A_email;
import static com.zimbra.common.mailbox.ContactConstants.A_firstName;
import static com.zimbra.common.mailbox.ContactConstants.A_homeCity;
import static com.zimbra.common.mailbox.ContactConstants.A_homeCountry;
import static com.zimbra.common.mailbox.ContactConstants.A_homePhone;
import static com.zimbra.common.mailbox.ContactConstants.A_homePhone2;
import static com.zimbra.common.mailbox.ContactConstants.A_homePostalCode;
import static com.zimbra.common.mailbox.ContactConstants.A_homeState;
import static com.zimbra.common.mailbox.ContactConstants.A_homeStreet;
import static com.zimbra.common.mailbox.ContactConstants.A_lastName;
import static com.zimbra.common.mailbox.ContactConstants.A_middleName;
import static com.zimbra.common.mailbox.ContactConstants.A_mobilePhone;
import static com.zimbra.common.mailbox.ContactConstants.A_namePrefix;
import static com.zimbra.common.mailbox.ContactConstants.A_nameSuffix;
import static com.zimbra.common.mailbox.ContactConstants.A_otherCity;
import static com.zimbra.common.mailbox.ContactConstants.A_otherCountry;
import static com.zimbra.common.mailbox.ContactConstants.A_otherPhone;
import static com.zimbra.common.mailbox.ContactConstants.A_otherPostalCode;
import static com.zimbra.common.mailbox.ContactConstants.A_otherState;
import static com.zimbra.common.mailbox.ContactConstants.A_otherStreet;
import static com.zimbra.common.mailbox.ContactConstants.A_pager;
import static com.zimbra.common.mailbox.ContactConstants.A_workCity;
import static com.zimbra.common.mailbox.ContactConstants.A_workCountry;
import static com.zimbra.common.mailbox.ContactConstants.A_workEmail1;
import static com.zimbra.common.mailbox.ContactConstants.A_workFax;
import static com.zimbra.common.mailbox.ContactConstants.A_workPhone;
import static com.zimbra.common.mailbox.ContactConstants.A_workPhone2;
import static com.zimbra.common.mailbox.ContactConstants.A_workPostalCode;
import static com.zimbra.common.mailbox.ContactConstants.A_workState;
import static com.zimbra.common.mailbox.ContactConstants.A_workStreet;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.ParsedContact;
/**
 * @author Greg Solovyev
 *
 */
@SuppressWarnings("serial")
public class YahooContactsUtil {
    static enum YContactFieldType {
        guid, nickname, email, yahooid, otherid, phone, jobTitle, company, notes, link, custom, name, address, birthday, anniversary
    }

    //parts of contact JSON object
    public static final String VALUE = "value";
    public static final String TYPE = "type";
    public static final String FLAGS = "flags";
    public static final String FIELDS = "fields";

    //yahoo name field value parts
    public static final String GIVENNAME = "givenName";
    public static final String MIDDLE = "middleName";
    public static final String FAMILYNAME = "familyName";
    public static final String PREFIX = "prefix";
    public static final String SUFFIX = "suffix";

    public static final Map<String, String> NAME_FIELDS_MAP = new HashMap<String, String>() {
        {
               put(A_firstName, GIVENNAME);
               put(A_middleName, MIDDLE);
               put(A_lastName, FAMILYNAME);
               put(A_namePrefix, PREFIX);
               put(A_nameSuffix, SUFFIX);
        }
    };

    //yahoo address field value parts
    public static final String STREET = "street";
    public static final String CITY = "city";
    public static final String STATE = "stateOrProvince";
    public static final String POSTALCODE = "postalCode";
    public static final String COUNTRY = "country";
    public static final String COUNTRYCODE = "countryCode";
    public static final Map<String, List<String>> WORK_ADDRESS_FIELDS_MAP = new HashMap<String, List<String>>() {
        {
               put(A_workStreet, Arrays.asList(STREET));
               put(A_workCity, Arrays.asList(CITY));
               put(A_workState, Arrays.asList(STATE));
               put(A_workPostalCode, Arrays.asList(POSTALCODE));
               put(A_workCountry, Arrays.asList(COUNTRY, COUNTRYCODE));
        }
    };
    public static final Map<String, List<String>> HOME_ADDRESS_FIELDS_MAP = new HashMap<String, List<String>>() {
        {
               put(A_homeStreet, Arrays.asList(STREET));
               put(A_homeCity, Arrays.asList(CITY));
               put(A_homeState, Arrays.asList(STATE));
               put(A_homePostalCode, Arrays.asList(POSTALCODE));
               put(A_homeCountry, Arrays.asList(COUNTRY, COUNTRYCODE));
        }
    };
    public static final Map<String, List<String>> OTHER_ADDRESS_FIELDS_MAP = new HashMap<String, List<String>>() {
        {
               put(A_otherStreet, Arrays.asList(STREET));
               put(A_otherCity, Arrays.asList(CITY));
               put(A_otherState, Arrays.asList(STATE));
               put(A_otherPostalCode, Arrays.asList(POSTALCODE));
               put(A_otherCountry, Arrays.asList(COUNTRY, COUNTRYCODE));
        }
    };
    //yahoo date field value parts
    public static final String DAY = "day";
    public static final String MONTH = "month";
    public static final String YEAR = "year";
    public static final Map<String, String> DATE_FIELDS_MAP = new HashMap<String, String>() {
        {
            put("birthday", A_birthday);
            put("anniversary", A_anniversary);
        }
    };
    public static final Map<String, String> PHONE_FIELDS_MAP = new HashMap<String, String>() {
        {
            put("personal", A_homePhone);
            put("work", A_workPhone);
            put("mobile", A_mobilePhone);
            put("other", A_otherPhone);
            put("pager", A_pager);
            put("fax", A_workFax);
            put("yahoophone", A_homePhone2);
            put("external", A_workPhone2);
        }
    };
    public static final Map<String, String> EMAIL_FIELDS_MAP = new HashMap<String, String>() {
        {
            put("personal", A_email);
            put("home", A_email);
            put("work", A_workEmail1);
        }
    };
    public static final Map<String, String> LINK_FIELDS_MAP = new HashMap<String, String>() {
        {
            put("personal", A_homeURL);
            put("home", A_homeURL);
            put("work", A_workURL);
            put("other", A_otherURL);
        }
    };
    public static void parseSimpleField(JsonObject fieldObject, String key, Map<String, String> fields) {
        if(fieldObject.has(VALUE)) {
            String value = fieldObject.get(VALUE).getAsString();
            if(value != null && !value.isEmpty()) {
                fields.put(key, value);
            }
        }
    }

    public static void parseFlaggedField(JsonObject fieldObject, String defaultFieldName, Map<String, String> flagMap, Map<String, String> fields) {
        if(fieldObject.has(VALUE)) {
            String zContactFieldName = null;
            if(fieldObject.has(FLAGS)) {
                JsonElement flagsElement = fieldObject.get(FLAGS);
                if(flagsElement.isJsonArray()) {
                    JsonArray flagsArray = flagsElement.getAsJsonArray();
                    if(flagsArray.size() > 0) {
                        String fieldFlag = flagsArray.get(0).getAsString().toLowerCase();
                        zContactFieldName = flagMap.get(fieldFlag);
                    }
                }
            }
            
            if(zContactFieldName == null) {
                zContactFieldName = defaultFieldName;
            }
            JsonElement valueElement = fieldObject.get(VALUE);
            if(valueElement != null) {
                int i = 1;
                String tmpName = zContactFieldName.replace("1", "");
                while(fields.containsKey(zContactFieldName)) {
                    i++;
                    zContactFieldName = String.format("%s%d", tmpName, i);
                }
                fields.put(zContactFieldName, valueElement.getAsString());
            }
        }
    }

    public static void parseDateField(JsonObject fieldObject, Locale locale, Map<String, String> fields) {
        String zContactFieldName = null;
        if(fieldObject.has(VALUE)) {
            zContactFieldName = DATE_FIELDS_MAP.get(fieldObject.get(TYPE).getAsString().toLowerCase());
            if(zContactFieldName != null) {
                JsonElement valueElement = fieldObject.get(VALUE);
                if(valueElement.isJsonObject()) {
                    JsonObject valueObject = valueElement.getAsJsonObject();
                    Integer year = null;
                    Integer month = null;
                    Integer day = null;
                    if(valueObject.has(YEAR)) {
                        year = valueObject.get(YEAR).getAsInt();
                    }
                    if(valueObject.has(MONTH)) {
                        month = valueObject.get(MONTH).getAsInt();
                    }
                    if(valueObject.has(DAY)) {
                        day = valueObject.get(DAY).getAsInt();
                    }
                    if(day == null || month == null) {
                        return;
                    }

                    Calendar cal = Calendar.getInstance(locale);
                    if(year != null) {
                        cal.set(Calendar.YEAR, year);
                    }
                    if(month != null) {
                        cal.set(Calendar.MONTH, month-1);
                    }
                    if(day != null) {
                        cal.set(Calendar.DAY_OF_MONTH, day);
                    }
                    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                    String dateString = df.format(cal.getTime());
                    fields.put(zContactFieldName, dateString);
                }
            }
        } 
    }

    public static void parseNameField(JsonObject fieldObject, Map<String, String> fields) {
        if(fieldObject.has(VALUE)) {
            JsonElement valueElement = fieldObject.get(VALUE);
            if(valueElement.isJsonObject()) {
                JsonObject valueObject = valueElement.getAsJsonObject();
                for (String key : NAME_FIELDS_MAP.keySet()) {
                    parseValuePart(valueObject, NAME_FIELDS_MAP.get(key), key, fields);
                }
            }
        }
    }

    public static void parseAddressField(JsonObject fieldObject, Map<String, String> fields) {
        String addressFlag = null;
        if(fieldObject.has(FLAGS)) {
            JsonElement flagsElement = fieldObject.get(FLAGS);
            if(flagsElement.isJsonArray()) {
                JsonArray flagsArray = flagsElement.getAsJsonArray();
                if(flagsArray.size() > 0) {
                    addressFlag = flagsArray.get(0).getAsString().toLowerCase();
                }
            }
        }
        if(fieldObject.has(VALUE)){
            JsonElement valueElement = fieldObject.get(VALUE);
            if(valueElement.isJsonObject()) {
                JsonObject valueObject = valueElement.getAsJsonObject();
                Map <String, List<String>> targetMap = HOME_ADDRESS_FIELDS_MAP;
                if(addressFlag != null) {
                    if(addressFlag.equalsIgnoreCase("work")) {
                        targetMap = WORK_ADDRESS_FIELDS_MAP;
                    } else if(addressFlag.equalsIgnoreCase("home")) {
                        targetMap = HOME_ADDRESS_FIELDS_MAP;
                    } else {
                        targetMap = OTHER_ADDRESS_FIELDS_MAP;
                    }
                }
                for (String key : targetMap.keySet()) {
                    parseValuePart(valueObject, targetMap.get(key), key, fields);
                }
            }
        }
    }

    public static void parseValuePart(JsonObject valueObject, String partName, String fieldName, Map<String, String> fields) {
        if(valueObject.has(partName)) {
            JsonElement tmp = valueObject.get(partName);
            if(tmp != null) {
                String szValue = tmp.getAsString();
                if(szValue != null && !szValue.isEmpty()) {
                    fields.put(fieldName, szValue);
                }
            }
        }
    }

    public static void parseValuePart(JsonObject valueObject, List<String> partNames, String fieldName, Map<String, String> fields) {
        for(String partName : partNames) {
            if(valueObject.has(partName)) {
                JsonElement tmp = valueObject.get(partName);
                if(tmp != null) {
                    String szValue = tmp.getAsString();
                    if(szValue != null && !szValue.isEmpty()) {
                        fields.put(fieldName, szValue);
                        break;
                    }
                }
            }    
        }
    }

    public static ParsedContact parseYContact(JsonObject jsonContact, DataSource ds) throws ServiceException {
        Map<String, String> contactFields = new HashMap<String, String>();
        if(jsonContact.has(FIELDS)) {
            JsonElement tmp = jsonContact.get(FIELDS);
            if(tmp.isJsonArray()) {
                JsonArray jsonFields = tmp.getAsJsonArray();
                Iterator<JsonElement> iter = jsonFields.iterator();
                while(iter.hasNext()) {
                    JsonElement next = iter.next();
                    if(next.isJsonObject()) {
                        JsonObject fieldObj = next.getAsJsonObject();
                        if(fieldObj.has(TYPE) && fieldObj.has(VALUE)) {
                            YahooContactsUtil.YContactFieldType type = YahooContactsUtil.YContactFieldType.valueOf(fieldObj.get(TYPE).getAsString().toLowerCase());
                            switch (type) {
                            case email:
                                parseFlaggedField(fieldObj, A_email, EMAIL_FIELDS_MAP, contactFields);
                                break;
                            case phone:
                                parseFlaggedField(fieldObj, A_homePhone, PHONE_FIELDS_MAP,  contactFields);
                                break;
                            case address:
                                parseAddressField(fieldObj, contactFields);
                                break;
                            case birthday:
                            case anniversary:
                                Locale locale = null;
                                if(ds != null) {
                                    locale = ds.getAccount().getLocale();
                                }
                                if(locale == null) {
                                    try {
                                        locale = Provisioning.getInstance().getConfig().getLocale();
                                    } catch (Exception e) {
                                        ZimbraLog.extensions.warn("Failed to get locale while parsing a contact");
                                    }
                                }
                                if(locale == null) {
                                    locale = Locale.US;
                                }
                                parseDateField(fieldObj, locale, contactFields);
                                break;
                            case name:
                                parseNameField(fieldObj, contactFields);
                                break;
                            case company:
                                parseSimpleField(fieldObj, A_company, contactFields);
                                break;
                            case notes:
                                parseSimpleField(fieldObj, A_notes, contactFields);
                                break;
                            case nickname:
                                parseSimpleField(fieldObj, A_nickname, contactFields);
                                break;
                            case jobTitle:
                                parseSimpleField(fieldObj, A_jobTitle, contactFields);
                                break;
                            case link:
                                parseFlaggedField(fieldObj, A_homeURL, LINK_FIELDS_MAP, contactFields);
                                break;
                            case yahooid:
                                parseSimpleField(fieldObj, A_imAddress1, contactFields);
                                break;
                            case otherid:
                            case custom:
                                default:
                                parseSimpleField(fieldObj, A_otherCustom1, contactFields);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(!contactFields.isEmpty()) {
            return new ParsedContact(contactFields);
        } else {
            return null;
        }
    }
}
