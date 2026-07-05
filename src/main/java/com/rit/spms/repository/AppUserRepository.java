package com.rit.spms.repository;

import com.rit.spms.domain.AppUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findByActiveTrue();

    @Query("SELECT u FROM AppUser u WHERE u.active = true AND (" +
           "LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.fname) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(u.lname) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<AppUser> searchActive(@Param("q") String q, Pageable pageable);
}
