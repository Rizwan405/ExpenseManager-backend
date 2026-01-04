package com.example.demo.repository;

import com.example.demo.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepoTest {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    public void userRepoTest_shouldReturnUser(){
//        Given

        Users user1 = Users.builder().username("zaheer").password("Rizwan312cb@").build();
        userRepo.save(user1);
//        When
        Users result  = userRepo.findByUsername("zaheer");
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("zaheer");

    }
    @Test
    void findByUsername_shouldReturnEmptyOptional_whenUserNotExists() {
        // When
        Users result = userRepo.findByUsername("non_existent");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void findByUsername_shouldReturnUserWithTransactions() {
        // Given
       Users user2 = Users.builder().username("rizwan").password("Rizwan312cb@").build();
        Transaction transaction1 = Transaction.builder()
                                              .title("Test Transaction")
                                              .amount(100.00)
                                              .transactionType(TransactionType.Expense)
                                              .date(LocalDate.now())
                                              .paymentType(PaymentType.Cash)
                                              .category(CategoryType.Health)
                                              .build();
        transaction1.setUser(user2);

        user2.setTransactions(List.of(transaction1));
        userRepo.save(user2);
        transactionRepository.save(transaction1);
        // When
        Users result = userRepo.findByUsername("rizwan");
//        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactions()).hasSize(1);
        assertThat(result.getTransactions().get(0).getTitle()).isEqualTo("Test Transaction");
    }
}
