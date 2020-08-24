package fi.vm.sade.cas.oppija.configuration;

import fi.vm.sade.cas.oppija.service.PersonService;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;
import org.apereo.cas.support.pac4j.config.support.authentication.Pac4jAuthenticationEventExecutionPlanConfiguration;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.logout.handler.LogoutHandler;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.function.Supplier;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.*;
import static fi.vm.sade.cas.oppija.CasOppijaUtils.resolveAttribute;

@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlClientConfiguration extends Pac4jAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private Clients builtClients;

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlClientConfiguration.class);

    @Autowired
    private PersonService personService;

    private final CasConfigurationProperties casProperties;

    public SamlClientConfiguration(CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    // override bean Pac4jAuthenticationEventExecutionPlanConfiguration#clientPrincipalFactory
    @Override
    public PrincipalFactory clientPrincipalFactory() {
        return new OidAttributePrincipalFactory(personService);
    }

    private static class OidAttributePrincipalFactory implements PrincipalFactory {

        private final PersonService personService;
        private final PrincipalFactory principalFactory;

        public OidAttributePrincipalFactory(PersonService personService) {
            this.personService = personService;
            this.principalFactory = PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Override
        public Principal createPrincipal(String id, Map<String, List<Object>> attributes) {
            try {
                resolveNationalIdentificationNumber(attributes)
                        .flatMap(this::findOidByNationalIdentificationNumber)
                        .ifPresent((String oid) -> attributes.put(ATTRIBUTE_NAME_PERSON_OID, Arrays.asList(oid)));
            } catch (Exception e) {
                LOGGER.error("Unable to get oid by national identification number", e);
            }

            return principalFactory.createPrincipal(id, attributes);
        }

        private Optional<String> resolveNationalIdentificationNumber(Map<String, List<Object>> attributes) {
            return resolveAttribute(attributes, ATTRIBUTE_NAME_NATIONAL_IDENTIFICATION_NUMBER, String.class);
        }

        private Optional<String> findOidByNationalIdentificationNumber(String nationalIdentificationNumber) {
            return personService.findOidByNationalIdentificationNumber(nationalIdentificationNumber);
        }

    }

    /* override bean Pac4jAuthenticationEventExecutionPlanConfiguration#clientAuthenticationHandler
    TODO ekä tätä ei enää tarvita? näyttää melkein samalta ja determinePrincipalIdFrom ei enää löydy.
    @Override
    public AuthenticationHandler clientAuthenticationHandler() {
        var pac4j = casProperties.getAuthn().getPac4j();
        var h = new ClientAuthenticationHandler(pac4j.getName(), servicesManager.getIfAvailable(),
                clientPrincipalFactory(personService), builtClients) {
            @Override
            protected String determinePrincipalIdFrom(UserProfile profile, BaseClient client) {
                String id = super.determinePrincipalIdFrom(profile, client);
                return profile.getClientName() + UserProfile.SEPARATOR + id;
            }
        };
        h.setTypedIdUsed(pac4j.isTypedIdUsed());
        h.setPrincipalAttributeId(pac4j.getPrincipalAttributeId());
        return h;
    } */

    @Bean
    public DelegatedClientFactory pac4jDelegatedClientFactory() {
        DelegatedClientFactory delegatedClientFactory = new DelegatedClientFactory(casProperties);
        delegatedClientFactory.build();
        Collection<IndirectClient> clients = delegatedClientFactory.build();

        Map<String, String> customProperties = casProperties.getCustom().getProperties();
        for(IndirectClient client : clients) {
            if (client instanceof SAML2Client && Objects.equals(customProperties.get("suomiFiClientName"), client.getName())) {
                SAML2Client saml2Client = (SAML2Client) client;
                SAML2Configuration configuration = saml2Client.getConfiguration();
                configuration.setSpLogoutRequestBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
                configuration.setSpLogoutResponseBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
                configuration.setLogoutHandler(new LogoutHandler() {});
                configuration.setAuthnRequestExtensions(createExtensions());
            }
        }
        return delegatedClientFactory;
    }

    private Supplier<List<XSAny>> createExtensions() {
        return () -> {
            String language = Optional.ofNullable(LocaleContextHolder.getLocale())
                    .map(Locale::getLanguage)
                    .filter(SUPPORTED_LANGUAGES::contains)
                    .orElse(DEFAULT_LANGUAGE);
            return List.of(createLanguageExtension(language));
        };
    }

    /**
     * <vetuma xmlns="urn:vetuma:SAML:2.0:extensions">
     *     <LG>sv</LG>
     * </vetuma>
     */
    private XSAny createLanguageExtension(String languageCode) {
        XSAny lg = new XSAnyBuilder().buildObject(new QName("LG"));
        lg.setTextContent(languageCode);
        XSAny vetuma = new XSAnyBuilder().buildObject(new QName("urn:vetuma:SAML:2.0:extensions", "vetuma"));
        vetuma.getUnknownXMLObjects().add(lg);
        return vetuma;
    }

}
