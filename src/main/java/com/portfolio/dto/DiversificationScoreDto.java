package com.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public class DiversificationScoreDto {
    
    private BigDecimal score;
    private String rating;
    private String description;
    private List<String> recommendations;
    private List<String> strengths;
    private List<String> weaknesses;
    
    public DiversificationScoreDto() {}
    
    public DiversificationScoreDto(BigDecimal score, String rating, String description) {
        this.score = score;
        this.rating = rating;
        this.description = description;
    }
    
    // Getters and Setters
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public String getRating() {
        return rating;
    }
    
    public void setRating(String rating) {
        this.rating = rating;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
    
    public List<String> getStrengths() {
        return strengths;
    }
    
    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }
    
    public List<String> getWeaknesses() {
        return weaknesses;
    }
    
    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses;
    }
}
