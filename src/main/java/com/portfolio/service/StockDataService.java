package com.portfolio.service;

import com.portfolio.dto.StockDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockDataService {
    
    @Value("${alpha-vantage.api-key}")
    private String apiKey;
    
    @Value("${alpha-vantage.base-url}")
    private String baseUrl;
    
    private final WebClient webClient;
    
    public StockDataService() {
        this.webClient = WebClient.builder().build();
    }
    
    public StockDataDto getStockData(String symbol) {
        try {
            // For demo purposes, we'll use mock data since Alpha Vantage requires API key
            // In production, you would make actual API calls here
            return getMockStockData(symbol);
        } catch (Exception e) {
            // Fallback to mock data if API fails
            return getMockStockData(symbol);
        }
    }
    
    private StockDataDto getMockStockData(String symbol) {
        // Mock data for demonstration
        Map<String, StockDataDto> mockData = new HashMap<>();
        
        mockData.put("AAPL", new StockDataDto("AAPL", "Apple Inc.", new BigDecimal("175.43")));
        mockData.put("GOOGL", new StockDataDto("GOOGL", "Alphabet Inc.", new BigDecimal("142.56")));
        mockData.put("MSFT", new StockDataDto("MSFT", "Microsoft Corporation", new BigDecimal("378.85")));
        mockData.put("TSLA", new StockDataDto("TSLA", "Tesla Inc.", new BigDecimal("248.50")));
        mockData.put("AMZN", new StockDataDto("AMZN", "Amazon.com Inc.", new BigDecimal("151.94")));
        mockData.put("NVDA", new StockDataDto("NVDA", "NVIDIA Corporation", new BigDecimal("875.28")));
        mockData.put("META", new StockDataDto("META", "Meta Platforms Inc.", new BigDecimal("485.20")));
        mockData.put("NFLX", new StockDataDto("NFLX", "Netflix Inc.", new BigDecimal("485.20")));
        mockData.put("AMD", new StockDataDto("AMD", "Advanced Micro Devices Inc.", new BigDecimal("128.45")));
        mockData.put("INTC", new StockDataDto("INTC", "Intel Corporation", new BigDecimal("43.21")));
        
        StockDataDto stockData = mockData.get(symbol.toUpperCase());
        if (stockData == null) {
            // Generate random price for unknown symbols
            double randomPrice = 50 + Math.random() * 500;
            stockData = new StockDataDto(symbol.toUpperCase(), symbol + " Corporation", 
                                       new BigDecimal(String.format("%.2f", randomPrice)));
        }
        
        // Add some random variation to make it look more realistic
        double variation = 0.95 + Math.random() * 0.1; // ±5% variation
        BigDecimal currentPrice = stockData.getPrice().multiply(new BigDecimal(variation));
        stockData.setPrice(currentPrice);
        
        // Add change and change percentage
        BigDecimal change = currentPrice.subtract(stockData.getPrice()).multiply(new BigDecimal("0.1"));
        stockData.setChange(change);
        stockData.setChangePercent(change.divide(stockData.getPrice(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100")));
        
        stockData.setLastUpdated(LocalDateTime.now());
        
        return stockData;
    }
    
    private Mono<StockDataDto> fetchFromAlphaVantage(String symbol) {
        return webClient.get()
                .uri(baseUrl + "?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apiKey}", symbol, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // Parse Alpha Vantage response and convert to StockDataDto
                    // This is a simplified version - actual implementation would be more robust
                    Map<String, Object> quote = (Map<String, Object>) response.get("Global Quote");
                    if (quote != null) {
                        String price = (String) quote.get("05. price");
                        return new StockDataDto(symbol, symbol, new BigDecimal(price));
                    }
                    return getMockStockData(symbol);
                })
                .onErrorReturn(getMockStockData(symbol));
    }
}
