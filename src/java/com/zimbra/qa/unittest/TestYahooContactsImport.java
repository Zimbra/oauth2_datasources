package com.zimbra.qa.unittest;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.zimbra.client.ZFolder;
import com.zimbra.client.ZMailbox;
import com.zimbra.client.ZOAuthDataSource;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
/**
 * @author Greg Solovyev
 *
 */
public class TestYahooContactsImport {
    @Rule
    public TestName testInfo = new TestName();
    private String clientId = null;
    private String clientSecret = null;
    private String yahooTestAccount = null;
    private String testToken = null;
    private static String USER_NAME = null;
    private String testId;

    @Before
    public void setUp() throws Exception {
        clientId = LC.get("yahoo_oauth2_client_id");
        TestUtil.assumeTrue("yahoo_oauth2_client_id is not set. This LC setting is required to use YahooContactsImport.", clientId != null && !clientId.isEmpty());
        clientSecret = LC.get("yahoo_oauth2_client_secret");
        TestUtil.assumeTrue("yahoo_oauth2_client_secret is not set. This LC setting is required to use YahooContactsImport.", clientSecret != null && !clientSecret.isEmpty());
        yahooTestAccount = LC.get("yahoo_test_account");
        TestUtil.assumeTrue("yahoo_test_account is not set. This LC setting is required to run the SOAP tests for YahooContactsImport.", yahooTestAccount != null && !yahooTestAccount.isEmpty());
        testToken = LC.get("yahoo_test_refresh_token");
        TestUtil.assumeTrue("yahoo_test_refresh_token is not set. This LC setting is required to run the SOAP tests for YahooContactsImport.", testToken != null && !testToken.isEmpty());
        testId = String.format("%s-%s-%d", this.getClass().getSimpleName(), testInfo.getMethodName(), (int)Math.abs(Math.random()*100));
        USER_NAME = String.format("%s-user", testId).toLowerCase();
        cleanUp();
        TestUtil.createAccount(USER_NAME);
    }

    @After
    public void tearDown() throws Exception {
        cleanUp();
    }
    
    private void cleanUp() throws Exception {
        if (USER_NAME != null) {
            TestUtil.deleteAccountIfExists(USER_NAME);
        }
    }

    @Test
    public void test() throws ServiceException {
        ZMailbox zmbox = TestUtil.getZMailbox(USER_NAME);
        ZFolder folder = TestUtil.createFolder(zmbox, "/testCustomDS");
        ZOAuthDataSource zds = new ZOAuthDataSource(yahooTestAccount, true, testToken, "https://api.login.yahoo.com/oauth2/get_token", folder.getId(), "com.synacor.zimbra.OAuthDataImport", true);
        String dsId = zmbox.createDataSource(zds);
        zds.setId(dsId);
        String result = zmbox.testDataSource(zds);
        assertNull("test should return null on success. Returned: " + result, result);
    }

}
