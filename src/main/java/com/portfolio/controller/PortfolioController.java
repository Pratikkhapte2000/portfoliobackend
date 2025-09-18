package com.portfolio.controller;

import com.portfolio.dto.AddAssetRequest;
import com.portfolio.dto.DiversificationScoreDto;
import com.portfolio.dto.PortfolioDto;
import com.portfolio.security.JwtUtil;
import com.portfolio.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@CrossOrigin(origins = "*")
public class PortfolioController {
    
    @Autowired
    private PortfolioService portfolioService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
    
    @PostMapping
    public ResponseEntity<?> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request, 
                                           HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            PortfolioDto portfolio = portfolioService.createPortfolio(
                    userId, 
                    request.getName(), 
                    request.getDescription()
            );
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getUserPortfolios(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<PortfolioDto> portfolios = portfolioService.getUserPortfolios(userId);
            return ResponseEntity.ok(portfolios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/{portfolioId}")
    public ResponseEntity<?> getPortfolio(@PathVariable Long portfolioId, 
                                        HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            PortfolioDto portfolio = portfolioService.getPortfolio(portfolioId, userId);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/{portfolioId}/assets")
    public ResponseEntity<?> addAsset(@PathVariable Long portfolioId,
                                    @Valid @RequestBody AddAssetRequest request,
                                    HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            PortfolioDto portfolio = portfolioService.addAsset(portfolioId, userId, request);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{portfolioId}/assets/{tickerSymbol}")
    public ResponseEntity<?> removeAsset(@PathVariable Long portfolioId,
                                       @PathVariable String tickerSymbol,
                                       HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            portfolioService.removeAsset(portfolioId, userId, tickerSymbol);
            return ResponseEntity.ok("Asset removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/{portfolioId}/diversification-score")
    public ResponseEntity<?> getDiversificationScore(@PathVariable Long portfolioId,
                                                   HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            DiversificationScoreDto score = portfolioService.calculateDiversificationScore(portfolioId, userId);
            return ResponseEntity.ok(score);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    public static class CreatePortfolioRequest {
        private String name;
        private String description;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
