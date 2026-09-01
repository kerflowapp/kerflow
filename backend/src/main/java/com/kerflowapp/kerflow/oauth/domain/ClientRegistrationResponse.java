package com.kerflowapp.kerflow.oauth.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClientRegistrationResponse(
    @JsonProperty("client_id") String clientId,
    // Only present for confidential clients, and only in this response.
    @JsonProperty("client_secret") String clientSecret,
    @JsonProperty("client_id_issued_at") Long clientIdIssuedAt,
    @JsonProperty("client_name") String clientName,
    @JsonProperty("redirect_uris") List<String> redirectUris,
    @JsonProperty("grant_types") List<String> grantTypes,
    @JsonProperty("response_types") List<String> responseTypes,
    @JsonProperty("token_endpoint_auth_method") String tokenEndpointAuthMethod,
    @JsonProperty("scope") String scope
) {
}
