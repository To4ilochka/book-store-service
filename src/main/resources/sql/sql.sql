-- Password: password
INSERT INTO EMPLOYEES (BIRTH_DATE, EMAIL, NAME, PASSWORD, PHONE)
VALUES ('1990-05-15', 'john.doe@email.com', 'John Doe', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-123-4567'),
       ('1985-09-20', 'jane.smith@email.com', 'Jane Smith', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-987-6543'),
       ('1978-03-08', 'bob.jones@email.com', 'Bob Jones', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-321-6789'),
       ('1982-11-25', 'alice.white@email.com', 'Alice White', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-876-5432'),
       ('1995-07-12', 'mike.wilson@email.com', 'Mike Wilson', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-234-5678'),
       ('1989-01-30', 'sara.brown@email.com', 'Sara Brown', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-876-5433'),
       ('1975-06-18', 'tom.jenkins@email.com', 'Tom Jenkins', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-345-6789'),
       ('1987-12-04', 'lisa.taylor@email.com', 'Lisa Taylor', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-789-0123'),
       ('1992-08-22', 'david.wright@email.com', 'David Wright', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-456-7890'),
       ('1980-04-10', 'emily.harris@email.com', 'Emily Harris', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', '555-098-7654');

-- Password: password
INSERT INTO CLIENTS (BALANCE, EMAIL, NAME, PASSWORD, IS_BLOCKED)
VALUES (1000.00, 'client1@example.com', 'Medelyn Wright', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (1500.50, 'client2@example.com', 'Landon Phillips', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (800.75, 'client3@example.com', 'Harmony Mason', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (1200.25, 'client4@example.com', 'Archer Harper', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (900.80, 'client5@example.com', 'Kira Jacobs', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (1100.60, 'client6@example.com', 'Maximus Kelly', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (1300.45, 'client7@example.com', 'Sierra Mitchell', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (950.30, 'client8@example.com', 'Quinton Saunders', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (1050.90, 'client9@example.com', 'Amina Clarke', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', false),
       (880.20, 'client10@example.com', 'Bryson Chavez', '$2a$10$V2Pbym4qVBoEEcc9YghPFOcvZ.jK0SPQf/anJwYUNJlLYy/bkzAvO', true);

INSERT INTO BOOKS (name, genre, age_group, price, publication_year, author, number_of_pages, characteristics, description, language)
VALUES ('The Hidden Treasure', 'Adventure', 'ADULT', 24.99, '2018-05-15', 'Emily White', 400, 'Mysterious journey','An enthralling adventure of discovery', 'ENGLISH'),
       ('Echoes of Eternity', 'Fantasy', 'TEEN', 16.50, '2011-01-15', 'Daniel Black', 350, 'Magical realms', 'A spellbinding tale of magic and destiny', 'ENGLISH'),
       ('Whispers in the Shadows', 'Mystery', 'ADULT', 29.95, '2018-08-11', 'Sophia Green', 450, 'Intriguing suspense','A gripping mystery that keeps you guessing', 'ENGLISH'),
       ('The Starlight Sonata', 'Romance', 'ADULT', 21.75, '2011-05-15', 'Michael Rose', 320, 'Heartwarming love story','A beautiful journey of love and passion', 'ENGLISH'),
       ('Beyond the Horizon', 'Science Fiction', 'CHILD', 18.99, '2004-05-15', 'Alex Carter', 280,'Interstellar adventure', 'An epic sci-fi adventure beyond the stars', 'ENGLISH'),
       ('Dancing with Shadows', 'Thriller', 'ADULT', 26.50, '2015-05-15', 'Olivia Smith', 380, 'Suspenseful twists','A thrilling tale of danger and intrigue', 'ENGLISH'),
       ('Voices in the Wind', 'Historical Fiction', 'ADULT', 32.00, '2017-05-15', 'William Turner', 500,'Rich historical setting', 'A compelling journey through time', 'ENGLISH'),
       ('Serenade of Souls', 'Fantasy', 'TEEN', 15.99, '2013-05-15', 'Isabella Reed', 330, 'Enchanting realms','A magical fantasy filled with wonder', 'ENGLISH'),
       ('Silent Whispers', 'Mystery', 'ADULT', 27.50, '2021-05-15', 'Benjamin Hall', 420, 'Intricate detective work','A mystery that keeps you on the edge', 'ENGLISH'),
       ('Whirlwind Romance', 'Romance', 'OTHER', 23.25, '2022-05-15', 'Emma Turner', 360, 'Passionate love affair','A romance that sweeps you off your feet', 'ENGLISH');