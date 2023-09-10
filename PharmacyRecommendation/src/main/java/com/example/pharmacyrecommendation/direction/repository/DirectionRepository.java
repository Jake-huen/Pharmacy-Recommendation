package com.example.pharmacyrecommendation.direction.repository;

import com.example.pharmacyrecommendation.direction.entity.Direction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectionRepository extends JpaRepository<Direction, Long> {
}
