-- Add sample books to the database
INSERT INTO books (title, author, isbn, publication_date, publisher, category, description, available_copies, total_copies, cover_image_url, language, pages)
VALUES 
('To Kill a Mockingbird', 'Harper Lee', '9780061120084', '1960-07-11', 'HarperCollins', 'Fiction', 'The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it.', 5, 5, 'https://m.media-amazon.com/images/I/71FxgtFKcQL._AC_UF1000,1000_QL80_.jpg', 'English', 336),

('1984', 'George Orwell', '9780451524935', '1949-06-08', 'Signet Classic', 'Fiction', 'A dystopian novel set in Airstrip One, a province of the superstate Oceania in a world of perpetual war, omnipresent government surveillance, and public manipulation.', 3, 3, 'https://m.media-amazon.com/images/I/71kxa1-0mfL._AC_UF1000,1000_QL80_.jpg', 'English', 328),

('The Great Gatsby', 'F. Scott Fitzgerald', '9780743273565', '1925-04-10', 'Scribner', 'Fiction', 'The story of the fabulously wealthy Jay Gatsby and his love for the beautiful Daisy Buchanan.', 4, 4, 'https://m.media-amazon.com/images/I/71FTb9X6wsL._AC_UF1000,1000_QL80_.jpg', 'English', 180),

('Pride and Prejudice', 'Jane Austen', '9780141439518', '1813-01-28', 'Penguin Classics', 'Romance', 'The story follows the main character, Elizabeth Bennet, as she deals with issues of manners, upbringing, morality, education, and marriage.', 2, 2, 'https://m.media-amazon.com/images/I/71Q1tPupKjL._AC_UF1000,1000_QL80_.jpg', 'English', 432),

('The Hobbit', 'J.R.R. Tolkien', '9780547928227', '1937-09-21', 'Houghton Mifflin Harcourt', 'Fantasy', 'The adventure of Bilbo Baggins, a hobbit who embarks on an unexpected journey.', 6, 6, 'https://m.media-amazon.com/images/I/710+HcoP38L._AC_UF1000,1000_QL80_.jpg', 'English', 300),

('Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', '9780747532743', '1997-06-26', 'Bloomsbury', 'Fantasy', 'The first novel in the Harry Potter series, it follows Harry Potter, a young wizard who discovers his magical heritage.', 8, 8, 'https://m.media-amazon.com/images/I/81m1s4wIPML._AC_UF1000,1000_QL80_.jpg', 'English', 223),

('The Catcher in the Rye', 'J.D. Salinger', '9780316769488', '1951-07-16', 'Little, Brown and Company', 'Fiction', 'The story of Holden Caulfield, a teenage boy who has been expelled from prep school and is wandering around New York City.', 3, 3, 'https://m.media-amazon.com/images/I/91HPG31dTwL._AC_UF1000,1000_QL80_.jpg', 'English', 277),

('The Lord of the Rings', 'J.R.R. Tolkien', '9780618640157', '1954-07-29', 'Houghton Mifflin Harcourt', 'Fantasy', 'An epic high-fantasy novel that follows the quest to destroy the One Ring.', 5, 5, 'https://m.media-amazon.com/images/I/71jLBXtWJWL._AC_UF1000,1000_QL80_.jpg', 'English', 1178),

('The Alchemist', 'Paulo Coelho', '9780062315007', '1988-01-01', 'HarperOne', 'Fiction', 'The story of Santiago, an Andalusian shepherd boy who yearns to travel in search of a worldly treasure.', 4, 4, 'https://m.media-amazon.com/images/I/51Z0nLAfLmL.jpg', 'English', 208),

('Brave New World', 'Aldous Huxley', '9780060850524', '1932-01-01', 'Harper Perennial', 'Science Fiction', 'A dystopian novel set in a futuristic World State, inhabited by genetically modified citizens and an intelligence-based social hierarchy.', 3, 3, 'https://m.media-amazon.com/images/I/81zE42gT3xL._AC_UF1000,1000_QL80_.jpg', 'English', 288);
