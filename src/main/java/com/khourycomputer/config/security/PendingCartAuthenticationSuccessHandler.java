package com.khourycomputer.config.security;

import com.khourycomputer.application.dto.cart.AddCartItemRequest;
import com.khourycomputer.application.dto.user.UserResponse;
import com.khourycomputer.application.service.CartApplicationService;
import com.khourycomputer.application.service.UserApplicationService;
import com.khourycomputer.web.viewmodel.cart.PendingCartAction;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PendingCartAuthenticationSuccessHandler
        extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String SUCCESS_MESSAGE_SESSION_ATTRIBUTE =
            "pendingCartSuccessMessage";

    public static final String ERROR_MESSAGE_SESSION_ATTRIBUTE =
            "pendingCartErrorMessage";

    private final CartApplicationService cartApplicationService;
    private final UserApplicationService userApplicationService;

    public PendingCartAuthenticationSuccessHandler(
            CartApplicationService cartApplicationService,
            UserApplicationService userApplicationService
    ) {
        this.cartApplicationService = cartApplicationService;
        this.userApplicationService = userApplicationService;

        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            super.onAuthenticationSuccess(
                    request,
                    response,
                    authentication
            );

            return;
        }

        Object sessionValue = session.getAttribute(
                PendingCartAction.SESSION_ATTRIBUTE
        );

        if (!(sessionValue instanceof PendingCartAction pendingAction)) {
            super.onAuthenticationSuccess(
                    request,
                    response,
                    authentication
            );

            return;
        }

        // Remove it before processing so it cannot be repeated accidentally.
        session.removeAttribute(
                PendingCartAction.SESSION_ATTRIBUTE
        );

        UserResponse currentUser =
                userApplicationService.getUserByEmail(
                        authentication.getName()
                );

        try {
            cartApplicationService.addItemToCart(
                    currentUser.id(),
                    new AddCartItemRequest(
                            pendingAction.productId(),
                            pendingAction.quantity()
                    )
            );

            session.setAttribute(
                    SUCCESS_MESSAGE_SESSION_ATTRIBUTE,
                    "Product added to cart successfully."
            );

        } catch (IllegalArgumentException exception) {
            session.setAttribute(
                    ERROR_MESSAGE_SESSION_ATTRIBUTE,
                    exception.getMessage()
            );
        }

        getRedirectStrategy().sendRedirect(
                request,
                response,
                "/cart"
        );
    }
}