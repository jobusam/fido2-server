package de.busam.fido2.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record User(@JsonProperty("id")int id,
                   @JsonProperty("name")String name,
                   @JsonProperty("email")String email,
                   @JsonProperty("password")String password,
                   @JsonProperty("roles")List<AppRole>roles,
                   @JsonProperty("userDetails")UserDetails userDetails) {
}

