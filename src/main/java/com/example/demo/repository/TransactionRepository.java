package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
	List<Transaction> findAll();
	Optional<Transaction> findById(Long id);
	Optional <Transaction> findByidAndUserId(Long id,Long userId);
	@Modifying
	@Transactional
	@Query("DELETE FROM Transaction t WHERE t.id = :id AND t.user.id = :userId")
	int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
