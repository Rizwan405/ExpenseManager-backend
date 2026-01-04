package com.example.demo.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name="transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
//	title 
	@NotBlank(message = "cannot be blank")
	@Column(name = "title")
	private String title;
//	amount
	@NotNull(message = "amount cannot be null")
	@Positive(message = "value must be positive")
	@Column(nullable = false, name = "amount")
	private double amount;
	
	@Enumerated(EnumType.STRING)
	@NotNull(message = "transactiontype cannot be blank")
	@Column(name = "transaction_type")
	private TransactionType transactionType;
	
	@NotNull(message = "transactiontype cannot be blank")
	@Column(name ="date")
	private LocalDate date;
	
	@Enumerated(EnumType.STRING)
	@NotNull(message = "transactiontype cannot be blank")
	@Column(name = "transaction_method")
	private PaymentType paymentType;
	
	@Enumerated(EnumType.STRING)
	@NotNull(message = "transactiontype cannot be blank")
	@Column(name = "category")
	private CategoryType category;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id",nullable = false, referencedColumnName = "id")
	@JsonBackReference
	private Users user;
}
