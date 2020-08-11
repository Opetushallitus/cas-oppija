package fi.vm.sade.cas.oppija.surrogate.exception;

import org.apereo.cas.services.UnauthorizedServiceException;

public class SurrogateNotAllowedException extends UnauthorizedServiceException {

    public SurrogateNotAllowedException(String msg) {
        super("403", msg);
    }

}
