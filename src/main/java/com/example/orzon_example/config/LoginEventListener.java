package com.example.orzon_example.config;

import com.example.orzon_example.event.EventService;
import com.example.orzon_example.event.UserEventType;
import com.example.orzon_example.user.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final EventService eventService;
    private final UserService userService;

    public LoginEventListener(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        userService.findByUsername(username)
                .ifPresent(user -> eventService.recordEvent(UserEventType.LOGIN, user, null, null));
    }
}
