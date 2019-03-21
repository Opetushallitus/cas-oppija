package fi.vm.sade.cas.oppija.surrogate;

import java.security.GeneralSecurityException;
import java.util.function.Function;

public interface SurrogateService {

    String getAuthorizeUrl(SurrogateSession session, Function<String, String> tokenToRedirectUrl);

    SurrogateAuthenticationDto getAuthentication(String token, String code) throws GeneralSecurityException;

}