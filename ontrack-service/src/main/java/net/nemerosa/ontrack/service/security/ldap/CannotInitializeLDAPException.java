package net.nemerosa.ontrack.service.security.ldap;

import net.nemerosa.ontrack.common.BaseException;

@Deprecated
public class CannotInitializeLDAPException extends BaseException {
    public CannotInitializeLDAPException(Exception e) {
        super("Cannot initialise connection to LDAP: %s", e);
    }
}
