package de.busam.fido2.model.user;

import io.javalin.core.security.Role;

public enum AppRole implements Role {
    ANYONE, USER, ADMIN
}