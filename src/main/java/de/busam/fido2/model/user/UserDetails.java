package de.busam.fido2.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDetails(@JsonProperty("dateOfBirth")String dateOfBirth,
                          @JsonProperty("address")String address) {
}
