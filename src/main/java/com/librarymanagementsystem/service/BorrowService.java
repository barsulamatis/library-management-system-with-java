package com.librarymanagementsystem.service;

import com.librarymanagementsystem.dao.BorrowDAO;
import com.librarymanagementsystem.dao.BorrowDAOImpl;
import com.librarymanagementsystem.exception.DatabaseException;
import com.librarymanagementsystem.model.BorrowRecord;
import com.librarymanagementsystem.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service class for borrow/return operations
 * Handles business logic for book borrowing and returning
 */
public class BorrowService {
    private static final Logger logger = LoggerFactory.getLogger(BorrowService.class);
    private final BorrowDAO borrowDAO;
    private static final double FINE_PER_DAY = 5.0; // Currency units per day

    public BorrowService() {
        this.borrowDAO = new BorrowDAOImpl();
    }

    /**
     * Borrow a book
     * @param userId User ID
     * @param bookId Book ID
     * @param borrowDays Number of days to borrow
     * @return BorrowRecord if successful, null otherwise
     */
    public BorrowRecord borrowBook(int userId, int bookId, int borrowDays) {
        try {
            // Validate input
            if (userId <= 0 || bookId <= 0 || borrowDays <= 0) {
                logger.warn("Invalid borrow parameters: userId={}, bookId={}, borrowDays={}", 
                           userId, bookId, borrowDays);
                return null;
            }

            // Create borrow record
            BorrowRecord record = new BorrowRecord();
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setBorrowDate(LocalDateTime.now());
            record.setDueDate(LocalDateTime.now().plusDays(borrowDays));
            record.setStatus("ACTIVE");
            record.setFine(0.0);

            // Save to database
            boolean success = borrowDAO.addBorrowRecord(record);
            if (success) {
                logger.info("Book borrowed successfully: userId={}, bookId={}", userId, bookId);
                return borrowDAO.getBorrowRecordById(record.getBorrowId());
            }

            return null;

        } catch (DatabaseException e) {
            logger.error("Database error during book borrow", e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error during book borrow", e);
            return null;
        }
    }

    /**
     * Return a book
     * @param borrowId Borrow record ID
     * @return true if return is successful, false otherwise
     */
    public boolean returnBook(int borrowId) {
        try {
            BorrowRecord record = borrowDAO.getBorrowRecordById(borrowId);
            if (record == null) {
                logger.warn("Borrow record not found: {}", borrowId);
                return false;
            }

            if (!"ACTIVE".equals(record.getStatus())) {
                logger.warn("Borrow record is not active: {}", borrowId);
                return false;
            }

            // Calculate fine if overdue
            LocalDateTime returnDate = LocalDateTime.now();
            double fine = calculateFine(record.getDueDate(), returnDate);

            // Update record
            record.setReturnDate(returnDate);
            record.setStatus("RETURNED");
            record.setFine(fine);

            boolean success = borrowDAO.updateBorrowRecord(record);
            if (success) {
                logger.info("Book returned successfully: borrowId={}, fine={}", borrowId, fine);
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error during book return", e);
            return false;
        }
    }

    /**
     * Calculate fine for overdue books
     * @param dueDate Due date
     * @param returnDate Return date
     * @return Fine amount (0 if not overdue)
     */
    private double calculateFine(LocalDateTime dueDate, LocalDateTime returnDate) {
        if (returnDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
            return daysOverdue * FINE_PER_DAY;
        }
        return 0.0;
    }

    /**
     * Get borrow record by ID
     * @param borrowId Borrow record ID
     * @return BorrowRecord if found, null otherwise
     */
    public BorrowRecord getBorrowRecordById(int borrowId) {
        try {
            return borrowDAO.getBorrowRecordById(borrowId);
        } catch (DatabaseException e) {
            logger.error("Database error retrieving borrow record: {}", borrowId, e);
            return null;
        }
    }

    /**
     * Get all borrow records for a user
     * @param userId User ID
     * @return List of borrow records
     */
    public List<BorrowRecord> getBorrowRecordsByUserId(int userId) {
        try {
            return borrowDAO.getBorrowRecordsByUserId(userId);
        } catch (DatabaseException e) {
            logger.error("Database error retrieving borrow records for user: {}", userId, e);
            return null;
        }
    }

    /**
     * Get all active borrow records for a user
     * @param userId User ID
     * @return List of active borrow records
     */
    public List<BorrowRecord> getActiveBorrowRecordsByUserId(int userId) {
        try {
            return borrowDAO.getActiveBorrowRecordsByUserId(userId);
        } catch (DatabaseException e) {
            logger.error("Database error retrieving active borrow records for user: {}", userId, e);
            return null;
        }
    }

    /**
     * Get all overdue borrow records
     * @return List of overdue borrow records
     */
    public List<BorrowRecord> getOverdueBorrowRecords() {
        try {
            List<BorrowRecord> records = borrowDAO.getOverdueBorrowRecords();
            
            // Update status to OVERDUE
            for (BorrowRecord record : records) {
                if ("ACTIVE".equals(record.getStatus()) && LocalDateTime.now().isAfter(record.getDueDate())) {
                    record.setStatus("OVERDUE");
                    double fine = calculateFine(record.getDueDate(), LocalDateTime.now());
                    record.setFine(fine);
                    borrowDAO.updateBorrowRecord(record);
                }
            }
            
            return records;
        } catch (DatabaseException e) {
            logger.error("Database error retrieving overdue borrow records", e);
            return null;
        }
    }

    /**
     * Get all borrow records for a book
     * @param bookId Book ID
     * @return List of borrow records
     */
    public List<BorrowRecord> getBorrowRecordsByBookId(int bookId) {
        try {
            return borrowDAO.getBorrowRecordsByBookId(bookId);
        } catch (DatabaseException e) {
            logger.error("Database error retrieving borrow records for book: {}", bookId, e);
            return null;
        }
    }

    /**
     * Update borrow record
     * @param record BorrowRecord to update
     * @return true if update is successful, false otherwise
     */
    public boolean updateBorrowRecord(BorrowRecord record) {
        try {
            if (record == null || record.getBorrowId() <= 0) {
                logger.warn("Invalid borrow record for update");
                return false;
            }

            boolean success = borrowDAO.updateBorrowRecord(record);
            if (success) {
                logger.info("Borrow record updated successfully: {}", record.getBorrowId());
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error updating borrow record", e);
            return false;
        }
    }

    /**
     * Delete borrow record
     * @param borrowId Borrow record ID
     * @return true if deletion is successful, false otherwise
     */
    public boolean deleteBorrowRecord(int borrowId) {
        try {
            boolean success = borrowDAO.deleteBorrowRecord(borrowId);
            if (success) {
                logger.info("Borrow record deleted successfully: {}", borrowId);
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error deleting borrow record", e);
            return false;
        }
    }

    /**
     * Get all borrow records
     * @return List of all borrow records
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        try {
            return borrowDAO.getAllBorrowRecords();
        } catch (DatabaseException e) {
            logger.error("Database error retrieving all borrow records", e);
            return null;
        }
    }

    /**
     * Get total count of active borrows
     * @return Total number of active borrows
     */
    public int getActiveBorrowCount() {
        try {
            return borrowDAO.getActiveBorrowCount();
        } catch (DatabaseException e) {
            logger.error("Database error counting active borrows", e);
            return 0;
        }
    }

    /**
     * Get total count of overdue borrows
     * @return Total number of overdue borrows
     */
    public int getOverdueBorrowCount() {
        try {
            return borrowDAO.getOverdueBorrowCount();
        } catch (DatabaseException e) {
            logger.error("Database error counting overdue borrows", e);
            return 0;
        }
    }

    /**
     * Calculate total fines for a user
     * @param userId User ID
     * @return Total fine amount
     */
    public double calculateUserTotalFines(int userId) {
        try {
            List<BorrowRecord> records = borrowDAO.getBorrowRecordsByUserId(userId);
            double totalFines = 0;
            for (BorrowRecord record : records) {
                totalFines += record.getFine();
            }
            return totalFines;
        } catch (DatabaseException e) {
            logger.error("Database error calculating fines for user: {}", userId, e);
            return 0;
        }
    }

    /**
     * Pay fine for a borrow record
     * @param borrowId Borrow record ID
     * @param amountPaid Amount paid
     * @return true if payment is successful, false otherwise
     */
    public boolean payFine(int borrowId, double amountPaid) {
        try {
            BorrowRecord record = borrowDAO.getBorrowRecordById(borrowId);
            if (record == null) {
                logger.warn("Borrow record not found for fine payment: {}", borrowId);
                return false;
            }

            double remainingFine = record.getFine() - amountPaid;
            if (remainingFine < 0) {
                logger.warn("Payment amount exceeds outstanding fine");
                return false;
            }

            record.setFine(remainingFine);
            boolean success = borrowDAO.updateBorrowRecord(record);
            if (success) {
                logger.info("Fine payment processed: borrowId={}, amountPaid={}", borrowId, amountPaid);
            }
            return success;

        } catch (DatabaseException e) {
            logger.error("Database error processing fine payment", e);
            return false;
        }
    }

    /**
     * Get fine rate per day
     * @return Fine amount per day
     */
    public double getFinePerDay() {
        return FINE_PER_DAY;
    }
}
