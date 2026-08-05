package com.khourycomputer.web.viewmodel.cart;

import java.io.Serializable;

public record PendingCartAction(
        Long productId,
        int quantity
) implements Serializable {

    public static final String SESSION_ATTRIBUTE =
            "pendingCartAction";
}