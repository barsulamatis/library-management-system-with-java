package com.librarymanagementsystem.service;

import com.librarymanagementsystem.dao.UserDAO;
import com.librarymanagementsystem.dao.UserDAOImpl;
import com.librarymanagementsystem.dto.UserDTO;
import com.librarymanagementsystem.exception.DatabaseException;
import com.librarymanagementsystem.model.User;
import com.librarymanagementsystem.util.PasswordUtil;
import com.librarymanagementsystem.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service class for user management operations
 * Handles business logic for user-related operations
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Register a new user
     * @param userDTO User data transfer object
     * @return true if registration is successful, false otherwise
     */
    public boolean registerUser(UserDTO userDTO) {
        try {
            // Validate input
            if (!Validator.isValidEmail(userDTO.getEmail())) {
                logger.warn("Invalid email format: {}", userDTO.getEmail());
                return false;
            }

            if (!Validator.isValidPhoneNumber(userDTO.getPhoneNumber())) {
                logger.warn("Invalid phone number format: {}", userDTO.getPhoneNumber());
                return false;
            }

            if (!Validator.isValidPassword(userDTO.getPassword())) {
                logger.warn("Password does not meet security requirements");
                return false;
            }

            // Check if user already exists
            if (userDAO.getUserByUsername(userDTO.getUsername()) != null) {
                logger.warn("Username already exists: {}", userDTO.getUsername());
                return false;
            }

            if (userDAO.getUserByEmail(userDTO.getEmail()) != null) {
                logger.warn("Email already registered: {}", userDTO.getEmail());
                return false;
            }

            // Hash password
            String passwordHash = PasswordUtil.hashPassword(userDTO.getPassword());

            // Create user object
            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setPasswordHash(passwordHash);
            user.setFullName(userDTO.getFullName());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setRole(userDTO.getRole() != null ? userDTO.getRole() : "MEMBER");
            user.setActive(true);

            // Save to database
            boolean success = userDAO.addUser(user);
            if (success) {
                logger.info("User registered successfully: {}", userDTO.getUsername());
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error during user registration", e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            return false;
        }
    }

    /**
     * Get user by username
     * @param username Username
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        try {
            return userDAO.getUserByUsername(username);
        } catch (DatabaseException e) {
            logger.error("Database error retrieving user by username: {}", username, e);
            return null;
        }
    }

    /**
     * Get user by email
     * @param email Email address
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        try {
            return userDAO.getUserByEmail(email);
        } catch (DatabaseException e) {
            logger.error("Database error retrieving user by email: {}", email, e);
            return null;
        }
    }

    /**
     * Get user by ID
     * @param userId User ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        try {
            return userDAO.getUserById(userId);
        } catch (DatabaseException e) {
            logger.error("Database error retrieving user by ID: {}", userId, e);
            return null;
        }
    }

    /**
     * Update user information
     * @param user User object with updated information
     * @return true if update is successful, false otherwise
     */
    public boolean updateUser(User user) {
        try {
            if (user == null || user.getUserId() <= 0) {
                logger.warn("Invalid user object for update");
                return false;
            }

            boolean success = userDAO.updateUser(user);
            if (success) {
                logger.info("User updated successfully: {}", user.getUsername());
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error updating user", e);
            return false;
        }
    }

    /**
     * Delete user by ID
     * @param userId User ID
     * @return true if deletion is successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        try {
            boolean success = userDAO.deleteUser(userId);
            if (success) {
                logger.info("User deleted successfully: ID {}", userId);
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error deleting user", e);
            return false;
        }
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (DatabaseException e) {
            logger.error("Database error retrieving all users", e);
            return null;
        }
    }

    /**
     * Get all users by role
     * @param role User role (ADMIN, LIBRARIAN, MEMBER)
     * @return List of users with the specified role
     */
    public List<User> getUsersByRole(String role) {
        try {
            return userDAO.getUsersByRole(role);
        } catch (DatabaseException e) {
            logger.error("Database error retrieving users by role: {}", role, e);
            return null;
        }
    }

    /**
     * Deactivate user account
     * @param userId User ID
     * @return true if deactivation is successful, false otherwise
     */
    public boolean deactivateUser(int userId) {
        try {
            User user = userDAO.getUserById(userId);
            if (user == null) {
                logger.warn("User not found for deactivation: {}", userId);
                return false;
            }

            user.setActive(false);
            boolean success = userDAO.updateUser(user);
            if (success) {
                logger.info("User deactivated: {}", userId);
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error deactivating user", e);
            return false;
        }
    }

    /**
     * Activate user account
     * @param userId User ID
     * @return true if activation is successful, false otherwise
     */
    public boolean activateUser(int userId) {
        try {
            User user = userDAO.getUserById(userId);
            if (user == null) {
                logger.warn("User not found for activation: {}", userId);
                return false;
            }

            user.setActive(true);
            boolean success = userDAO.updateUser(user);
            if (success) {
                logger.info("User activated: {}", userId);
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error activating user", e);
            return false;
        }
    }

    /**
     * Change user password
     * @param userId User ID
     * @param oldPassword Old password
     * @param newPassword New password
     * @return true if password change is successful, false otherwise
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        try {
            User user = userDAO.getUserById(userId);
            if (user == null) {
                logger.warn("User not found for password change: {}", userId);
                return false;
            }

            // Verify old password
            if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
                logger.warn("Old password verification failed for user: {}", userId);
                return false;
            }

            // Validate new password
            if (!Validator.isValidPassword(newPassword)) {
                logger.warn("New password does not meet security requirements");
                return false;
            }

            // Hash new password
            String newPasswordHash = PasswordUtil.hashPassword(newPassword);
            user.setPasswordHash(newPasswordHash);

            boolean success = userDAO.updateUser(user);
            if (success) {
                logger.info("Password changed successfully for user: {}", userId);
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error changing password", e);
            return false;
        }
    }

    /**
     * Count total users
     * @return Total number of users
     */
    public int getTotalUserCount() {
        try {
            return userDAO.getTotalUserCount();
        } catch (DatabaseException e) {
            logger.error("Database error counting users", e);
            return 0;
        }
    }
}
