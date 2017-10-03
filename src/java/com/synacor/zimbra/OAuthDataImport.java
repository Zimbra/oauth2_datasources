package com.synacor.zimbra;
import com.synacor.zimbra.ys.contacts.YahooContactsImport;
import com.zimbra.cs.account.DataSource;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource.DataImport;
/**
 * @author Greg Solovyev
 * DataImport class that picks source-specific implementation based on oauth2 URL. 
 * This class will only be aware of implementations packaged together with this class.
 */
public class OAuthDataImport implements DataImport {
    private DataImport implementation;
    public OAuthDataImport(DataSource ds) {
        String sourceURL = ds.getOauthRefreshTokenUrl();
        if(sourceURL.indexOf("api.login.yahoo.com/oauth2/get_token") > -1) {
            implementation = new YahooContactsImport(ds);
        } else {
            throw new UnsupportedOperationException(String.format("No known DataImport implementation for oauth URL %", sourceURL));
        }
    }

    @Override
    public void test() throws ServiceException {
        implementation.test();

    }

    @Override
    public void importData(List<Integer> folderIds, boolean fullSync) throws ServiceException {
        implementation.importData(folderIds, fullSync);
    }
}
