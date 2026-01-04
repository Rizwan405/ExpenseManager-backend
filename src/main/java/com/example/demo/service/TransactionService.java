package com.example.demo.service;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.dto.TransactionDTO;
import com.example.demo.model.Users;
import com.example.demo.repository.TransactionRepository;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.Transaction;
@Service
public class TransactionService {
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
    private EntityManager entityManager;
	public Long addNewTransaction(TransactionDTO transactionDTO, Users currentUser) {
	    Transaction transaction = new Transaction();
	    transaction.setTitle(transactionDTO.getTitle());
	    transaction.setCategory(transactionDTO.getCategory());
	    transaction.setAmount(transactionDTO.getAmount());
	    transaction.setPaymentType(transactionDTO.getPaymentType());
	    transaction.setTransactionType(transactionDTO.getTransactionType());
	    transaction.setDate(transactionDTO.getDate());

	    // Associate transaction with user
	    transaction.setUser(currentUser);

	    transactionRepository.save(transaction);
		return transaction.getId();
	}
	public List<TransactionDTO> getAllTransactions() {
		return transactionRepository.findAll().stream().map(trans -> {
			UserDTO userDTO = new UserDTO();
			userDTO.setUsername(trans.getUser().getUsername());
			
			userDTO.setId(trans.getUser().getId());
			TransactionDTO resDto = new TransactionDTO();
			
			resDto.setId(trans.getId());
			resDto.setTitle(trans.getTitle());
			resDto.setAmount(trans.getAmount());
			resDto.setCategory(trans.getCategory());
			resDto.setPaymentType(trans.getPaymentType());
			resDto.setDate(trans.getDate());
			resDto.setTransactionType(trans.getTransactionType());
			resDto.setUser(userDTO);
			return resDto;
		}).collect(Collectors.toList());
	}
	@Transactional
	public String deleteTransaction(Long id,Long userId) {
        int affectedRows = transactionRepository.deleteByIdAndUserId(id, userId);

        if (affectedRows == 0) {
            // No matching transaction found, return an error message
            return "Transaction with id " + id + " not found for user with id " + userId;
        }

        // Successful deletion
        return "Transaction successfully deleted";
    }
//		logic for updating the entity 
	
	public Transaction updateEntity(Long id,Long userId,TransactionDTO dto) {
//		first need to find the entity that matches userid and id 
		Transaction targeTransaction = transactionRepository.findByidAndUserId(id, userId).orElseThrow(()-> new ResourceNotFoundException("Target entity not found"));
//		doing the functions and then save 
		
		targeTransaction.setTitle(dto.getTitle());
		targeTransaction.setPaymentType(dto.getPaymentType());
		targeTransaction.setAmount(dto.getAmount());
		targeTransaction.setTransactionType(dto.getTransactionType());
		targeTransaction.setDate(dto.getDate());
		targeTransaction.setCategory(dto.getCategory());
		transactionRepository.save(targeTransaction);
		return targeTransaction;
		
		
	}

		
	}


