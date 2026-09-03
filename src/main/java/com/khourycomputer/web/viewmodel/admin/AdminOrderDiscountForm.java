package com.khourycomputer.web.viewmodel.admin;

import java.math.BigDecimal;

public class AdminOrderDiscountForm {

    private BigDecimal agreedFinalTotal;

    public AdminOrderDiscountForm() {
    }

    public AdminOrderDiscountForm(
            BigDecimal agreedFinalTotal) {

        this.agreedFinalTotal = agreedFinalTotal;
    }

    public BigDecimal getAgreedFinalTotal() {
        return agreedFinalTotal;
    }

    public void setAgreedFinalTotal(
            BigDecimal agreedFinalTotal) {

        this.agreedFinalTotal = agreedFinalTotal;
    }
}