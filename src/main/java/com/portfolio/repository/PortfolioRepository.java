package com.portfolio.repository;

import com.portfolio.entity.Portfolio;
import com.portfolio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUser(User user);
    List<Portfolio> findByUserId(Long userId);
    boolean existsByIdAndUserId(Long portfolioId, Long userId);
}
