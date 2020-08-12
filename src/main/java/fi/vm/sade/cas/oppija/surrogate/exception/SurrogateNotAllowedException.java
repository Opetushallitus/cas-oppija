package fi.vm.sade.cas.oppija.surrogate.exception;

import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;

public class SurrogateNotAllowedException extends UnauthorizedAuthenticationException {

    public SurrogateNotAllowedException(String msg) {
        super(msg);
    }

}
