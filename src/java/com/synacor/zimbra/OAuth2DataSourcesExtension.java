package com.synacor.zimbra;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;

public class OAuth2DataSourcesExtension implements ZimbraExtension {

    @Override
    public String getName() {
        return "ContactsImport";
    }

    @Override
    public void init() throws ExtensionException, ServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
