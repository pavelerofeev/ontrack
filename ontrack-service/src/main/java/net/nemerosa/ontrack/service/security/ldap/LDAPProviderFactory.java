package net.nemerosa.ontrack.service.security.ldap;

import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

@Deprecated
public interface LDAPProviderFactory {

    LdapAuthenticationProvider getProvider();

}
