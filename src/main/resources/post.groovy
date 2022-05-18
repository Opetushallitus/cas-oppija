import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration
import org.apereo.cas.web.support.WebUtils

def run(Object[] args) {
    def requestContext = args[0]
    def provider = (args[1] as Set<DelegatedClientIdentityProviderConfiguration>)[0]
    def logger = args[2]
    logger.info("Checking provider ${provider.name}...")
    def response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext)
    logger.debug("Redirecting to ${provider.redirectUrl}")
    response.sendRedirect(provider.redirectUrl);
}