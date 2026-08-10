package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"item", "booker"})
    Optional<Booking> findById(Long id);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where i.owner.id = :ownerId")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where i.owner.id = :ownerId and b.start < :now and b.end > :now")
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Sort sort);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where i.owner.id = :ownerId and b.end < :now")
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Sort sort);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where i.owner.id = :ownerId and b.start > :now")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Sort sort);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where i.owner.id = :ownerId and b.status = :status")
    List<Booking> findAllByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status, Sort sort);

    @Query("select b from Booking b where b.booker.id = :userId and b.item.id = :itemId and b.status = 'APPROVED' and b.end < :now")
    List<Booking> findCompletedBookingsForComment(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where b.item.id = :itemId and b.status = 'APPROVED' and b.start < :now order by b.start desc")
    List<Booking> findLastApprovedByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where b.item.id = :itemId and b.status = 'APPROVED' and b.start > :now order by b.start asc")
    List<Booking> findNextApprovedByItemId(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where b.item.id in :itemIds and b.status = 'APPROVED' and b.start < :now order by b.start desc")
    List<Booking> findLastApprovedByItemIdIn(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now, Sort sort);

    @Query("select b from Booking b join fetch b.item i join fetch b.booker u where b.item.id in :itemIds and b.status = 'APPROVED' and b.start > :now order by b.start asc")
    List<Booking> findNextApprovedByItemIdIn(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now, Sort sort);
}
