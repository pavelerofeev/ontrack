package net.nemerosa.ontrack.ui.controller;

import net.nemerosa.ontrack.common.RunProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component
@Profile({RunProfile.UNIT_TEST})
public class MockURIBuilder implements URIBuilder {
    @Override
    public URI build(Object invocationInfo) {
        MvcUriComponentsBuilder.MethodInvocationInfo info = (MvcUriComponentsBuilder.MethodInvocationInfo) invocationInfo;
        return URI.create(
                format(
                        "urn:test:%s#%s:%s",
                        info.getControllerMethod().getDeclaringClass().getName(),
                        info.getControllerMethod().getName(),
                        Arrays.asList(info.getArgumentValues()).stream()
                                .map(o -> Objects.toString(o, ""))
                                .map(this::encode)
                                .collect(Collectors.joining(","))
                )
        );
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot encode URI parameter", e);
        }
    }
}
