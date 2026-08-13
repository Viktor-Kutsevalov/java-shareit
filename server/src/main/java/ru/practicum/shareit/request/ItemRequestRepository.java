package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @EntityGraph(attributePaths = "items")
    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(Long requestorId);

    @EntityGraph(attributePaths = "items")
    @Query("SELECT r FROM ItemRequest r WHERE r.requestor.id != :userId ORDER BY r.created DESC")
    List<ItemRequest> findAllByRequestorIdNot(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Optional<ItemRequest> findById(Long id);
}
