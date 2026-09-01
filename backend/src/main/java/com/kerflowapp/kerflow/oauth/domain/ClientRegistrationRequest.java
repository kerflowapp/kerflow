package com.kerflowapp.kerflow.oauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * RFC 7591 client metadata. Unknown members are ignored on purpose: clients send plenty of
 * optional metadata (logo_uri, contacts, software_id...) that we have no use for.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientRegistrationRequest(
    @JsonProperty("client_name") String clientName,
    @JsonProperty("redirect_uris") List<String> redirectUris,
    @JsonProperty("grant_types") List<String> grantTypes,
    @JsonProperty("response_types") List<String> responseTypes,
    @JsonProperty("token_endpoint_auth_method") String tokenEndpointAuthMethod,
    @JsonProperty("scope") String scope
) {
}
