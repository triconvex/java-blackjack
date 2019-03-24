package com.codesquad.blackjack.domain;

import javax.persistence.*;
import java.util.Objects;

@Embeddable
public class Chip {
    public static final int ZERO = 0;
    private static final int TWO = 2;
    private static final double ONE_POINT_FIVE = 1.5;

    @Column
    private final int amount;

    //TODO : 질문하기
    public Chip() {
        this.amount = 0;
    }

    public Chip(int amount) {
        this.amount = amount;
    }

    public Chip sum(Chip target) {
        return new Chip(amount + target.amount);
    }

    public Chip subtract(int bettingChip) {
        return new Chip(amount - bettingChip);
    }

    public Chip blackjack() {
        return new Chip((int)(amount + (amount * ONE_POINT_FIVE)));
    }

    public Chip twice() {
        return new Chip(amount * TWO);
    }

    public boolean isOver(int bettingChip) {
        return this.amount >= bettingChip;
    }

    public boolean isZero() {
        return this.amount <= ZERO;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chip chip = (Chip) o;
        return amount == chip.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return "Chip{" +
                "amount=" + amount +
                '}';
    }
}
