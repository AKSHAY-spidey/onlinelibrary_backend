package com.library.service.impl;

import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.payload.request.BookRequest;
import com.library.payload.response.BookResponse;
import com.library.payload.response.LoanResponse;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.service.BlockchainService;
import com.library.service.EmailService;
import com.library.service.LibrarianService;
import com.library.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibrarianServiceImpl implements LibrarianService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BlockchainService blockchainService;

    @Override
    @Transactional
    public BookResponse addBook(BookRequest bookRequest) {
        // Check if book with same ISBN already exists
        if (bookRequest.getIsbn() != null && !bookRequest.getIsbn().isEmpty() &&
                bookRepository.findByIsbn(bookRequest.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Book with ISBN " + bookRequest.getIsbn() + " already exists");
        }

        Book book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setDescription(bookRequest.getDescription());
        book.setCategory(bookRequest.getCategory());
        book.setPublicationDate(bookRequest.getPublicationDate());
        book.setPublisher(bookRequest.getPublisher());
        book.setLanguage(bookRequest.getLanguage());
        book.setPages(bookRequest.getPages());
        book.setTotalCopies(bookRequest.getTotalCopies());
        book.setAvailableCopies(bookRequest.getTotalCopies()); // Initially all copies are available
        book.setCoverImageUrl(bookRequest.getCoverImageUrl());

        Book savedBook = bookRepository.save(book);

        // Notify users who have this book in their wishlist
        notifyUsersAboutNewBook(savedBook);

        return new BookResponse(savedBook);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest bookRequest) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        // Check if ISBN is being changed and if the new ISBN already exists
        if (bookRequest.getIsbn() != null && !bookRequest.getIsbn().equals(book.getIsbn()) &&
                bookRepository.findByIsbn(bookRequest.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Book with ISBN " + bookRequest.getIsbn() + " already exists");
        }

        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setDescription(bookRequest.getDescription());
        book.setCategory(bookRequest.getCategory());
        book.setPublicationDate(bookRequest.getPublicationDate());
        book.setPublisher(bookRequest.getPublisher());
        book.setLanguage(bookRequest.getLanguage());
        book.setPages(bookRequest.getPages());

        // Handle copy count changes
        Integer oldTotalCopies = book.getTotalCopies();
        Integer newTotalCopies = bookRequest.getTotalCopies();

        if (!oldTotalCopies.equals(newTotalCopies)) {
            // Calculate the difference in available copies
            Integer loanedCopies = oldTotalCopies - book.getAvailableCopies();
            Integer newAvailableCopies = newTotalCopies - loanedCopies;

            // Ensure we don't have negative available copies
            if (newAvailableCopies < 0) {
                throw new IllegalArgumentException("Cannot reduce total copies below the number of loaned copies");
            }

            book.setTotalCopies(newTotalCopies);
            book.setAvailableCopies(newAvailableCopies);
        }

        book.setCoverImageUrl(bookRequest.getCoverImageUrl());

        Book updatedBook = bookRepository.save(book);

        // Notify users about the book update
        notifyUsersAboutNewBook(updatedBook);

        return new BookResponse(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        // Check if there are any active loans for this book
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new IllegalStateException("Cannot delete book with active loans");
        }

        bookRepository.delete(book);
    }

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> getAllLoans() {
        List<Loan> loans = loanRepository.findAll();
        return loans.stream()
                .map(LoanResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));

        // Use case-insensitive comparison for status
        if (!"PENDING".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Can only approve loans with PENDING status");
        }

        Book book = loan.getBook();

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies of this book");
        }

        // Update book available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // We already know it's PENDING from the check above
        // Just update the loan date to today if it's being approved
        loan.setStatus("APPROVED");
        loan.setLoanDate(LocalDate.now());
        loanRepository.save(loan);

        // Record the loan in the blockchain
        blockchainService.recordLoan(loan);

        // Send notification to the user
        notificationService.sendUserNotification(
            loan.getUser().getId(),
            "LOAN_APPROVED",
            "Your loan request for " + loan.getBook().getTitle() + " has been approved.",
            loan
        );
    }

    @Override
    @Transactional
    public void rejectLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));

        // Use case-insensitive comparison for status
        if (!"PENDING".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Can only reject loans with PENDING status");
        }

        loan.setStatus("REJECTED");
        loanRepository.save(loan);

        // Send notification to the user
        notificationService.sendUserNotification(
            loan.getUser().getId(),
            "LOAN_REJECTED",
            "Your loan request for " + loan.getBook().getTitle() + " has been rejected.",
            loan
        );
    }

    @Override
    @Transactional
    public void processReturn(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));

        // Use case-insensitive comparison for status
        if (!"APPROVED".equalsIgnoreCase(loan.getStatus()) && !"OVERDUE".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Can only process returns for APPROVED or OVERDUE loans. Current status: " + loan.getStatus());
        }

        Book book = loan.getBook();

        // Update book available copies
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        // Update loan status
        loan.setStatus("RETURNED");
        loan.setReturnDate(LocalDate.now());

        // Calculate fine if overdue
        if (loan.getDueDate().isBefore(LocalDate.now())) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
            double fineAmount = daysOverdue * 10.0; // ₹10 per day
            loan.setFineAmount(fineAmount);

            // Record fine payment in blockchain if there is a fine
            if (fineAmount > 0) {
                blockchainService.recordFinePayment(loan, fineAmount);
            }
        }

        loanRepository.save(loan);

        // Record the return in the blockchain
        blockchainService.recordReturn(loan);

        // Send notification to the user
        String message = "Your book return for " + loan.getBook().getTitle() + " has been processed.";
        if (loan.getFineAmount() > 0) {
            message += " A fine of ₹" + loan.getFineAmount() + " has been applied.";
        }

        notificationService.sendUserNotification(
            loan.getUser().getId(),
            "RETURN_PROCESSED",
            message,
            loan
        );
    }

    @Override
    @Transactional
    public void modifyReturnDate(Long id, LocalDate newReturnDate) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));

        // Validate the loan status - only allow modifying active loans - case insensitive
        if (!"APPROVED".equalsIgnoreCase(loan.getStatus()) && !"OVERDUE".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Can only modify return dates for APPROVED or OVERDUE loans");
        }

        // Remove the validation that prevents setting past dates
        // This allows admins to set past dates to trigger fines

        // Store the old due date for notification
        LocalDate oldDueDate = loan.getDueDate();

        // Update the due date
        loan.setDueDate(newReturnDate);

        // Recalculate fine if the loan is overdue
        if ("OVERDUE".equalsIgnoreCase(loan.getStatus())) {
            if (newReturnDate.isAfter(LocalDate.now())) {
                // If new date is in the future, loan is no longer overdue
                loan.setStatus("APPROVED");
                loan.setFineAmount(0.0);
            } else {
                // Recalculate fine based on new due date
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(newReturnDate, LocalDate.now());
                double fineAmount = daysOverdue * 10.0; // ₹10 per day
                loan.setFineAmount(fineAmount);
            }
        } else if ("APPROVED".equalsIgnoreCase(loan.getStatus()) && newReturnDate.isBefore(LocalDate.now())) {
            // If the loan was approved but the new date is in the past, mark as overdue and calculate fine
            loan.setStatus("OVERDUE");
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(newReturnDate, LocalDate.now());
            double fineAmount = daysOverdue * 10.0; // ₹10 per day
            loan.setFineAmount(fineAmount);
        }

        loanRepository.save(loan);

        // Record the change in the blockchain
        blockchainService.recordLoanModification(loan, oldDueDate, newReturnDate);

        // Send notification to the user
        String message = "The return date for your loan of " + loan.getBook().getTitle() +
                         " has been changed from " + oldDueDate + " to " + newReturnDate + ".";

        if (loan.getFineAmount() > 0) {
            message += " A fine of ₹" + loan.getFineAmount() + " has been applied.";
        } else if ("APPROVED".equalsIgnoreCase(loan.getStatus()) && "OVERDUE".equalsIgnoreCase(loan.getStatus())) {
            message += " Your loan is no longer overdue.";
        }

        notificationService.sendUserNotification(
            loan.getUser().getId(),
            "LOAN_DATE_MODIFIED",
            message,
            loan
        );

        // Send email notification
        if (loan.getUser().getEmail() != null && !loan.getUser().getEmail().isEmpty()) {
            emailService.sendLoanStatusEmail(
                loan.getUser().getEmail(),
                loan.getUser().getUsername(),
                loan.getBook().getTitle(),
                "date_modified",
                newReturnDate
            );
        }
    }

    /**
     * Notify users about a new book or book update
     * This method would typically check for users who have this book in their wishlist
     * or have shown interest in similar books
     */
    private void notifyUsersAboutNewBook(Book book) {
        // In a real implementation, you would query for users who have this book in their wishlist
        // or have shown interest in similar books based on category, author, etc.
        // For now, we'll just log the action
        System.out.println("New book added/updated: " + book.getTitle() + " by " + book.getAuthor());

        // You could also send notifications to all users or specific users
        // For example:
        // List<User> interestedUsers = userRepository.findUsersInterestedInCategory(book.getCategory());
        // for (User user : interestedUsers) {
        //     if (user.getEmail() != null && !user.getEmail().isEmpty()) {
        //         emailService.sendWishlistAvailabilityEmail(
        //             user.getEmail(),
        //             user.getUsername(),
        //             book.getTitle(),
        //             book.getAuthor()
        //         );
        //     }
        // }
    }
}
