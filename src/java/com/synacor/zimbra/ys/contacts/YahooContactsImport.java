package com.synacor.zimbra.ys.contacts;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.zimbra.common.httpclient.ZimbraHttpClientManager;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.DataSource.DataImport;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mime.ParsedContact;
import com.zimbra.cs.service.mail.CreateContact;
import com.zimbra.cs.service.util.ItemId;
/**
 * @author Greg Solovyev
 *
 */
public class YahooContactsImport implements DataImport {
    private static String DEFAULT_CONTACTS_TEST_URL = "https://social.yahooapis.com/v1/user/%s/contacts?format=%s&count=%d";
    private static String DEFAULT_CONTACTS_SYNC_URL = "https://social.yahooapis.com/v1/user/%s/contacts?format=%s&view=sync&rev=%d";
    private static String DEFAULT_TOKEN_URL = "https://api.login.yahoo.com/oauth2/get_token";

    private DataSource mDataSource;
    public YahooContactsImport(DataSource ds) {
        mDataSource = ds;
    }

    @Override
    public void test() throws ServiceException {
        String YSocialURLPattern = LC.get("zm_oauth_yahoo_contacts_test_uri_template");
        if(YSocialURLPattern == null || YSocialURLPattern.isEmpty()) {
            YSocialURLPattern = DEFAULT_CONTACTS_TEST_URL;
        }
        Pair<String, String> tokenAndGuid = getAccessTokenAndGuid();
        HttpGet get = new HttpGet(String.format(YSocialURLPattern, tokenAndGuid.getSecond(), "json", 10));
        String authorizationHeader = String.format("Bearer %s", tokenAndGuid.getFirst());
        get.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        HttpClient client = ZimbraHttpClientManager.getInstance().getExternalHttpClient();
        JsonArray jsonContacts = null;
        HttpResponse response = null;
        String respContent = "";
        ParsedContact testContact = null;
        try {
            response = client.execute(get);
            respContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JsonParser parser = new JsonParser();
            JsonElement jsonResponse = parser.parse(respContent);
            if(jsonResponse != null && jsonResponse.isJsonObject()) {
                JsonObject jsonObj = jsonResponse.getAsJsonObject();
                if(jsonObj.has("contacts") && jsonObj.get("contacts").isJsonObject()) {
                    JsonObject contactsObject = jsonObj.get("contacts").getAsJsonObject();
                    if(contactsObject.has("contact") && contactsObject.get("contact").isJsonArray()) {
                        jsonContacts = contactsObject.get("contact").getAsJsonArray();
                        for(JsonElement contactElement : jsonContacts) {
                            ParsedContact contact = YahooContactsUtil.parseYContact(contactElement.getAsJsonObject(), mDataSource);
                            if(contact != null) {
                                testContact = contact;
                                break;
                            }
                        }
                    } else {
                        ZimbraLog.extensions.debug("Did not find 'contact' element in 'contacts' object");
                    }
                } else {
                    ZimbraLog.extensions.debug("Did not find 'contacts' element in JSON response object");
                }
            } else {
                ZimbraLog.extensions.debug("Did not find JSON response object");
            }
        } catch (UnsupportedOperationException | IOException e) {
            throw ServiceException.FAILURE("Data source test failed. Failed to fetch contacts from  Yahoo Contacts API for testing", e);
        }
        if(testContact == null) {
            int respCode = 0;
            String respLine = "";
            if(response != null) {
                respCode = response.getStatusLine().getStatusCode();
                respLine = response.getStatusLine().getReasonPhrase();
            }
            throw ServiceException.FAILURE(String.format("Data source test failed. Failed to fetch contacts from  Yahoo Contacts API for testing. Response status code %d. Response status line: %s. Response body %s", respCode, respLine, respContent), null);
        }

    }

    private Pair<String, String> getAccessTokenAndGuid() throws ServiceException {
        String clientId = LC.get("zm_oauth_yahoo_client_id");
        if(clientId == null || clientId.isEmpty()) {
            throw ServiceException.FAILURE("yahoo_oauth2_client_id is not set in local config. Cannot access Yahoo API without a valid yahoo_oauth2_client_id.", null);
        }
        String clientSecret = LC.get("zm_oauth_yahoo_client_secret");
        if(clientSecret == null || clientSecret.isEmpty()) {
            throw ServiceException.FAILURE("yahoo_oauth2_client_secret is not set in local config. Cannot access Yahoo API without a valid yahoo_oauth2_client_secret.", null);
        }
        String tokenUrl = LC.get("zm_oauth_yahoo_authenticate_uri");
        if(tokenUrl == null || tokenUrl.isEmpty()) {
            tokenUrl = DEFAULT_TOKEN_URL;
        }
        if(mDataSource == null) {
            throw ServiceException.FAILURE("DataSource object is NULL", null);
        }
        String refreshToken = mDataSource.getOauthRefreshToken();
        if(refreshToken == null || refreshToken.isEmpty()) {
            throw ServiceException.FAILURE(String.format("Refresh token is not set for DataSource %s of Account %s. Cannot access Yahoo API without a valid refresh token.", mDataSource.getName(), mDataSource.getAccountId()), null);
        }
        String accessToken = null;
        String YGuid = null;
        HttpPost post = new HttpPost(tokenUrl);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("refresh_token", refreshToken));
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("client_secret", clientSecret));
        params.add(new BasicNameValuePair("redirect_uri", "oob"));

        try {
            String authorizationHeader = String.format("Basic %s", new String(Base64.encodeBase64(String.format("%s:%s",clientId, clientSecret).getBytes("UTF-8"))));
            post.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
            post.addHeader(HttpHeaders.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpClient client = ZimbraHttpClientManager.getInstance().getExternalHttpClient();
            HttpResponse response = client.execute(post);
            accessToken = null;
            YGuid = null;
            try(JsonReader parser = new JsonReader(new InputStreamReader(response.getEntity().getContent()))) {
                parser.beginObject();
                while(parser.hasNext()) {
                    String name = parser.nextName();
                    if(name.equalsIgnoreCase("access_token")) {
                        accessToken = parser.nextString();
                        ZimbraLog.extensions.trace("found access_token %s", accessToken);
                    } else if(name.equalsIgnoreCase("xoauth_yahoo_guid")) {
                        YGuid = parser.nextString();
                        ZimbraLog.extensions.trace("found xoauth_yahoo_guid %s", YGuid);
                    } else if(name.equalsIgnoreCase("refresh_token")) {
                        refreshToken = parser.nextString();
                        ZimbraLog.extensions.trace("found refresh_token %s", refreshToken);
                    } else {
                        parser.skipValue();
                    }
                    if(YGuid != null && accessToken != null && refreshToken != null) {
                        if(!refreshToken.equalsIgnoreCase(refreshToken)) {
                            Map<String, Object> attrs = new HashMap<String, Object>();
                            attrs.put(Provisioning.A_zimbraDataSourceOAuthRefreshToken, refreshToken);
                            Provisioning.getInstance().modifyDataSource(mDataSource.getAccount(), mDataSource.getId(), attrs);
                        }
                        break;
                    }
                }
                parser.endObject();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("Failed to get access token and GUID.", e);
        } finally {
            post.releaseConnection();
        }
        if(accessToken == null || accessToken.isEmpty()) {
            throw ServiceException.FAILURE("Failed to get access token. No exception was raised.", null);
        }
        if(YGuid == null || YGuid.isEmpty()) {
            throw ServiceException.FAILURE("Failed to get user GUID. No exception was raised.", null);
        }
        return new Pair<String, String>(accessToken, YGuid);
    }

    @Override
    public void importData(List<Integer> folderIds /* This argument is always passed as NULL. Likely needs to be deprecated upstream. */, boolean fullSync) throws ServiceException {
        String YSocialURLPattern = LC.get("zm_oauth_yahoo_contacts_uri_template");
        if(YSocialURLPattern == null || YSocialURLPattern.isEmpty()) {
            YSocialURLPattern = DEFAULT_CONTACTS_SYNC_URL;
        }
        Pair<String, String> tokenAndGuid = getAccessTokenAndGuid();
        String[] attrs = mDataSource.getMultiAttr(Provisioning.A_zimbraDataSourceAttribute);
        int rev = 0;
        if(attrs.length > 0) {
            String val = attrs[0];
            try {
                Integer revision = Integer.parseInt(val);
                if(revision != null) {
                    rev = revision.intValue();
                }
            } catch (NumberFormatException e) {
                throw ServiceException.FAILURE(String.format("Invalid value in zimbraDataSourceAttribute: %s", val), e);
            }
        }
        String url = String.format(YSocialURLPattern, tokenAndGuid.getSecond(), "json", rev);
        HttpGet get = new HttpGet(url);
        String authorizationHeader = String.format("Bearer %s", tokenAndGuid.getFirst());
        get.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        HttpClient client = ZimbraHttpClientManager.getInstance().getExternalHttpClient();
        JsonArray jsonContacts = null;
        HttpResponse response = null;
        String respContent = "";
        //log this only at the most verbose level, because this contains privileged information
        ZimbraLog.extensions.trace("Attempting to sync Yahoo contacts. URL: %s. authorizationHeader: %", url, authorizationHeader);
        try {
            response = client.execute(get);
            respContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            //log this only at the most verbose level, because this contains privileged information
            ZimbraLog.extensions.trace("contacts sync response from Yahoo %s", respContent);
            JsonParser parser = new JsonParser();
            JsonElement jsonResponse = parser.parse(respContent);
            if(jsonResponse != null && jsonResponse.isJsonObject()) {
                JsonObject jsonObj = jsonResponse.getAsJsonObject();
                if(jsonObj.has("contactsync") && jsonObj.get("contactsync").isJsonObject()) {
                    JsonObject contactsObject = jsonObj.get("contactsync").getAsJsonObject();
                    if(contactsObject.has("contacts") && contactsObject.get("contacts").isJsonArray()) {
                        jsonContacts = contactsObject.get("contacts").getAsJsonArray();
                        List<ParsedContact> clist = new ArrayList<ParsedContact>();
                        for(JsonElement contactElement : jsonContacts) {
                            if(contactElement.isJsonObject() && contactElement.getAsJsonObject().has("op")) {
                                String op = contactElement.getAsJsonObject().get("op").getAsString();
                                if("add".equalsIgnoreCase(op)) {
                                    ParsedContact contact = YahooContactsUtil.parseYContact(contactElement.getAsJsonObject(), mDataSource);
                                    if(contact != null) {
                                        clist.add(contact);
                                    }
                                }
                            }
                        }
                        ItemId iidFolder = new ItemId(mDataSource.getMailbox(), mDataSource.getFolderId());
                        if(!clist.isEmpty()) {
                            CreateContact.createContacts(null, mDataSource.getMailbox(), iidFolder, clist, null);
                        }
                    } else {
                        ZimbraLog.extensions.debug("Did not find 'contacts' element in 'contactsync' object");
                    }
                    if(contactsObject.has("rev")) {
                        rev = contactsObject.get("rev").getAsInt();
                        Map<String, Object> dsAttrs = new HashMap<String, Object>();
                        dsAttrs.put(Provisioning.A_zimbraDataSourceAttribute, rev);
                        Provisioning.getInstance().modifyDataSource(mDataSource.getAccount(), mDataSource.getId(), dsAttrs);
                    } else {
                        ZimbraLog.extensions.debug("Did not find 'rev' element in 'contactsync' object. Response body: %s", respContent);
                    }
                } else {
                    ZimbraLog.extensions.debug("Did not find 'contactsync' element in JSON response object. Response body: %s", respContent);
                }
            } else {
                ZimbraLog.extensions.debug("Did not find JSON response object. Response body: %s", respContent);
            }
        } catch (UnsupportedOperationException | IOException e) {
            throw ServiceException.FAILURE(String.format("Data source test failed. Failed to fetch contacts from  Yahoo Contacts API for testing. Response body: %s", respContent), e);
        }
    }
}
