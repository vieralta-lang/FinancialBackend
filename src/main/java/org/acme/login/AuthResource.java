package org.acme.login;

import java.util.Map;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Tag(name = "Authentication", description = "Login and registration")
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @Operation(summary = "Login", description = "Authenticate with username and password, returns a JWT token")
    @APIResponse(responseCode = "200", description = "Login successful")
    @APIResponse(responseCode = "401", description = "Invalid credentials")
    public Response login(LoginRequest request) {
        String token = authService.authenticate(request.username, request.password);
        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid username or password"))
                    .build();
        }
        return Response.ok(Map.of("token", token)).build();
    }

    @POST
    @Path("/register")
    @Operation(summary = "Register", description = "Create a new user account")
    @APIResponse(responseCode = "201", description = "User registered")
    @APIResponse(responseCode = "409", description = "Username already exists")
    public Response register(RegisterRequest request) {
        try {
            var user = authService.register(request.name, request.username, request.email, request.password);
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("id", user.id, "username", user.username, "name", user.name))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class RegisterRequest {
        public String name;
        public String username;
        public String email;
        public String password;
    }

    public static class ChangePasswordRequest {
        public String currentPassword;
        public String newPassword;
    }

    @PUT
    @Path("/change-password")
    @RolesAllowed("user")
    @Operation(summary = "Change password", description = "Change the password of the authenticated user")
    @APIResponse(responseCode = "200", description = "Password changed successfully")
    @APIResponse(responseCode = "400", description = "Current password is incorrect")
    public Response changePassword(ChangePasswordRequest request) {
        try {
            String username = jwt.getName();
            authService.changePassword(username, request.currentPassword, request.newPassword);
            return Response.ok(Map.of("message", "Password changed successfully")).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
