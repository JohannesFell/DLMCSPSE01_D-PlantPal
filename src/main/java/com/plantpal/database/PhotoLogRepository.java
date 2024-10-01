package com.plantpal.database;

import java.sql.*;
import java.time.LocalDateTime;

public class PhotoLogRepository {

    public void savePhoto(int plantId, String photoPath, LocalDateTime dateTaken) throws SQLException {
        String sql = "INSERT INTO PhotoLog(plant_id, photo_path, date_taken) VALUES(?, ?, ?)";

        try (Connection conn = SQLiteDB.getConnection()) {
            conn.setAutoCommit(false); // Transaktion starten
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, plantId);
                pstmt.setString(2, photoPath);
                pstmt.setTimestamp(3, Timestamp.valueOf(dateTaken));
                pstmt.executeUpdate();
                conn.commit(); // Transaktion comitten
            } catch (SQLException e) {
                conn.rollback(); // Im Fehlerfall zurückrollen
                throw e;
            }
        }
    }

    public String getLatestPhotoPath(int plantId) {
        String sql = "SELECT photo_path FROM PhotoLog WHERE plant_id = ? ORDER BY date_taken DESC LIMIT 1";
        try (Connection conn = SQLiteDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, plantId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("photo_path");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

