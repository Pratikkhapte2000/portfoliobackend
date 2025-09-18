package com.portfolio.service;

import com.portfolio.dto.*;
import com.portfolio.entity.Portfolio;
import com.portfolio.entity.PortfolioAsset;
import com.portfolio.entity.User;
import com.portfolio.repository.PortfolioAssetRepository;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioService {
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private PortfolioAssetRepository portfolioAssetRepository;
    
    @Autowired
    private StockDataService stockDataService;
    
    public PortfolioDto createPortfolio(Long userId, String name, String description) {
        User user = new User();
        user.setId(userId);
        
        Portfolio portfolio = new Portfolio(name, description, user);
        portfolio = portfolioRepository.save(portfolio);
        
        return convertToDto(portfolio);
    }
    
    public List<PortfolioDto> getUserPortfolios(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        return portfolios.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PortfolioDto getPortfolio(Long portfolioId, Long userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return convertToDto(portfolio);
    }
    
    public PortfolioDto addAsset(Long portfolioId, Long userId, AddAssetRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Check if asset already exists
        if (portfolioAssetRepository.existsByPortfolioAndTickerSymbol(portfolio, request.getTickerSymbol())) {
            throw new RuntimeException("Asset already exists in portfolio");
        }
        
        // Get current stock data
        StockDataDto stockData = stockDataService.getStockData(request.getTickerSymbol());
        
        PortfolioAsset asset = new PortfolioAsset(
                request.getTickerSymbol(),
                stockData.getName(),
                request.getQuantity(),
                request.getPurchasePrice(),
                portfolio
        );
        
        asset.setCurrentPrice(stockData.getPrice());
        portfolioAssetRepository.save(asset);
        
        return convertToDto(portfolio);
    }
    
    public void removeAsset(Long portfolioId, Long userId, String tickerSymbol) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        PortfolioAsset asset = portfolioAssetRepository.findByPortfolioAndTickerSymbol(portfolio, tickerSymbol)
                .orElseThrow(() -> new RuntimeException("Asset not found in portfolio"));
        
        portfolioAssetRepository.delete(asset);
    }
    
    public void updateAssetPrices(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        List<PortfolioAsset> assets = portfolio.getAssets();
        for (PortfolioAsset asset : assets) {
            StockDataDto stockData = stockDataService.getStockData(asset.getTickerSymbol());
            asset.setCurrentPrice(stockData.getPrice());
            portfolioAssetRepository.save(asset);
        }
    }
    
    public DiversificationScoreDto calculateDiversificationScore(Long portfolioId, Long userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Update prices before calculation
        updateAssetPrices(portfolioId);
        
        List<PortfolioAsset> assets = portfolio.getAssets();
        if (assets.isEmpty()) {
            return new DiversificationScoreDto(
                    BigDecimal.ZERO, 
                    "No Assets", 
                    "Portfolio has no assets to analyze"
            );
        }
        
        // Calculate diversification score based on:
        // 1. Number of different assets
        // 2. Distribution of portfolio value across assets
        // 3. Industry/sector diversity (simplified)
        
        int assetCount = assets.size();
        BigDecimal totalValue = assets.stream()
                .map(PortfolioAsset::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate concentration risk (Herfindahl-Hirschman Index)
        BigDecimal hhi = assets.stream()
                .map(asset -> {
                    BigDecimal weight = asset.getTotalValue().divide(totalValue, 4, RoundingMode.HALF_UP);
                    return weight.multiply(weight);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Convert HHI to diversification score (0-100)
        // HHI ranges from 1/n to 1, where n is number of assets
        BigDecimal maxHHI = BigDecimal.ONE.divide(new BigDecimal(assetCount), 4, RoundingMode.HALF_UP);
        BigDecimal minHHI = BigDecimal.ONE;
        
        BigDecimal diversificationScore = minHHI.subtract(hhi)
                .divide(minHHI.subtract(maxHHI), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        
        // Ensure score is between 0 and 100
        diversificationScore = diversificationScore.max(BigDecimal.ZERO).min(new BigDecimal("100"));
        
        String rating;
        String description;
        
        if (diversificationScore.compareTo(new BigDecimal("80")) >= 0) {
            rating = "Excellent";
            description = "Your portfolio is well-diversified with good distribution across assets.";
        } else if (diversificationScore.compareTo(new BigDecimal("60")) >= 0) {
            rating = "Good";
            description = "Your portfolio shows good diversification, but could be improved.";
        } else if (diversificationScore.compareTo(new BigDecimal("40")) >= 0) {
            rating = "Fair";
            description = "Your portfolio has moderate diversification. Consider adding more assets.";
        } else {
            rating = "Poor";
            description = "Your portfolio lacks diversification. Consider spreading investments across more assets.";
        }
        
        DiversificationScoreDto scoreDto = new DiversificationScoreDto(diversificationScore, rating, description);
        
        // Add recommendations
        if (assetCount < 5) {
            scoreDto.getRecommendations().add("Consider adding more assets to improve diversification");
        }
        if (hhi.compareTo(new BigDecimal("0.3")) > 0) {
            scoreDto.getRecommendations().add("Reduce concentration in individual assets");
        }
        if (assetCount >= 10) {
            scoreDto.getStrengths().add("Good number of assets for diversification");
        }
        
        return scoreDto;
    }
    
    private PortfolioDto convertToDto(Portfolio portfolio) {
        PortfolioDto dto = new PortfolioDto();
        dto.setId(portfolio.getId());
        dto.setName(portfolio.getName());
        dto.setDescription(portfolio.getDescription());
        dto.setCreatedAt(portfolio.getCreatedAt());
        dto.setUpdatedAt(portfolio.getUpdatedAt());
        dto.setUserId(portfolio.getUser().getId());
        
        // Convert assets
        List<PortfolioAssetDto> assetDtos = portfolio.getAssets() != null ? 
                portfolio.getAssets().stream()
                        .map(this::convertAssetToDto)
                        .collect(Collectors.toList()) : 
                new ArrayList<>();
        dto.setAssets(assetDtos);
        
        // Calculate totals
        BigDecimal totalValue = assetDtos.stream()
                .map(PortfolioAssetDto::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalValue(totalValue);
        
        BigDecimal totalCost = assetDtos.stream()
                .map(PortfolioAssetDto::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalCost(totalCost);
        
        BigDecimal totalGainLoss = totalValue.subtract(totalCost);
        dto.setTotalGainLoss(totalGainLoss);
        
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalGainLossPercentage = totalGainLoss.divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            dto.setTotalGainLossPercentage(totalGainLossPercentage);
        } else {
            dto.setTotalGainLossPercentage(BigDecimal.ZERO);
        }
        
        return dto;
    }
    
    private PortfolioAssetDto convertAssetToDto(PortfolioAsset asset) {
        PortfolioAssetDto dto = new PortfolioAssetDto();
        dto.setId(asset.getId());
        dto.setTickerSymbol(asset.getTickerSymbol());
        dto.setAssetName(asset.getAssetName());
        dto.setQuantity(asset.getQuantity());
        dto.setPurchasePrice(asset.getPurchasePrice());
        dto.setCurrentPrice(asset.getCurrentPrice());
        dto.setAddedAt(asset.getAddedAt());
        dto.setUpdatedAt(asset.getUpdatedAt());
        
        // Calculate derived values
        dto.setTotalValue(asset.getTotalValue());
        dto.setTotalCost(asset.getTotalCost());
        dto.setGainLoss(asset.getGainLoss());
        dto.setGainLossPercentage(asset.getGainLossPercentage());
        
        return dto;
    }
}
