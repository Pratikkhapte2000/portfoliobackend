package com.portfolio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class AddAssetRequest {
    
    @NotBlank
    private String tickerSymbol;
    
    @NotBlank
    private String assetName;
    
    @NotNull
    @Positive
    private BigDecimal quantity;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal purchasePrice;
    
    public AddAssetRequest() {}
    
    public AddAssetRequest(String tickerSymbol, String assetName, BigDecimal quantity, BigDecimal purchasePrice) {
        this.tickerSymbol = tickerSymbol;
        this.assetName = assetName;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }
    
    public String getTickerSymbol() {
        return tickerSymbol;
    }
    
    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }
    
    public String getAssetName() {
        return assetName;
    }
    
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}
