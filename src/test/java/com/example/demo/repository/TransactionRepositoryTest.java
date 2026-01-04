package com.example.demo.repository;

import com.example.demo.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class TransactionRepositoryTest {
    @Autowired
     private TransactionRepository transactionRepository;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private EntityManager entityManager;
//    test 01 that return all saved transactions
    private Users testUser;
    @BeforeEach
    void setUp() {
// we need to declare a user before each test
        this.testUser = userRepo.save(  // Assign to 'this.testUser'
                Users.builder()
                        .username("zaheer")
                        .password("Rizwan312cb@")
                        .build()
        );
}
    @Test
    void findAll_shouldReturnAllTransactions(){
        // Given
        Transaction transaction1 = Transaction.builder()
                .title("Test Transaction")
                .amount(100.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .build();
        Transaction transaction12 = Transaction.builder()
                .title("Test Transaction 2")
                .amount(10.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .build();
        transaction1.setUser(testUser);
        transaction12.setUser(testUser);

        transactionRepository.saveAll(List.of(transaction1,transaction12));

//      when
        List<Transaction> result = transactionRepository.findAll();

//        Then

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Transaction");
        assertThat(result).extracting(Transaction::getTitle).containsExactlyInAnyOrder("Test Transaction","Test Transaction 2");
    }

    @Test
    void findAll_shouldReturnEmptyList() {
        // When
        List<Transaction> result = transactionRepository.findAll();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnTransaction_whenExists() {
        // Given
        Transaction transaction = Transaction.builder()
                .title("Test")
                .amount(10.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .build();
        transaction.setUser(testUser);
        Transaction saved = transactionRepository.save(transaction);

        // When
        Optional<Transaction> result = transactionRepository.findById(saved.getId());

        // Then
        assertThat(result).isPresent();
        Assertions.assertEquals(result.get().getId(), saved.getId());
        assertThat(result.get().getTitle()).isEqualTo("Test");
    }
    @Test
    void findById_shouldReturnEmptyOptional_whenNotExists() {
        // When
        Optional<Transaction> result = transactionRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }
    @Test
    void findByIdAndUserId_shouldReturnTransaction_whenExistsAndOwnedByUser() {
        // Given
        Transaction transaction = Transaction.builder()
                .title("Test")
                .amount(10.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .build();
        transaction.setUser(testUser);
        Transaction saved = transactionRepository.save(transaction);

        // When
        Optional<Transaction> result = transactionRepository.findByidAndUserId(saved.getId(),testUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }
    @Test
    void findByIdAndUserId_shouldReturnEmptyOptional_whenTransactionNotExists() {
        // When
        Optional<Transaction> result = transactionRepository.findByidAndUserId(999L,testUser.getId());

        // Then
        assertThat(result).isEmpty();
    }
    @Test
    void findByIdAndUserId_shouldReturnEmptyOptional_whenUserNotOwner() {
        // Given
        Users otherUser = Users.builder().username("other").password("password").build();
        userRepo.save(otherUser);

        Transaction transaction = Transaction.builder()
                .title("Test")
                .amount(10.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .build();
        transaction.setUser(testUser);
        Transaction saved = transactionRepository.save(transaction);

        // When
        Optional<Transaction> result = transactionRepository.findByidAndUserId(saved.getId(),otherUser.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void deleteByIdAndUserId_shouldReturn0_whenTransactionNotExists() {
        // When
        int result = transactionRepository.deleteByIdAndUserId(999L, testUser.getId());

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void deleteByIdAndUserId_shouldReturn0_whenWrongUser() {
        // Given
        Users otherUser = Users.builder().username("other").password("password").build();
        userRepo.save(otherUser);

        Transaction transaction = Transaction.builder()
                .title("Test")
                .amount(10.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .build();
        transaction.setUser(otherUser);
        Transaction saved = transactionRepository.save(transaction);

        // When
        int result = transactionRepository.deleteByIdAndUserId(
                saved.getId(), testUser.getId()  // Different user
        );

        // Then
        assertThat(result).isEqualTo(0);
        assertThat(transactionRepository.findById(saved.getId())).isPresent();
    }
}
