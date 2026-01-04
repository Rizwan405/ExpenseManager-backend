package com.example.demo.controller;

import com.example.demo.dto.TransactionDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.CategoryType;
import com.example.demo.model.PaymentType;
import com.example.demo.model.TransactionType;
import com.example.demo.model.Users;
import com.example.demo.service.JWTService;
import com.example.demo.service.TransactionService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoBeans;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private JWTService jwtService;

    private Users mockUser;
    private TransactionDTO mockTransactionDTO;
    private String validToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    @BeforeEach
    public void setUp() {
        this.mockUser = Users.builder()
                .id(1L)
                .username("testUser")
                .password("<PASSWORD>")
                .build();

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");

        this.mockTransactionDTO = TransactionDTO.builder()
                .id(10L)
                .title("Test Transaction")
                .amount(100.00)
                .transactionType(TransactionType.Expense)
                .date(LocalDate.now())
                .paymentType(PaymentType.Cash)
                .category(CategoryType.Health)
                .user(userDTO)
                .build();
    }

    private void setupSecurityContext() {
        // Create a mock Authentication object
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock the principal (current user)
        when(authentication.getPrincipal()).thenReturn(mockUser);
    }

    @Test
    void homeEndpoint_ShouldReturnWelcomeMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("welcome home"));
    }

    @Test
    void homeEndpoint_ShouldNotRequireAuthentication() throws Exception {
        // Clear security context to simulate unauthenticated request
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
    @Test
    void registerUser_WithValidUserData_ShouldReturnUser() throws Exception {
        // Arrange
        Users newUser = Users.builder().username("newuser").password("password123").build();

        doNothing().when(userService).registerUser(any(Users.class));

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(userService, times(1)).registerUser(any(Users.class));
    }
    @Test
    void registerUser_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Arrange: Invalid JSON
        String invalidJson = "{username: \"test\", email: invalid}";

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
    @Test
    void registerUser_WithMissingRequiredFields_ShouldStillProcess() throws Exception {
        // Arrange: User missing password (assuming it's required in service layer)
        Users incompleteUser = new Users();
        incompleteUser.setUsername("incomplete");
        // No password set

        doNothing().when(userService).registerUser(any(Users.class));

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteUser)))
                .andExpect(status().isOk());
    }

    /**
     * Test 3: POST /login - User Login
     */
    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        Users loginUser = new Users();
        loginUser.setUsername("testuser");
        loginUser.setPassword("password123");

        when(userService.verify(any(Users.class))).thenReturn("valid.jwt.token");

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("valid.jwt.token"));

        verify(userService, times(1)).verify(any(Users.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnErrorMessage() throws Exception {
        // Arrange
        Users loginUser = new Users();
        loginUser.setUsername("wronguser");
        loginUser.setPassword("wrongpass");

        when(userService.verify(any(Users.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())  // Note: Your controller returns 200 even on error
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_WithEmptyCredentials_ShouldHandleGracefully() throws Exception {
        // Arrange
        Users emptyUser = new Users();
        emptyUser.setUsername("");
        emptyUser.setPassword("");

        when(userService.verify(any(Users.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }


    /**
     * Test 4: POST /addNew - Create Transaction
     * This requires authentication and uses @Valid annotation
     */

}
