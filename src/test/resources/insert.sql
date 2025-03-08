DELETE FROM tb_tickets;
DELETE FROM tb_events;
DELETE FROM tb_users;

INSERT INTO tb_users (user_id, is_student, password, phone_number, `role`, username) VALUES
('5f74ce15-982a-4326-8365-80e800d772f9', 1, '$2a$10$L5s.aQSiKDGOaI2mPml/2ewK5DCbm97RG1fo.ZdgQvnaTD2UHsdze', '+5548998426134', 'ORGANIZER', 'rafael'),
('5f74ce15-982a-4326-8365-80e800d772f8', 0, '$2a$10$49FJC4.Q2kCGqkFegtd.WOJzxQexvY/O672QTUlwyfMCLJRmKM8qu', '+5548998426134', 'PARTICIPANT', 'rafolas'),
('5f74ce15-982a-4326-8365-80e800d772f7', 1, '$2a$10$JYxloUxpU5R26AUaKm0YDO8MfRgyqDDXqAA/KBOK2wJsUis91aMPa', '+5548998426134', 'ADMIN', 'admin');


INSERT INTO tb_events (event_id, category, `date`, description, location, price, rating, reviews, title, organizer_id) VALUES
(1, 'Technology', '2025-06-05 10:00:00', 'A discussion about the latest trends in technology.', 'Silicon Valley Convention Center, CA', 89.99, NULL, 0, 'Tech Stars 2025', '5f74ce15-982a-4326-8365-80e800d772f7'),
(2, 'Music', '2025-06-05 10:00:00', 'A music festival with the best bands in the world.', 'Wembley Stadium, London', 99.99, NULL, 0, 'Music Fest 2025', '5f74ce15-982a-4326-8365-80e800d772f9'),
(3, 'Sports', '2025-02-05 10:00:00', 'A soccer match between the two best teams in the world.', 'Maracanã Stadium, Rio de Janeiro', 79.99, NULL, 0, 'Soccer Cup 2025', '5f74ce15-982a-4326-8365-80e800d772f7');


INSERT INTO tb_tickets (ticket_id, rating, status, event_id, user_id) VALUES
(1, NULL, 'APPROVED', 1, '5f74ce15-982a-4326-8365-80e800d772f9'),
(2, NULL, 'PENDING', 1, '5f74ce15-982a-4326-8365-80e800d772f8'),
(3, NULL, 'PENDING', 2, '5f74ce15-982a-4326-8365-80e800d772f8'),
(4, NULL, 'APPROVED', 2, '5f74ce15-982a-4326-8365-80e800d772f7'),
(5, NULL, 'APPROVED', 3, '5f74ce15-982a-4326-8365-80e800d772f9'),
(6, NULL, 'REJECTED', 3, '5f74ce15-982a-4326-8365-80e800d772f8'),
(7, NULL, 'APPROVED', 3, '5f74ce15-982a-4326-8365-80e800d772f7');
