package com.synacor.zimbra;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.qa.unittest.TestYahooContactsImport;
import com.zimbra.qa.unittest.ZimbraSuite;

public class OAuth2DataSourcesExtension implements ZimbraExtension {

    @Override
    public String getName() {
        return "ContactsImport";
    }

    @Override
    public void init() throws ExtensionException, ServiceException {
        try {
            ZimbraSuite.addTest(TestYahooContactsImport.class);
        } catch (NoClassDefFoundError e) {
            // Expected in production, because JUnit is not available.
            ZimbraLog.test.debug("Unable to load TestYahooContactsImport unit tests.", e);
        }

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
