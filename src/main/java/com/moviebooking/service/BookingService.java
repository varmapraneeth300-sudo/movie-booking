package com.moviebooking.service;

import com.moviebooking.dto.*;
import com.moviebooking.entity.*;
import com.moviebooking.entity.enums.BookingStatus;
import com.moviebooking.entity.enums.ShowSeatStatus;
import com.moviebooking.entity.enums.ShowStatus;
import com.moviebooking.exception.ApplicationException;
import com.moviebooking.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepo bookingRepo;
    private final ShowRepo showRepo;
    private final SeatRepo seatRepo;
    private final ShowSeatRepo showSeatRepo;
    private final BookingSeatRepo bookingSeatRepo;
    private final RazorpayService razorpayService;
    private final UserRepo userRepo;

    BookingService(BookingRepo bookingRepo, ShowRepo showRepo, ShowSeatRepo showSeatRepo, SeatRepo seatRepo, BookingSeatRepo bookingSeatRepo, RazorpayService razorpayService, UserRepo userRepo) {
        this.bookingRepo = bookingRepo;
        this.showSeatRepo = showSeatRepo;
        this.showRepo = showRepo;
        this.seatRepo = seatRepo;
        this.bookingSeatRepo = bookingSeatRepo;
        this.razorpayService = razorpayService;
        this.userRepo = userRepo;
    }

    @Transactional
    public RazorpayOrderResponse initiateBooking(BookingRequest bookingRequest) {
        User user = getUser();
        Show show = showRepo.findById(bookingRequest.getShowId())
                .orElseThrow(() -> new ApplicationException(
                        "Show not found",
                        HttpStatus.NOT_FOUND
                ));

        if (show.getShowStatus() == ShowStatus.INACTIVE) {
            throw new ApplicationException(
                    "Cannot book an inactive show",
                    HttpStatus.BAD_REQUEST
            );
        }

        Set<Long> seatIds = bookingRequest.getBookingSeatRequests()
                .stream()
                .map(BookingSeatRequest::getSeatId)
                .collect(Collectors.toSet());

        if (seatIds.size() != bookingRequest.getBookingSeatRequests().size()) {
            throw new ApplicationException(
                    "Duplicate seats are not allowed",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<ShowSeat> bookedShowSeats = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Long seatId : seatIds) {

            ShowSeat showSeat = showSeatRepo
                    .findByShowAndSeatId(show, seatId)
                    .orElseThrow(() -> new ApplicationException(
                            "Seat not found for this show: " + seatId,
                            HttpStatus.NOT_FOUND
                    ));

            if (showSeat.getStatus() != ShowSeatStatus.AVAILABLE && !Objects.equals(showSeat.getHeldByUserId(), getUser().getId())) {
                throw new ApplicationException(
                        "Seat " + seatId + " is not available",
                        HttpStatus.CONFLICT
                );
            }

            showSeat.setStatus(ShowSeatStatus.HELD);

            bookedShowSeats.add(showSeat);

            totalAmount = totalAmount.add(BigDecimal.valueOf(showSeat.getPrice()));
        }


        // todo : call Razorpay
        RazorpayOrderResponse razorpayOrderResponse;
        try {
            LocalDateTime orderExpiry = LocalDateTime.now().plusMinutes(1);
            Booking booking = new Booking();
            booking.setPaymentInitiationExpiresAt(orderExpiry);
            razorpayOrderResponse = razorpayService.createOrder(totalAmount, orderExpiry);
            List<BookingSeat> bookingSeats = new ArrayList<>();
            booking.setStatus(BookingStatus.PENDING);
            booking.setRazorpayOrderId(razorpayOrderResponse.getOrderId());
            booking.setTotalAmount(totalAmount);
            booking.setShow(show);

            booking.setUser(user);
            bookingRepo.save(booking);

            for (ShowSeat showSeat : bookedShowSeats) {
                BookingSeat bookingSeat = new BookingSeat();
                bookingSeat.setBooking(booking);
                bookingSeat.setShowSeat(showSeat);
                bookingSeat.setPrice(BigDecimal.valueOf(showSeat.getPrice()));
                bookingSeats.add(bookingSeat);
            }
            bookingSeatRepo.saveAll(bookingSeats);
            return razorpayOrderResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetBookingResponse getBookingById(Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ApplicationException(
                        "Booking not found",
                        HttpStatus.NOT_FOUND
                ));

        List<BookingSeatResponse> seats = bookingSeatRepo
                .findByBooking(booking)
                .stream()
                .map(this::mapToBookingSeatResponse)
                .toList();

        Show show = booking.getShow();

        return new GetBookingResponse(
                booking.getId(),
                show.getId(),
                show.getStartTime(),
                show.getEndTime(),
                booking.getTotalAmount(),
                booking.getStatus(),
                seats
        );
    }

    private BookingSeatResponse mapToBookingSeatResponse(
            BookingSeat bookingSeat) {

        Seat seat = bookingSeat.getShowSeat().getSeat();

        return new BookingSeatResponse(
                seat.getId(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                seat.getSeatType(),
                bookingSeat.getPrice()
        );
    }

    @Transactional
    public CancelBookingResponse cancelBooking(Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new ApplicationException(
                        "Booking not found",
                        HttpStatus.NOT_FOUND
                ));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApplicationException(
                    "Booking is already cancelled",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<BookingSeat> bookingSeats =
                bookingSeatRepo.findByBooking(booking);

        for (BookingSeat bookingSeat : bookingSeats) {
            bookingSeat.getShowSeat()
                    .setStatus(ShowSeatStatus.AVAILABLE);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        return new CancelBookingResponse(
                booking.getId(),
                booking.getStatus(),
                "Booking cancelled successfully"
        );
    }

    @Transactional
    public BookingConfirmationResponse finalizeBooking(
            String razorpayOrderId) {


        Booking booking = bookingRepo
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() ->
                        new ApplicationException(
                                "Booking not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        List<BookingSeat> bookingSeats =
                bookingSeatRepo.findByBooking(booking);

        for (BookingSeat bookingSeat : bookingSeats) {

            ShowSeat showSeat =
                    bookingSeat.getShowSeat();
            showSeat.setHeldByUserId(null);
            showSeat.setHoldExpiresAt(null);
            showSeat.setHoldId(null);
            showSeat.setStatus(ShowSeatStatus.BOOKED);
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        return new BookingConfirmationResponse(
                booking.getId(),
                booking.getRazorpayOrderId(),
                "Booking confirmed successfully"
        );
    }

    @Transactional
    public BookingConfirmationResponse releaseSeatsAfterPaymentFailure(
            String razorpayOrderId) {
        Booking booking = bookingRepo
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() ->
                        new ApplicationException(
                                "Booking not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        List<BookingSeat> bookingSeats =
                bookingSeatRepo.findByBooking(booking);

        for (BookingSeat bookingSeat : bookingSeats) {

            ShowSeat showSeat =
                    bookingSeat.getShowSeat();

            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
        }
        booking.setStatus(BookingStatus.FAILED);

        return new BookingConfirmationResponse(
                booking.getId(),
                booking.getRazorpayOrderId(),
                "Payment failed. Booking cancelled and seats released."
        );
    }

    User getUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();
        return userRepo.findByUsername(username).orElseThrow(() -> new ApplicationException("user not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void holdSeat(LockSeatRequestDto lockSeatRequest) {
        User user = getUser();
        Show show = showRepo.findById(lockSeatRequest.getShowId()).orElseThrow(() -> new ApplicationException("show not found", HttpStatus.NOT_FOUND));
        ShowSeat selectedShowSeat = showSeatRepo.findByShowAndSeatId(show, lockSeatRequest.getSeatId()).orElseThrow(() -> new ApplicationException("show seat not found", HttpStatus.NOT_FOUND));
        if (selectedShowSeat.getStatus() != ShowSeatStatus.AVAILABLE)
            throw new ApplicationException("Selected Show Seat has been already booked.", HttpStatus.NOT_FOUND);
        List<com.moviebooking.entity.ShowSeat> heldShowSeatsByExistingUser = showSeatRepo.findByShowAndHeldByUserIdAndStatus(show, user.getId(), ShowSeatStatus.HELD);
        if (!heldShowSeatsByExistingUser.isEmpty())
            selectedShowSeat.setHoldId(heldShowSeatsByExistingUser.get(0).getHoldId());
        else
            selectedShowSeat.setHoldId(UUID.randomUUID().toString());
        selectedShowSeat.setHeldByUserId(user.getId());
        selectedShowSeat.setStatus(ShowSeatStatus.HELD);
        heldShowSeatsByExistingUser.add(selectedShowSeat);
        LocalDateTime now = LocalDateTime.now();
        for (ShowSeat toBeheldShowSeat : heldShowSeatsByExistingUser) {
            toBeheldShowSeat.setHoldExpiresAt(now.plusMinutes(5));
        }
    }

    @Transactional
    public void releaseSeat(ReleaseSeatRequestDto releaseSeatRequest) {
        User user = getUser();
        Show show = showRepo.findById(releaseSeatRequest.getShowId()).orElseThrow(() -> new ApplicationException("show not found", HttpStatus.NOT_FOUND));
        ShowSeat showSeatToBeReleased = showSeatRepo.findByShowAndSeatId(show, releaseSeatRequest.getSeatId()).orElseThrow(() -> new ApplicationException("show seat not found", HttpStatus.NOT_FOUND));
        if (!Objects.equals(
                user.getId(),
                showSeatToBeReleased.getHeldByUserId()))
            throw new ApplicationException("user not allowed to perform this operation", HttpStatus.FORBIDDEN);

        if (showSeatToBeReleased.getStatus() != ShowSeatStatus.HELD) {
            throw new ApplicationException(
                    "Seat is not currently held",
                    HttpStatus.BAD_REQUEST
            );
        }

        showSeatToBeReleased.setHeldByUserId(null);
        showSeatToBeReleased.setStatus(ShowSeatStatus.AVAILABLE);
        showSeatToBeReleased.setHoldExpiresAt(null);
        showSeatToBeReleased.setHoldId(null);
    }

    @Transactional
    public void confirmBooking(Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }

        List<BookingSeat> bookingSeats =
                bookingSeatRepo.findByBooking(booking);

        // Confirm only if every seat is still held
        // and its hold has not expired.
        LocalDateTime now = LocalDateTime.now();

        boolean seatsAvailableForConfirmation =
                !bookingSeats.isEmpty()
                        && bookingSeats.stream().allMatch(bs -> {
                    ShowSeat seat = bs.getShowSeat();

                    return seat.getStatus() == ShowSeatStatus.HELD
                            && seat.getHoldExpiresAt() != null
                            && seat.getHoldExpiresAt().isAfter(now);
                });

        if (!seatsAvailableForConfirmation) {
            booking.setStatus(BookingStatus.PAYMENT_UNRESOLVED);
            return;
        }

        for (BookingSeat bookingSeat : bookingSeats) {

            ShowSeat showSeat = bookingSeat.getShowSeat();

            showSeat.setStatus(ShowSeatStatus.BOOKED);
            showSeat.setHeldByUserId(null);
            showSeat.setHoldExpiresAt(null);
            showSeat.setHoldId(null);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Transactional
    public void expireBooking(Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.EXPIRED);
        }
    }

    @Transactional
    public void markPaymentUnresolved(Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.PAYMENT_UNRESOLVED);
        }
    }

    @Transactional
    public void markRefundRequired(Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() == BookingStatus.PAYMENT_UNRESOLVED) {
            booking.setStatus(BookingStatus.REFUND_REQUIRED);
        }
    }

    @Transactional
    public void markUnresolvedPaymentFailed(Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow();

        if (booking.getStatus() == BookingStatus.PAYMENT_UNRESOLVED) {
            booking.setStatus(BookingStatus.FAILED);
        }
    }
}
