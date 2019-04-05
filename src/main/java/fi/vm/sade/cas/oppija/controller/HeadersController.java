package fi.vm.sade.cas.oppija.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@RestController
@RequestMapping("/headers")
public class HeadersController {

    @GetMapping
    public Map<String, Object> getHeaders(@RequestHeader HttpHeaders headers) {
        return headers.entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
