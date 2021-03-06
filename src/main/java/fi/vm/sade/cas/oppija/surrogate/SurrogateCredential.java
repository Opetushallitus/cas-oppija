package fi.vm.sade.cas.oppija.surrogate;

import org.apereo.cas.authentication.Credential;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SurrogateCredential implements Credential {

    private final String token;
    private final String code;
    private Map<String, Object> authenticationAttributes;

    public SurrogateCredential(String token, String code) {
        this.token = requireNonNull(token);
        this.code = requireNonNull(code);
    }

    @Override
    public String getId() {
        return token;
    }

    public String getToken() {
        return token;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> getAuthenticationAttributes() {
        return authenticationAttributes;
    }

    public void setAuthenticationAttributes(Map<String, Object> authenticationAttributes) {
        this.authenticationAttributes = authenticationAttributes;
    }

}
