package com.portfolio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_assets")
public class PortfolioAsset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "ticker_symbol", length = 10)
    private String tickerSymbol;
    
    @NotBlank
    @Column(name = "asset_name", length = 200)
    private String assetName;
    
    @NotNull
    @Positive
    @Column(name = "quantity", precision = 15, scale = 6)
    private BigDecimal quantity;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(name = "current_price", precision = 15, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    public PortfolioAsset() {
        this.addedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public PortfolioAsset(String tickerSymbol, String assetName, BigDecimal quantity, 
                         BigDecimal purchasePrice, Portfolio portfolio) {
        this();
        this.tickerSymbol = tickerSymbol;
        this.assetName = assetName;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.portfolio = portfolio;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public LocalDateTime getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Portfolio getPortfolio() {
        return portfolio;
    }
    
    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
    
    // Helper methods
    public BigDecimal getTotalValue() {
        if (currentPrice != null) {
            return currentPrice.multiply(quantity);
        }
        return purchasePrice.multiply(quantity);
    }
    
    public BigDecimal getTotalCost() {
        return purchasePrice.multiply(quantity);
    }
    
    public BigDecimal getGainLoss() {
        return getTotalValue().subtract(getTotalCost());
    }
    
    public BigDecimal getGainLossPercentage() {
        if (getTotalCost().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getGainLoss().divide(getTotalCost(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
