package com.codesquad.blackjack.domain;

import com.codesquad.blackjack.domain.user.User;

public class Rule {
    public static final int BLACKJACK_NUMBER = 21;

    public static boolean isTie(User dealer, User player) {
        return dealer.getTotal() == player.getTotal();
    }

    public static boolean isBurstUser(User user) {
        return user.getTotal() > BLACKJACK_NUMBER;
    }

    public static boolean isBlackjackUser(User user) {
        return user.getTotal() == BLACKJACK_NUMBER;
    }
}
