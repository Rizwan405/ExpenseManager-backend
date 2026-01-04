package com.example.demo.service;

import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.dto.TransactionDTO;
import com.example.demo.model.*;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepo;
import jakarta.persistence.EntityManager;
import org.h2.command.dml.MergeUsing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepo userRepo;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private Users testUser;
    private TransactionDTO testTransactionDTO;

    @BeforeEach
    void setUp() {
//        making test user
       this.testUser = Users.builder().id(1L).username("zaheer").password("Rizwan312cb@").build();
//        making transaction

        this.testTransaction = Transaction.builder()
                .id(10L)
                .title("Test Transaction")
                .amount(100.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .user(testUser)
                .build();

         this.testTransactionDTO = TransactionDTO.builder()
                 .id(10L)
                .title("Test Transaction")
                .amount(100.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .build();
    }
    @Test
    void addNewTransaction_WithValidData_ShouldSaveTransaction() {
        // Arrange - now non-null!
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        transactionService.addNewTransaction(testTransactionDTO, testUser);  // Pass ID?

        // Assert
        verify(transactionRepository, times(1)).save(any(Transaction.class));

    }

    @Test
    void addNewTransaction_ShouldSetUserCorrectly() {
        // Arrange
        Transaction capturedTransaction = new Transaction();
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    capturedTransaction.setTitle(invocation.getArgument(0, Transaction.class).getTitle());
                    capturedTransaction.setUser(invocation.getArgument(0, Transaction.class).getUser());
                    return capturedTransaction;
                });

        // Act
        transactionService.addNewTransaction(testTransactionDTO, testUser);

        // Assert
        assertNotNull(capturedTransaction.getUser());
        assertEquals(testUser.getId(), capturedTransaction.getUser().getId());
    }

    @Test
    void getAllTransactions_WhenTransactionsExist_ShouldReturnTransactionDTOs() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        // Act
        List<TransactionDTO> result = transactionService.getAllTransactions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Transaction", result.get(0).getTitle());
        assertEquals("zaheer", result.get(0).getUser().getUsername());
        verify(transactionRepository, times(1)).findAll();
    }
    @Test
    void getAllTransactions_WhenNoTransactions_ShouldReturnEmptyList() {
        // Arrange
        when(transactionRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<TransactionDTO> result = transactionService.getAllTransactions();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    @Test
    void deleteTransaction_WhenTransactionExists_ShouldReturnSuccessMessage() {
        // Arrange
        Long transactionId = 100L;
        Long userId = 1L;
        when(transactionRepository.deleteByIdAndUserId(eq(transactionId), eq(userId))).thenReturn(1);

        // Act
        String result = transactionService.deleteTransaction(transactionId, userId);

        // Assert
        assertEquals("Transaction successfully deleted", result);
        verify(transactionRepository, times(1)).deleteByIdAndUserId(transactionId, userId);
    }
    @Test
    void deleteTransaction_WhenTransactionNotFound_ShouldReturnErrorMessage() {
        // Arrange
        Long transactionId = 999L;
        Long userId = 1L;
        when(transactionRepository.deleteByIdAndUserId(eq(transactionId), eq(userId))).thenReturn(0);

        // Act
        String result = transactionService.deleteTransaction(transactionId, userId);

        // Assert
        assertEquals("Transaction with id 999 not found for user with id 1", result);
    }
    @Test
    void deleteTransaction_WithInvalidUserId_ShouldReturnErrorMessage() {
        // Arrange
        Long transactionId = 100L;
        Long userId = 999L;
        when(transactionRepository.deleteByIdAndUserId(eq(transactionId), eq(userId))).thenReturn(0);

        // Act
        String result = transactionService.deleteTransaction(transactionId, userId);

        // Assert
        assertEquals("Transaction with id 100 not found for user with id 999", result);
    }
    @Test
    void updateEntity_WhenTransactionExists_ShouldUpdateAndReturnTransaction() {
        // Arrange
        Long transactionId = 100L;
        Long userId = 1L;

        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setTitle("Updated Groceries");
        updateDTO.setAmount(200.00);
        updateDTO.setCategory(CategoryType.Health);
        updateDTO.setPaymentType(PaymentType.Card);
        updateDTO.setTransactionType(TransactionType.Expense);
        updateDTO.setDate(LocalDate.now().plusDays(1));

        when(transactionRepository.findByidAndUserId(eq(transactionId), eq(userId)))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        Transaction result = transactionService.updateEntity(transactionId, userId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Groceries", result.getTitle());
        assertEquals(200.00, result.getAmount());
        assertEquals(CategoryType.Health, result.getCategory());
        assertEquals(PaymentType.Card, result.getPaymentType());

        verify(transactionRepository, times(1)).findByidAndUserId(transactionId, userId);
        verify(transactionRepository, times(1)).save(testTransaction);
    }
    @Test
    void updateEntity_WhenTransactionNotFound_ShouldThrowException() {
        // Arrange
        Long transactionId = 999L;
        Long userId = 1L;

        when(transactionRepository.findByidAndUserId(eq(transactionId), eq(userId)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.updateEntity(transactionId, userId, testTransactionDTO);
        });

        verify(transactionRepository, times(1)).findByidAndUserId(transactionId, userId);
        verify(transactionRepository, never()).save(any());
    }


    @Test
    void updateEntity_ShouldUpdateAllFieldsCorrectly() {
        // Arrange
        Long transactionId = 100L;
        Long userId = 1L;

        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setTitle("New Title");
        updateDTO.setAmount(300.00);
        updateDTO.setCategory(CategoryType.Health);
        updateDTO.setPaymentType(PaymentType.Cash);
        updateDTO.setTransactionType(TransactionType.Expense);
        updateDTO.setDate(LocalDate.of(2024, 12, 25));

        Transaction existingTransaction = new Transaction();
        existingTransaction.setId(transactionId);
        existingTransaction.setUser(testUser);

        when(transactionRepository.findByidAndUserId(eq(transactionId), eq(userId)))
                .thenReturn(Optional.of(existingTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        // Act
        Transaction result = transactionService.updateEntity(transactionId, userId, updateDTO);

        // Assert
        assertEquals("New Title", result.getTitle());
        assertEquals(300.00, result.getAmount());
        assertEquals(CategoryType.Health, result.getCategory());
        assertEquals(PaymentType.Cash, result.getPaymentType());
        assertEquals(TransactionType.Expense, result.getTransactionType());
        assertEquals(LocalDate.of(2024, 12, 25), result.getDate());
    }

}
