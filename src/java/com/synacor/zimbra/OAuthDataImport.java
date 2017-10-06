package com.synacor.zimbra;
import com.synacor.zimbra.ys.contacts.YahooContactsImport;
import com.zimbra.cs.account.DataSource;

import java.util.List;

import com.zimbra.client.ZDataSource;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.DataSource.DataImport;
/**
 * @author Greg Solovyev
 * DataImport class that picks source-specific implementation based on oauth2 URL. 
 * This class will only be aware of implementations packaged together with this class.
 */
public class OAuthDataImport implements DataImport {
    private DataImport implementation;
    private String source = null;
    public OAuthDataImport(DataSource ds) {
        source = ds.getHost();
        if(source.equalsIgnoreCase(ZDataSource.SOURCE_HOST_YAHOO)) {
            implementation = new YahooContactsImport(ds);
        } else {
            ZimbraLog.extensions.error(new UnsupportedOperationException(String.format("No known DataImport implementation for %s", source)));
        }
    }

    @Override
    public void test() throws ServiceException {
        if(implementation == null) {
            throw new UnsupportedOperationException(String.format("No known DataImport implementation for %s", source));
        } else {
            implementation.test();
        }

    }

    @Override
    public void importData(List<Integer> folderIds, boolean fullSync) throws ServiceException {
        if(implementation == null) {
            throw new UnsupportedOperationException(String.format("No known DataImport implementation for %s", source));
        } else {
            implementation.importData(folderIds, fullSync);
        }
    }
}
