package com.example.demo.controller;
import com.example.demo.model.Transaction;
import com.example.demo.model.UserPrincipal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Users;
import com.example.demo.service.TransactionService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.dto.TransactionDTO;
import com.example.demo.dto.UserDTO;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;



@RestController
public class TestController extends BaseController{
	@Autowired
	private UserService service;
	@Autowired
	private TransactionService transactionService;
@GetMapping("/")
public String getMethodName() {
    return "welcome home";
}
@PostMapping("/register")
public Users postMethodName(@RequestBody Users user) {
    //TODO: process POST request
    service.registerUser(user);
    return user;
}

@PostMapping("/login")
public Map<String,String> login(@RequestBody Users user) {
	String token =  service.verify(user);
	Map<String, String> response  = new HashMap<>();
	if (token != null) {
        response.put("token", token); // ✅ Return JSON instead of plain text
    } else {
        response.put("error", "Invalid credentials");
    }
	return response;
	
}
@PostMapping(value = "/addNew")
public ResponseEntity<?> addTransaction(@RequestBody @Valid TransactionDTO transaction) {  
	Users currentUser = getCurrentUser();
    
    // Associate the transaction with the user
    Map<String, String> response = new HashMap<>();
	transactionService.addNewTransaction(transaction,currentUser);
	response.put("message", "Value added successfully");
	return ResponseEntity.ok().body(response);

}
//
@GetMapping(value = "/getAll")
public ResponseEntity<List<TransactionDTO>> getAllTransactions(@RequestHeader("Authorization") String authorization) {
	System.out.println("Authorization header: " + authorization);
	List<TransactionDTO> transactions =  transactionService.getAllTransactions();
	return ResponseEntity.ok(transactions.isEmpty()?new ArrayList<>():transactions);
}

//Delete function 
@DeleteMapping(value = "/delete/{id}")
public ResponseEntity<?> deleteTransaction(@PathVariable long id){
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();  
	Users currentUser =getCurrentUser();
	
	String responseMessage = transactionService.deleteTransaction(id, currentUser.getId());
	Map<String, String> response = new HashMap<>();
    response.put("message", responseMessage);

    // Check if the deletion was successful or if there's an error message
    if (responseMessage.contains("not found")) {
        // If the transaction was not found, return a NOT_FOUND status (404)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // If successful, return a 200 OK with success message
    return ResponseEntity.ok(response);
}

//update the entity
@PutMapping(value = "update/{id}")
public ResponseEntity<?> updateEntity(@PathVariable Long id, @RequestBody @Valid TransactionDTO dto) {
//	need current user then update the entity
	try {
		Transaction targeTransaction =  transactionService.updateEntity(id,getCurrentUser().getId(),dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(targeTransaction);
	} catch (Exception e) {
		// TODO: handle exception
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}
	
}

}
