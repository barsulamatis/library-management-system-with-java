package com.librarymanagementsystem.dao;

import com.librarymanagementsystem.model.BorrowRecord;
import com.librarymanagementsystem.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAOImpl implements BorrowDAO {
    private static final Logger logger = LoggerFactory.getLogger(BorrowDAOImpl.class);

    @Override
    public BorrowRecord getBorrowById(int borrowId) throws DatabaseException {
        String sql = "SELECT * FROM borrow_records WHERE borrow_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borrowId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBorrowRecord(rs);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving borrow record: " + borrowId, e);
            throw new DatabaseException("Failed to retrieve borrow record", e);
        }
        return null;
    }

    @Override
    public List<BorrowRecord> getBorrowsByUserId(int userId) throws DatabaseException {
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
            throw new DatabaseException("Failed to retrieve borrow records", e);
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getBorrowsByBookId(int bookId) throws DatabaseException {
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
            throw new DatabaseException("Failed to retrieve borrow records", e);
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getActiveBorrows() throws DatabaseException {
        String sql = "SELECT * FROM borrow_records WHERE status = 'ACTIVE'";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving active borrow records", e);
            throw new DatabaseException("Failed to retrieve active borrow records", e);
        }
        return records;
    }

    @Override
    public List<BorrowRecord> getOverdueBorrows() throws DatabaseException {
        String sql = "SELECT * FROM borrow_records WHERE status = 'OVERDUE' OR (status = 'ACTIVE' AND due_date < NOW())";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
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
    public void addBorrow(BorrowRecord borrowRecord) throws DatabaseException {
        String sql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status, fine) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borrowRecord.getUserId());
            stmt.setInt(2, borrowRecord.getBookId());
            stmt.setTimestamp(3, Timestamp.valueOf(borrowRecord.getBorrowDate()));
            stmt.setTimestamp(4, Timestamp.valueOf(borrowRecord.getDueDate()));
            stmt.setString(5, borrowRecord.getStatus());
            stmt.setDouble(6, borrowRecord.getFine());
            stmt.executeUpdate();
            logger.info("Borrow record added successfully");
        } catch (SQLException e) {
            logger.error("Error adding borrow record", e);
            throw new DatabaseException("Failed to add borrow record", e);
        }
    }

    @Override
    public void updateBorrow(BorrowRecord borrowRecord) throws DatabaseException {
        String sql = "UPDATE borrow_records SET user_id = ?, book_id = ?, borrow_date = ?, due_date = ?, return_date = ?, status = ?, fine = ? WHERE borrow_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borrowRecord.getUserId());
            stmt.setInt(2, borrowRecord.getBookId());
            stmt.setTimestamp(3, Timestamp.valueOf(borrowRecord.getBorrowDate()));
            stmt.setTimestamp(4, Timestamp.valueOf(borrowRecord.getDueDate()));
            stmt.setTimestamp(5, borrowRecord.getReturnDate() != null ? Timestamp.valueOf(borrowRecord.getReturnDate()) : null);
            stmt.setString(6, borrowRecord.getStatus());
            stmt.setDouble(7, borrowRecord.getFine());
            stmt.setInt(8, borrowRecord.getBorrowId());
            stmt.executeUpdate();
            logger.info("Borrow record updated successfully");
        } catch (SQLException e) {
            logger.error("Error updating borrow record", e);
            throw new DatabaseException("Failed to update borrow record", e);
        }
    }

    @Override
    public void returnBook(int borrowId) throws DatabaseException {
        String sql = "UPDATE borrow_records SET return_date = ?, status = 'RETURNED' WHERE borrow_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, borrowId);
            stmt.executeUpdate();
            logger.info("Book returned successfully");
        } catch (SQLException e) {
            logger.error("Error returning book", e);
            throw new DatabaseException("Failed to return book", e);
        }
    }

    @Override
    public void updateFine(int borrowId, double fine) throws DatabaseException {
        String sql = "UPDATE borrow_records SET fine = ? WHERE borrow_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, fine);
            stmt.setInt(2, borrowId);
            stmt.executeUpdate();
            logger.info("Fine updated successfully");
        } catch (SQLException e) {
            logger.error("Error updating fine", e);
            throw new DatabaseException("Failed to update fine", e);
        }
    }

    @Override
    public List<BorrowRecord> getAllBorrowRecords() throws DatabaseException {
        String sql = "SELECT * FROM borrow_records";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all borrow records", e);
            throw new DatabaseException("Failed to retrieve borrow records", e);
        }
        return records;
    }

    private BorrowRecord mapResultSetToBorrowRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setBorrowId(rs.getInt("borrow_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());
        record.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());
        Timestamp returnDate = rs.getTimestamp("return_date");
        if (returnDate != null) {
            record.setReturnDate(returnDate.toLocalDateTime());
        }
        record.setStatus(rs.getString("status"));
        record.setFine(rs.getDouble("fine"));
        return record;
    }
}
