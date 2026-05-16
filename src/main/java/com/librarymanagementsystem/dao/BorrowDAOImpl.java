package com.librarymanagementsystem.dao;

import com.librarymanagementsystem.model.BorrowRecord;
import com.librarymanagementsystem.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of BorrowDAO interface
 * Handles all database operations for borrow records
 */
public class BorrowDAOImpl implements BorrowDAO {
    private static final Logger logger = LoggerFactory.getLogger(BorrowDAOImpl.class);

    @Override
    public BorrowRecord getBorrowRecordById(int borrowId) throws DatabaseException {
        String sql = "SELECT * FROM borrow_records WHERE borrow_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borrowId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBorrowRecord(rs);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving borrow record with ID: " + borrowId, e);
            throw new DatabaseException("Failed to retrieve borrow record", e);
        }
        return null;
    }

    @Override
    public List<BorrowRecord> getBorrowRecordsByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM borrow_records WHERE user_id = ?";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving borrow records for user: " + userId, e);
            throw new DatabaseException("Failed to retrieve borrow records for user", e);
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getActiveBorrowRecordsByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM borrow_records WHERE user_id = ? AND status = 'ACTIVE'";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving active borrow records for user: " + userId, e);
            throw new DatabaseException("Failed to retrieve active borrow records", e);
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getBorrowRecordsByBookId(int bookId) throws DatabaseException {
        String sql = "SELECT * FROM borrow_records WHERE book_id = ?";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving borrow records for book: " + bookId, e);
            throw new DatabaseException("Failed to retrieve borrow records for book", e);
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getOverdueBorrowRecords() throws DatabaseException {
        String sql = "SELECT * FROM borrow_records WHERE status = 'OVERDUE' OR (status = 'ACTIVE' AND due_date < NOW())";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving overdue borrow records", e);
            throw new DatabaseException("Failed to retrieve overdue borrow records", e);
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getAllBorrowRecords() throws DatabaseException {
        String sql = "SELECT * FROM borrow_records";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all borrow records", e);
            throw new DatabaseException("Failed to retrieve borrow records", e);
        }
        return records;
    }

    @Override
    public boolean addBorrowRecord(BorrowRecord record) throws DatabaseException {
        String sql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status, fine) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, record.getUserId());
            stmt.setInt(2, record.getBookId());
            stmt.setTimestamp(3, Timestamp.valueOf(record.getBorrowDate()));
            stmt.setTimestamp(4, Timestamp.valueOf(record.getDueDate()));
            stmt.setString(5, record.getStatus());
            stmt.setDouble(6, record.getFine());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    record.setBorrowId(generatedKeys.getInt(1));
                }
                logger.info("Borrow record added successfully");
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error adding borrow record", e);
            throw new DatabaseException("Failed to add borrow record", e);
        }
        return false;
    }

    @Override
    public boolean updateBorrowRecord(BorrowRecord record) throws DatabaseException {
        String sql = "UPDATE borrow_records SET user_id = ?, book_id = ?, borrow_date = ?, due_date = ?, return_date = ?, status = ?, fine = ? WHERE borrow_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getUserId());
            stmt.setInt(2, record.getBookId());
            stmt.setTimestamp(3, Timestamp.valueOf(record.getBorrowDate()));
            stmt.setTimestamp(4, Timestamp.valueOf(record.getDueDate()));
            stmt.setTimestamp(5, record.getReturnDate() != null ? Timestamp.valueOf(record.getReturnDate()) : null);
            stmt.setString(6, record.getStatus());
            stmt.setDouble(7, record.getFine());
            stmt.setInt(8, record.getBorrowId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Borrow record updated successfully");
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating borrow record", e);
            throw new DatabaseException("Failed to update borrow record", e);
        }
        return false;
    }

    @Override
    public boolean deleteBorrowRecord(int borrowId) throws DatabaseException {
        String sql = "DELETE FROM borrow_records WHERE borrow_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borrowId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Borrow record deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deleting borrow record", e);
            throw new DatabaseException("Failed to delete borrow record", e);
        }
        return false;
    }

    @Override
    public int getActiveBorrowCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting active borrow records", e);
            throw new DatabaseException("Failed to count active borrow records", e);
        }
        return 0;
    }

    @Override
    public int getOverdueBorrowCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE status = 'OVERDUE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting overdue borrow records", e);
            throw new DatabaseException("Failed to count overdue borrow records", e);
        }
        return 0;
    }

    /**
     * Map ResultSet to BorrowRecord object
     */
    private BorrowRecord mapResultSetToBorrowRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setBorrowId(rs.getInt("borrow_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBookId(rs.getInt("book_id"));
        
        Timestamp borrowDate = rs.getTimestamp("borrow_date");
        if (borrowDate != null) {
            record.setBorrowDate(borrowDate.toLocalDateTime());
        }
        
        Timestamp dueDate = rs.getTimestamp("due_date");
        if (dueDate != null) {
            record.setDueDate(dueDate.toLocalDateTime());
        }
        
        Timestamp returnDate = rs.getTimestamp("return_date");
        if (returnDate != null) {
            record.setReturnDate(returnDate.toLocalDateTime());
        }
        
        record.setStatus(rs.getString("status"));
        record.setFine(rs.getDouble("fine"));
        
        return record;
    }
}
