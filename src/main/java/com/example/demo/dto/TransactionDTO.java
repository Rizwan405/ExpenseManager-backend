package com.example.demo.dto;

import java.time.LocalDate;

import com.example.demo.model.CategoryType;
import com.example.demo.model.PaymentType;
import com.example.demo.model.TransactionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
	@Valid
	private long id;
	@NotBlank(message = "title could not blank")
	private String title;
	@NotNull(message = "amount cannot be null")
	@Positive(message = "number must be positive")
	private double amount;
	@NotNull(message = "transaction type cannot be null")
	private TransactionType transactionType;
	@NotNull(message = "date cannot be null")
	private LocalDate date;
	@NotNull(message = "payment type cannot null")
	private PaymentType paymentType;
	@NotNull(message = "category cannot be empty")
	private CategoryType category;
	private UserDTO user;
}
