package com.example.filesharing.Repositories;

import com.example.filesharing.Entities.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileData, UUID> {
    Optional<FileData> findByToken(String token);
    @Query("SELECT f FROM FileData f WHERE f.createdAt < :threshold AND f.downloaded = false")
    List<FileData> findOldFiles(@Param("threshold") LocalDateTime threshold);
}