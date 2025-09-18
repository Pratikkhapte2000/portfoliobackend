package com.portfolio.repository;

import com.portfolio.entity.Portfolio;
import com.portfolio.entity.PortfolioAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioAssetRepository extends JpaRepository<PortfolioAsset, Long> {
    List<PortfolioAsset> findByPortfolio(Portfolio portfolio);
    List<PortfolioAsset> findByPortfolioId(Long portfolioId);
    Optional<PortfolioAsset> findByPortfolioAndTickerSymbol(Portfolio portfolio, String tickerSymbol);
    boolean existsByPortfolioAndTickerSymbol(Portfolio portfolio, String tickerSymbol);
}
