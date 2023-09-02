package com.example.pharmacyrecommendation.pharmacy.repository;

import com.example.pharmacyrecommendation.pharmacy.entity.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
}
