package com.test.backend.iam.interfaces.rest;

import com.test.backend.iam.domain.services.UserCommandService;
import com.test.backend.iam.domain.model.commands.UpdatePasswordCommand;
import com.test.backend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.test.backend.iam.interfaces.rest.resources.SignInResource;
import com.test.backend.iam.interfaces.rest.resources.SignUpResource;
import com.test.backend.iam.interfaces.rest.resources.UpdatePasswordResource;
import com.test.backend.iam.interfaces.rest.resources.UserResource;
import com.test.backend.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.test.backend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.test.backend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.test.backend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var authenticatedUser = userCommandService.handle(signInCommand);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(authenticatedUser.get());
        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResource);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordResource resource) {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var command = new UpdatePasswordCommand(email, resource.currentPassword(), resource.newPassword());
        var result = userCommandService.handle(command);
        if (!result) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}
