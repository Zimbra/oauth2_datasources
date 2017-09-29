package com.synacor.zimbra.ys.contacts;

import static com.zimbra.common.mailbox.ContactConstants.A_email;
import static com.zimbra.common.mailbox.ContactConstants.A_email2;
import static com.zimbra.common.mailbox.ContactConstants.A_firstName;
import static com.zimbra.common.mailbox.ContactConstants.A_fullName;
import static com.zimbra.common.mailbox.ContactConstants.A_imAddress1;
import static com.zimbra.common.mailbox.ContactConstants.A_lastName;
import static com.zimbra.common.mailbox.ContactConstants.A_nickname;
import static com.zimbra.common.mailbox.ContactConstants.A_workEmail1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.DataSource.DataImport;
import com.zimbra.cs.account.Provisioning;
public class YahooContactsImport implements DataImport {
    private static String DEFAULT_CONTACTS_URL = "https://social.yahooapis.com/v1/user/%s/contactsformat=%s&count=%d";

    private DataSource mDataSource;
    public YahooContactsImport(DataSource ds) {
        mDataSource = ds;
    }

    @Override
    public void test() throws ServiceException {
        String YSocialURLPattern = LC.get("yahoo_social_contacts_url_pattern");
        if(YSocialURLPattern == null || YSocialURLPattern.isEmpty()) {
            YSocialURLPattern = DEFAULT_CONTACTS_URL;
        }
        Pair<String, String> tokenAndGuid = getAccessTokenAndGuid();
        HttpGet get = new HttpGet(String.format(YSocialURLPattern, tokenAndGuid.getSecond(), "json", 2));
        String authorizationHeader = String.format("Bearer %s", tokenAndGuid.getFirst());
        get.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
        HttpClient client = ZimbraHttpClientManager.getInstance().getExternalHttpClient();
        JsonArray jsonContacts = null;
        HttpResponse response = null;
        try {
            response = client.execute(get);
            try(JsonReader reader = new JsonReader(new InputStreamReader(response.getEntity().getContent()))) {
                JsonParser parser = new JsonParser();
                JsonElement jsonResponse = parser.parse(reader);
                if(jsonResponse != null && jsonResponse.isJsonObject()) {
                    JsonObject jsonObj = jsonResponse.getAsJsonObject();
                    if(jsonObj.has("contacts")) {
                        if(jsonObj.has("contact") && jsonObj.get("contact").isJsonArray()) {
                            jsonContacts = jsonObj.get("contact").getAsJsonArray();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw ServiceException.FAILURE("Failed to fetch contacts from  Yahoo Contacts API for testing", e);
        }
        if(jsonContacts == null || jsonContacts.size() == 0) {
            int respCode = 0;
            String respLine = "";
            if(response != null) {
                respCode = response.getStatusLine().getStatusCode();
                respLine = response.getStatusLine().getReasonPhrase();
            }
            throw ServiceException.FAILURE(String.format("Failed to fetch contacts from  Yahoo Contacts API for testing. Response status code %d. Respose status line: %s", respCode, respLine), null);
        }
    }

    private Pair<String, String> getAccessTokenAndGuid() throws ServiceException {
        String clientId = LC.get("yahoo_oauth2_client_id");
        if(clientId == null || clientId.isEmpty()) {
            throw ServiceException.FAILURE("yahoo_oauth2_client_id is not set in local config. Cannot access Yahoo API without a valid yahoo_oauth2_client_id.", null);
        }
        String clientSecret = LC.get("yahoo_oauth2_client_secret");
        if(clientSecret == null || clientSecret.isEmpty()) {
            throw ServiceException.FAILURE("yahoo_oauth2_client_secret is not set in local config. Cannot access Yahoo API without a valid yahoo_oauth2_client_secret.", null);
        }
        if(mDataSource == null) {
            throw ServiceException.FAILURE("DataSource object is NULL", null);
        }
        String refreshToken = mDataSource.getOauthRefreshToken();
        if(refreshToken == null || refreshToken.isEmpty()) {
            throw ServiceException.FAILURE(String.format("Refresh token is not set for DataSource %s of Account %s. Cannot access Yahoo API without a valid refresh token.", mDataSource.getName(), mDataSource.getAccountId()), null);
        }
        String tokenUrl = mDataSource.getOauthRefreshTokenUrl();
        if(tokenUrl == null || tokenUrl.isEmpty()) {
            throw ServiceException.FAILURE(String.format("Refresh token URL is not set for DataSource %s of Account %s. Cannot access Yahoo API without a valid refresh token URL.", mDataSource.getName(), mDataSource.getAccountId()), null);
        }
        String accessToken = null;
        String YGuid = null;
        HttpPost post = new HttpPost(mDataSource.getOauthRefreshTokenUrl());
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
                        ZimbraLog.extensions.debug("found access_token %s", accessToken);
                    } else if(name.equalsIgnoreCase("xoauth_yahoo_guid")) {
                        YGuid = parser.nextString();
                        ZimbraLog.extensions.debug("found xoauth_yahoo_guid %s", YGuid);
                    } else if(name.equalsIgnoreCase("refresh_token")) {
                        refreshToken = parser.nextString();
                        ZimbraLog.extensions.debug("found refresh_token %s", refreshToken);
                    } else {
                        parser.skipValue();
                    }
                    if(YGuid != null && accessToken != null && refreshToken != null) {
                        if(!refreshToken.equalsIgnoreCase(tokenUrl)) {
                            Map<String, Object> attrs = mDataSource.getAttrs(false);
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
    public void importData(List<Integer> folderIds, boolean fullSync) throws ServiceException {
        Pair<String, String> tokenAndGuid = getAccessTokenAndGuid();

    }

    private static String getFileAs(Map<String, String> fields) {
        if (!fields.containsKey(A_firstName) &&
            !fields.containsKey(A_lastName)) {
            String fileAs;
            if ((fileAs = fields.get(A_fullName)) != null ||
                (fileAs = fields.get(A_nickname)) != null ||
                (fileAs = fields.get(A_email)) != null ||
                (fileAs = fields.get(A_email2)) != null ||
                (fileAs = fields.get(A_workEmail1)) != null ||
                (fileAs = fields.get(A_imAddress1)) != null) {
                return fileAs;
            }
        }
        return null;
    }
}
